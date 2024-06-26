package manhunt.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
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

import static manhunt.ManhuntMod.LOGGER;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    NbtList positions = new NbtList();
    private PlayerEntity player = (PlayerEntity) (Object) this;

    @Inject(at = @At("HEAD"), method = "tick")
    private void Manhunt$tick(CallbackInfo ci) {

        DataResult<NbtElement> var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, player.getWorld().getRegistryKey());
        var10000.resultOrPartial(LOGGER::error).ifPresent((dimension) -> {
            for (int i = 0; i < positions.size(); ++i) {
                NbtCompound compound = positions.getCompound(i);
                if (Objects.equals(compound.getString("LodestoneDimension"), dimension.asString())) {
                    positions.remove(compound);
                }
            }

            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.put("LodestonePos", NbtHelper.fromBlockPos(player.getBlockPos()));
            nbtCompound.put("LodestoneDimension", dimension);
            positions.add(nbtCompound);
        });
    }

    @Inject(at = @At(value = "HEAD"), method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", cancellable = true)
    private void Manhunt$dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> ci) {
        if (stack.get(DataComponentTypes.CUSTOM_DATA) != null) {
            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Remove")) {
                ci.setReturnValue(null);
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "writeCustomDataToNbt")
    private void Manhunt$addAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        nbt.putBoolean("manhuntModded", true);
        nbt.put("Positions", positions);
    }

    @Inject(at = @At("RETURN"), method = "readCustomDataFromNbt")
    public void Manhunt$readAdditionalSaveData(NbtCompound nbt, CallbackInfo cbi) {
        this.positions = nbt.getList("Positions", 10);
    }
}
