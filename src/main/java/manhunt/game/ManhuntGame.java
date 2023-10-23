package manhunt.game;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import nota.Nota;

import static manhunt.config.ManhuntConfig.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class ManhuntGame {

    public static ManhuntState state;

    public static void state(ManhuntState state, MinecraftServer server) {
        server.setMotd(state.getColor() + "[" + state.getMotd() + "]§f Minecraft MANHUNT");
        ManhuntGame.state = state;
    }

    public static void start(MinecraftServer server) {
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "team remove players");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky cancel");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky confirm");

        server.setFlightEnabled(false);
        server.getPlayerManager().setWhitelistEnabled(false);

        ManhuntGame.state(PLAYING, server);

        var world = server.getOverworld();
        world.setTimeOfDay(0);

        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
        server.getGameRules().get(GameRules.DO_INSOMNIA).set(true, server);
        server.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, server);
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);
        server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(3, server);
        server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        server.getGameRules().get(GameRules.SPAWN_RADIUS).set(10, server);

        server.setPvpEnabled(true);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.teleport(world, world.getSpawnPos().getX(), world.getSpawnPos().getY(), world.getSpawnPos().getZ(), 0, 0);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);

            for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
                AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                for(String criteria : progress.getObtainedCriteria()) {
                    player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                }
            }

            updateGameMode(player);

            if (gameTitles) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.gamemode")));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.start")));
            }
            player.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                        SoundEvents.BLOCK_NOTE_BLOCK_PLING,
                        SoundCategory.BLOCKS,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        0.5f, 2f, 0
                    )
            );

            if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                if (hunterFreeze != 0) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, hunterFreeze * 20, 255, false, true));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, hunterFreeze * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, hunterFreeze * 20, 248, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (hunterFreeze - 1) * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, hunterFreeze * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, hunterFreeze * 20, 255, false, false));
                }
            }
            Nota.stopPlaying(player);

            if (hunterFreeze == 0) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.hunterfreeze"), Text.literal(hunterFreeze + " seconds (disabled)").formatted(Formatting.RED)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.hunterfreeze"), Text.literal(hunterFreeze + " seconds").formatted(Formatting.GREEN)), false);
            }
            if (timeLimit == 0) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.timelimit"), Text.literal(timeLimit + " minutes (disabled)").formatted(Formatting.RED)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.timelimit"), Text.literal(timeLimit + " minutes").formatted(Formatting.GREEN)), false);
            }
            if (compassUpdate.equals("Automatic")) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.compassupdate"), Text.literal("Automatic").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.compassupdate"), Text.literal("Manual").formatted(Formatting.RED)), false);
            }
            if (dimensionInfo) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.dimensioninfo"), Text.literal("Show").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.dimensioninfo"), Text.literal("Hide").formatted(Formatting.RED)), false);
            }
            if (latePlayers) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.lateplayers"), Text.literal("Join Hunters").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.lateplayers"), Text.literal("Join Spectators").formatted(Formatting.RED)), false);
            }
            if (teamColor) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.teamcolor"), Text.literal("Show").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.teamcolor"), Text.literal("Hide").formatted(Formatting.RED)), false);
            }
            if (bedExplosions) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bedexplosions"), Text.literal("Enabled").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bedexplosions"), Text.literal("Disabled").formatted(Formatting.RED)), false);
            }
            if (worldDifficulty.equals("easy")) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Easy").formatted(Formatting.GREEN)), false);
            } else if (worldDifficulty.equals("normal")) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Normal").formatted(Formatting.GOLD)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Hard").formatted(Formatting.RED)), false);
            }
            if (borderSize == 59999968) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bordersize"), Text.literal(borderSize + " blocks (maximum)").formatted(Formatting.RED)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bordersize"), Text.literal(borderSize + " blocks").formatted(Formatting.GREEN)), false);
            }
            if (gameTitles) {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.gametitles"), Text.literal("Show").formatted(Formatting.GREEN)), false);
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.gametitles"), Text.literal("Hide").formatted(Formatting.RED)), false);
            }
        }
    }

    public static void updateGameMode(ServerPlayerEntity player) {
        if (ManhuntGame.state == PREGAME) {
            player.changeGameMode(GameMode.ADVENTURE);
        } else if (ManhuntGame.state == PLAYING) {
            player.changeGameMode(GameMode.SURVIVAL);
        } else {
            player.changeGameMode(GameMode.SPECTATOR);
        }
    }
}
