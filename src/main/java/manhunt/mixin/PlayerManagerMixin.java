package manhunt.mixin;

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

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    public PlayerManagerMixin() {
        super();
    }

    @Inject(at = @At(value = "RETURN"), method = "loadPlayerData", cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (gameState == PREGAME) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "manhunt:lobby");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(0.5));
            position.add(NbtDouble.of(63));
            position.add(NbtDouble.of(0));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0f));
            rotation.add(NbtFloat.of(0f));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        } else if (ci.getReturnValue() == null && gameState == PLAYING) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "overworld");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(player.getServer().getOverworld().getSpawnPos().getX()));
            position.add(NbtDouble.of(player.getServer().getOverworld().getSpawnPos().getY()));
            position.add(NbtDouble.of(player.getServer().getOverworld().getSpawnPos().getZ()));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0f));
            rotation.add(NbtFloat.of(0f));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
    }
}