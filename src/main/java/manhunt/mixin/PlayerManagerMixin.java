package manhunt.mixin;

import manhunt.config.ManhuntConfig;
import manhunt.database.DatabasePlayer;
import manhunt.database.PostgreSQLDatabase;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import nota.Nota;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

import static manhunt.Manhunt.*;
import static manhunt.config.ManhuntConfig.setRoles;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    public PlayerManagerMixin() {
        super();
    }

    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
        PostgreSQLDatabase database = new PostgreSQLDatabase();

        for (DatabasePlayer databasePlayer : database.getPlayersDataFromDatabase()) {
            if (databasePlayer.getName().equals(player.getName().getString())) {
                if (databasePlayer.isMuteLobbyMusic()) {
                    muteLobbyMusic.put(player.getUuid(), true);
                } else {
                    muteLobbyMusic.put(player.getUuid(), false);
                }
                if (databasePlayer.isDoNotDisturb()) {
                    doNotDisturb.put(player.getUuid(), true);
                } else {
                    doNotDisturb.put(player.getUuid(), false);
                }
            }
        }

        if (ManhuntGame.state == PREGAME) {
            for (DatabasePlayer databasePlayer : database.getPlayersDataFromDatabase()) {
                if (databasePlayer.getUuid().equals(player.getUuid().toString())) {
                    if (!databasePlayer.isMuteLobbyMusic()) {
                        Nota.stopPlaying(player);
                        playLobbyMusic(player);
                    }
                }
            }

            updateGameMode(player);

            parkourTimer.put(player.getUuid(), 0);
            startedParkour.put(player.getUuid(), false);
            finishedParkour.put(player.getUuid(), false);

            player.getInventory().clear();

            ManhuntGame.updateGameMode(player);
            player.setHealth(20);
            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 0.5f);

            if (!setRoles.equals("Free Select")) {
                NbtCompound nbt = new NbtCompound();
                nbt.putBoolean("Remove", true);
                ItemStack itemStack = new ItemStack(Items.BARRIER);
                itemStack.setNbt(nbt);
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    serverPlayer.getInventory().setStack(3, itemStack);
                    serverPlayer.getInventory().setStack(5, itemStack);
                }
                if (setRoles.equals("All Runners")) {
                    currentRole.put(player.getUuid(), "runner");
                    player.getServer().getScoreboard().addPlayerToTeam(player.getName().getString(), player.getServer().getScoreboard().getTeam("runners"));
                }
            }

            if (setRoles.equals("Free Select") || setRoles.equals("All Hunters")) {
                currentRole.put(player.getUuid(), "hunter");
                player.getServer().getScoreboard().addPlayerToTeam(player.getName().getString(), player.getServer().getScoreboard().getTeam("hunters"));
            }

            if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
                database.insertPlayerDataToDatabase(player, "gameleader", "true");
            } else if (!player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
                database.insertPlayerDataToDatabase(player, "gameleader", "false");
            }
        }

        if (ManhuntGame.state == ManhuntState.PLAYING) {
            Team hunters = player.getScoreboard().getTeam("hunters");
            Team runners = player.getScoreboard().getTeam("runners");

            if (!player.isTeamPlayer(hunters) && !player.isTeamPlayer(runners)) {
                if (ManhuntConfig.latePlayers) {
                    player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
                } else {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "remove")
    public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo info) {
        PostgreSQLDatabase database = new PostgreSQLDatabase();
        database.insertPlayerDataToDatabase(player, "mutelobbymusic", muteLobbyMusic.get(player.getUuid()).toString());
        database.insertPlayerDataToDatabase(player, "donotdisturb", doNotDisturb.get(player.getUuid()).toString());
    }

    @Inject(at = @At(value = "RETURN"), method = "loadPlayerData", cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ManhuntGame.state == PREGAME) {
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
        } else if (ci.getReturnValue() == null && ManhuntGame.state == PLAYING) {
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

    private void playLobbyMusic(ServerPlayerEntity player) {
        Song soChill = NBSDecoder.parse(new File(FabricLoader.getInstance().getGameDir().resolve("nbs") + "/" + "soChill.nbs"));
        RadioSongPlayer rsp = new RadioSongPlayer(soChill);
        rsp.setVolume(Byte.parseByte("20"));
        rsp.addPlayer(player);
        rsp.setPlaying(true);
        player.sendMessage(Text.translatable("manhunt.jukebox.mutelobbymusic"));
        player.sendMessage(Text.translatable("manhunt.jukebox.volume"));
    }

    private void updateGameMode(ServerPlayerEntity player) {
        if(ManhuntGame.state == PREGAME) {
            player.changeGameMode(GameMode.ADVENTURE);
        }else if(ManhuntGame.state == PLAYING) {
            player.changeGameMode(GameMode.SURVIVAL);
        } else {
            player.changeGameMode(GameMode.SPECTATOR);
        }
    }
}