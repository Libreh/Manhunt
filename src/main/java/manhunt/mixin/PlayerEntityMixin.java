package manhunt.mixin;

import com.mojang.serialization.DataResult;
import manhunt.Manhunt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated.

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    NbtList positions = new NbtList();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {

        DataResult<NbtElement> var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, getWorld().getRegistryKey());
        var10000.resultOrPartial(Manhunt.LOGGER::error).ifPresent((dimension) -> {
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
}
