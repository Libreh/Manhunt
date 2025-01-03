package me.libreh.manhunt.command.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import static me.libreh.manhunt.game.GameState.PREGAME;
import static me.libreh.manhunt.utils.Fields.OVERWORLD;
import static me.libreh.manhunt.utils.Fields.gameState;
import static me.libreh.manhunt.utils.Methods.isPreGame;
import static net.minecraft.server.command.CommandManager.literal;

public class DurationCommand {
    public static String duration;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("duration")
                .requires(source -> !isPreGame())
                .executes(context -> showDuration(context.getSource())));
    }

    private static int showDuration(ServerCommandSource source) {
        setDuration();

        source.sendFeedback(() -> Text.translatable("chat.manhunt.duration",
                Texts.bracketedCopyable(DurationCommand.duration).formatted(Formatting.GREEN)), false);

        return Command.SINGLE_SUCCESS;
    }

    public static void setDuration() {
        if (gameState != PREGAME) {
            String hoursString;
            int hours = (int) Math.floor((double) OVERWORLD.getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));

            if (hours <= 9) {
                hoursString = "0" + hours;
            } else {
                hoursString = String.valueOf(hours);
            }

            String minutesString;
            int minutes = (int) Math.floor((double) OVERWORLD.getTime() % (20 * 60 * 60) / (20 * 60));

            if (minutes <= 9) {
                minutesString = "0" + minutes;
            } else {
                minutesString = String.valueOf(minutes);
            }

            String secondsString;
            int seconds = (int) Math.floor((double) OVERWORLD.getTime() % (20 * 60) / (20));

            if (seconds <= 9) {
                secondsString = "0" + seconds;
            } else {
                secondsString = String.valueOf(seconds);
            }

            duration = hoursString + ":" + minutesString + ":" + secondsString;
        }
    }
}
