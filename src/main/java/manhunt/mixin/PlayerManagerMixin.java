package manhunt.mixin;

import manhunt.config.ManhuntConfig;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import nota.model.Playlist;
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
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    public PlayerManagerMixin() {
        super();
    }

    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
        if (getPlayerData(player).getString("pingSound") == null) {
            playerData.put(player.getUuid(), true);
            muteMusic.put(player.getUuid(), false);
            muteLobbyMusic.put(player.getUuid(), false);
            doNotDisturb.put(player.getUuid(), false);
            currentRole.put(player.getUuid(), "hunter");
        } else {
            if (getPlayerData(player).getBool("muteMusic")) {
                muteMusic.put(player.getUuid(), true);
            } else {
                muteMusic.put(player.getUuid(), false);
            }
            if (getPlayerData(player).getBool("muteLobbyMusic")) {
                muteLobbyMusic.put(player.getUuid(), true);
            } else {
                muteLobbyMusic.put(player.getUuid(), false);
            }
            if (getPlayerData(player).getBool("doNotDisturb")) {
                doNotDisturb.put(player.getUuid(), true);
            } else {
                doNotDisturb.put(player.getUuid(), false);
            }
        }

        if (!getPlayerData(player).getBool("muteMusic") && !getPlayerData(player).getBool("muteLobbyMusic")) {
            playLobbyMusic(player);
        }

        updateGameMode(player);

        parkourTimer.put(player.getUuid(), 0);
        startedParkour.put(player.getUuid(), false);
        finishedParkour.put(player.getUuid(), false);

        if (ManhuntGame.state == ManhuntState.PREPARING) {

            player.getInventory().clear();

            ManhuntGame.updateGameMode(player);
            player.setHealth(20);
            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 0.5f);

            var world = player.getWorld();

            if (world.getBlockState(new BlockPos(2, 60, 24)).getBlock().equals(Blocks.AIR)) {
                world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource().withSilent(), "setblock 2 60 24 ice");
                world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource().withSilent(), "summon glow_squid 2 60 27 {NoGravity:1b,Silent:1b,Invulnerable:1b,NoAI:1b}");
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

        if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
            getPlayerData(player).put("lobbyRole", "leader");
        } else if (!player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
            getPlayerData(player).put("lobbyRole", "player");
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "remove")
    public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo info) {
        getPlayerData(player).put("muteMusic", muteMusic.get(player.getUuid()));
        getPlayerData(player).put("muteLobbyMusic", muteLobbyMusic.get(player.getUuid()));
        getPlayerData(player).put("doNotDisturb", doNotDisturb.get(player.getUuid()));
    }

    @Inject(at = @At(value = "RETURN"), method = "loadPlayerData", cancellable = true)
    private void loadPlayerData(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> ci) {
        if (ci.getReturnValue() == null && ManhuntGame.state == ManhuntState.PREGAME) {
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
        } else if (ci.getReturnValue() == null && ManhuntGame.state == ManhuntState.PLAYING) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Dimension", "overworld");

            NbtList position = new NbtList();
            position.add(NbtDouble.of(ManhuntGame.findSpawnPos(player.getServer().getOverworld()).getX()));
            position.add(NbtDouble.of(ManhuntGame.findSpawnPos(player.getServer().getOverworld()).getY()));
            position.add(NbtDouble.of(ManhuntGame.findSpawnPos(player.getServer().getOverworld()).getZ()));
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
        Song elevatorMusic = NBSDecoder.parse(new File(ManhuntConfig.musicDirectory + "/" + "elevatorMusic.nbs"));
        Song localForecast = NBSDecoder.parse(new File(ManhuntConfig.musicDirectory + "/" + "localForecast.nbs"));
        Song soChill = NBSDecoder.parse(new File(ManhuntConfig.musicDirectory + "/" + "soChill.nbs"));
        Playlist lobbyMusic = new Playlist(soChill, localForecast, elevatorMusic);
        RadioSongPlayer rsp = new RadioSongPlayer(lobbyMusic);
        rsp.setVolume(Byte.parseByte("20"));
        rsp.addPlayer(player);
        rsp.setPlaying(true);
        player.sendMessage(Text.translatable("manhunt.jukebox.playing", Text.translatable(rsp.getSong().getPath().getAbsoluteFile().getName())));
        player.sendMessage(Text.translatable("manhunt.jukebox.cancel"));
        player.sendMessage(Text.translatable("manhunt.jukebox.permanent"));
        player.sendMessage(Text.translatable("manhunt.mutelobbymusic.disable"));
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