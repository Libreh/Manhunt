package manhunt.mixin.game;

import manhunt.ManhuntMod;
import manhunt.game.GameEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At(value = "RETURN"), cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ci.getReturnValue() == null) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", ManhuntMod.LOBBY_REGISTRY_KEY.getValue().toString());

            NbtList position = new NbtList();
            position.add(NbtDouble.of(GameEvents.LOBBY_SPAWN_DOUBLE.x));
            position.add(NbtDouble.of(GameEvents.LOBBY_SPAWN_DOUBLE.y));
            position.add(NbtDouble.of(GameEvents.LOBBY_SPAWN_DOUBLE.z));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(180.0F));
            rotation.add(NbtFloat.of(0.0F));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
    }
}