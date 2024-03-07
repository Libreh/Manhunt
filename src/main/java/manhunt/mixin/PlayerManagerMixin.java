package manhunt.mixin;

import eu.pb4.playerdata.api.PlayerDataApi;
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

import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.*;

// Thanks to https://gitlab.com/horrific-tweaks/bingo

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    public PlayerManagerMixin() {
        super();
    }

    @Inject(at = @At(value = "RETURN"), method = "loadPlayerData", cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ci.getReturnValue() == null && gameState == PREGAME) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "manhunt:lobby");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(0));
            position.add(NbtDouble.of(63));
            position.add(NbtDouble.of(5.5));
            nbt.put("Pos", position);

            NbtList rotation = new NbtList();
            rotation.add(NbtFloat.of(0));
            rotation.add(NbtFloat.of(0));
            nbt.put("Rotation", rotation);

            player.readNbt(nbt);
            ci.setReturnValue(nbt);
        }
        if (ci.getReturnValue() == null && gameState == PLAYING || ci.getReturnValue() == null &&   gameState == POSTGAME) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "manhunt:overworld");

            setPlayerSpawnXYZ(player.getServer().getWorld(overworldRegistryKey), player);

            NbtList position = new NbtList();
            double playerX = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnX)));
            double playerY = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnY)));
            double playerZ = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnZ)));
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