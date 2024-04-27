package manhunt.mixin;

import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
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
public abstract class PlayerManagerMixin {
    public PlayerManagerMixin() {
        super();
    }

    @Inject(at = @At(value = "RETURN"), method = "loadPlayerData", cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ManhuntMod.getGameState() == GameState.PREGAME) {
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
        } else if (ci.getReturnValue() == null && ManhuntMod.getGameState() == GameState.PLAYING || ci.getReturnValue() == null && ManhuntMod.getGameState() == GameState.POSTGAME) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "manhunt:overworld");

            ManhuntGame.setPlayerSpawn(player.getServer().getWorld(ManhuntMod.overworldKey), player);

            NbtList position = new NbtList();
            double playerX = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getX()));
            double playerY = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getY()));
            double playerZ = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getZ()));
            position.add(NbtDouble.of(playerX));
            position.add(NbtDouble.of(playerY));
            position.add(NbtDouble.of(playerZ));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0));
            rotation.add(NbtFloat.of(0));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
    }
}