package me.libreh.manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PauseCommands {
    public static void pauseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pause")
                .requires(source -> !isPaused && isPlaying() && requirePermissionOrOperator(source, "manhunt.pause"))
                .executes(context -> pauseCommand(5))
                .then(argument("minutes", IntegerArgumentType.integer())
                        .executes(context -> pauseCommand(IntegerArgumentType.getInteger(context, "minutes")))));
    }

    private static int pauseCommand(int minutes) {
        pauseGame(minutes);

        return Command.SINGLE_SUCCESS;
    }

    public static void pauseGame(int minutes) {
        isPaused = true;

        pauseTicks = minutes * 60 * 20;

        server.getTickManager().setFrozen(true);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);

            if (!player.getStatusEffects().isEmpty()) {
                SAVED_EFFECTS.put(player.getUuid(), player.getStatusEffects());
            }

            SAVED_POS.put(player.getUuid(), player.getPos());
            SAVED_YAW.put(player.getUuid(), player.getYaw());
            SAVED_PITCH.put(player.getUuid(), player.getPitch());

            SAVED_AIR.put(player.getUuid(), player.getAir());
            var nbtCompound = new NbtCompound();
            player.getHungerManager().writeNbt(nbtCompound);
            SAVED_HUNGER.put(player.getUuid(), nbtCompound);

            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE,
                    255, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE,
                    255, false, false));
        }
    }

    public static void unpauseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .requires(source -> isPaused && isPlaying() && requirePermissionOrOperator(source, "manhunt.unpause"))
                .executes(context -> unpauseCommand())
        );
    }

    private static int unpauseCommand() {
        unpauseGame();

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame() {
        isPaused = false;

        server.getTickManager().setFrozen(false);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);

            resetAttributes(player);
            player.clearStatusEffects();
            var playerUuid = player.getUuid();
            if (SAVED_EFFECTS.containsKey(playerUuid)) {
                for (StatusEffectInstance statusEffect : SAVED_EFFECTS.get(playerUuid)) {
                    player.addStatusEffect(statusEffect);
                }
            }

            player.getHungerManager().readNbt(SAVED_HUNGER.get(playerUuid));
        }

        SAVED_POS.clear();
        SAVED_YAW.clear();
        SAVED_PITCH.clear();
        SAVED_AIR.clear();
        SAVED_HUNGER.clear();
        SAVED_EFFECTS.clear();
    }
}
