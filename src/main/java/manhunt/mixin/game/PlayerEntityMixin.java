package manhunt.mixin.game;

import com.mojang.serialization.DataResult;
import manhunt.ManhuntMod;
import manhunt.event.OnGameTick;
import manhunt.game.ManhuntGame;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public abstract Scoreboard getScoreboard();

    @Shadow
    public abstract String getNameForScoreboard();

    @Unique
    private int tickCount;
    @Unique
    NbtList positions = new NbtList();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        tickCount++;
        if (tickCount == 10) {
            if (this.isTeamPlayer(this.getScoreboard().getTeam("runners"))) {
                DataResult<NbtElement> var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE,
                        this.getWorld().getRegistryKey());
                var10000.resultOrPartial(ManhuntMod.LOGGER::error).ifPresent((dimension) -> {
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
            tickCount = 0;
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value =
            "HEAD"), cancellable = true)
    private void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership,
                          CallbackInfoReturnable<ItemEntity> ci) {
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && customData.copyNbt().getBoolean("Remove")) {
            ci.setReturnValue(null);
            ci.cancel();
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void addAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        if (ManhuntGame.POSITIONS_LIST.contains(this.getUuid())) {
            ManhuntGame.POSITIONS_LIST.remove(this.getUuid());
            positions = new NbtList();
            nbt.remove("Positions");
        }
        nbt.putBoolean("manhuntModded", true);
        nbt.put("Positions", positions);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        this.positions = nbt.getList("Positions", 10);
    }

    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), cancellable = true)
    private void disableDamage(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (OnGameTick.headStart) {
            cir.cancel();
        }
    }
}
