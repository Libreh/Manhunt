package manhunt.mixin;

import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

// Thanks to https://gitlab.com/horrific-tweaks/bingo

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
            position.add(NbtDouble.of(0));
            position.add(NbtDouble.of(63));
            position.add(NbtDouble.of(5.5));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0.0F));
            rotation.add(NbtFloat.of(0.0F));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        } else if (ci.getReturnValue() == null && gameState == PLAYING) {
            MinecraftServer server = Manhunt.SERVER;

            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "overworld");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(ManhuntGame.worldSpawnPos.getX()));
            position.add(NbtDouble.of(ManhuntGame.worldSpawnPos.getY()));
            position.add(NbtDouble.of(ManhuntGame.worldSpawnPos.getZ()));
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