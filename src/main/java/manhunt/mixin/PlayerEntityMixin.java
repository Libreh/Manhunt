package manhunt.mixin;

import com.mojang.serialization.DataResult;
import mrnavastar.sqlib.DataContainer;
import mrnavastar.sqlib.Table;
import mrnavastar.sqlib.database.MySQLDatabase;
import mrnavastar.sqlib.sql.SQLDataType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DeathMessageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static manhunt.Manhunt.MOD_ID;
import static manhunt.config.ManhuntConfig.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    NbtList positions = new NbtList();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {

        DataResult<NbtElement> var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, getWorld().getRegistryKey());
        Logger logger = LoggerFactory.getLogger("Manhunt");
        var10000.resultOrPartial(logger::error).ifPresent((dimension) -> {
            for (int i = 0; i < positions.size(); ++i) {
                NbtCompound compound = positions.getCompound(i);
                if (Objects.equals(compound.getString("LodestoneDimension"), dimension.asString())) {
                    positions.remove(compound);
                }
            }
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put("LodestonePos", NbtHelper.fromBlockPos(this.getBlockPos()));
            nbtCompound.put("LodestoneDimension", dimension);
            positions.add(nbtCompound);
        });
    }

    @Inject(at = @At(value = "HEAD"), method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", cancellable = true)
    public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> ci) {
        if (stack.hasNbt()) {
            if (stack.getNbt().getBoolean("Remove")) {
                ci.setReturnValue(null);
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    public void addAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        nbt.putBoolean("manhuntModded", true);
        nbt.put("Positions", positions);
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    public void readAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        this.positions = nbt.getList("Positions", 10);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setAbsorptionAmount(F)V"), method = "applyDamage", cancellable = true)
    private void cancelDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (bedExplosions && source.getType().deathMessageType() == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            ci.cancel();
        }
    }

    public DataContainer getPlayerData(NbtCompound nbt) {
        final MySQLDatabase database = new MySQLDatabase(MOD_ID, databaseName, databaseAddress, databasePort, databaseUser, databasePassword);
        Table table = database.createTable("players")
                .addColumn("muteMusic", SQLDataType.BOOL)
                .addColumn("muteLobbyMusic", SQLDataType.BOOL)
                .addColumn("doNotDisturb", SQLDataType.BOOL)
                .addColumn("pingSound", SQLDataType.STRING)
                .addColumn("lobbyRole", SQLDataType.STRING)
                .finish();
        DataContainer playerData = table.get(this.getUuidAsString());
        if (nbt.getBoolean("playerData")) {
            if (table.get(this.getUuidAsString()) == null) {
                playerData = table.createDataContainer(this.getUuidAsString());
            }
        }
        return playerData;
    }
}
