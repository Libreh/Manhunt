package me.libreh.manhunt.command.game.pause;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.SERVER;
import static me.libreh.manhunt.utils.Fields.isPaused;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.literal;

public class UnpauseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .requires(
                        source -> isPlaying() && isPaused &&
                                (source.isExecutedByPlayer() && hasPermission(source.getPlayer(), "manhunt.command.unpause") ||
                                        !source.isExecutedByPlayer() ||
                                        isRunner(source.getPlayer()))
                ).executes(context -> unpauseCommand())
        );
    }

    private static int unpauseCommand() {
        unpauseGame();

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame() {
        isPaused = false;

        SERVER.getTickManager().setFrozen(false);

        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
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