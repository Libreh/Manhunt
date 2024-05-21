package manhunt.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static manhunt.ManhuntMod.overworldWorld;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At(value = "RETURN"), cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ci.getReturnValue() == null) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "manhunt:lobby");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(0.5));
            position.add(NbtDouble.of(63));
            position.add(NbtDouble.of(0.5));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0));
            rotation.add(NbtFloat.of(0));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld redirectOverworld(@NotNull MinecraftServer server, @NotNull ServerPlayerEntity player, boolean alive) {
        return overworldWorld;
    }
}