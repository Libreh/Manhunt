package manhunt.mixin;

import manhunt.game.GameState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static manhunt.ManhuntMod.lobbySpawn;
import static manhunt.ManhuntMod.state;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "loadPlayerData", at = @At(value = "RETURN"), cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ci.getReturnValue() == null && state == GameState.PREGAME) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", Identifier.of("manhunt", "lobby").toString());

            NbtList position = new NbtList();
            position.add(NbtDouble.of(lobbySpawn.x));
            position.add(NbtDouble.of(lobbySpawn.y));
            position.add(NbtDouble.of(lobbySpawn.z));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(180f));
            rotation.add(NbtFloat.of(0f));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
    }
}