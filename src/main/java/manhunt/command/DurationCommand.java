package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import static manhunt.game.ManhuntGame.gameState;
import static manhunt.game.ManhuntGame.previousDuration;
import static manhunt.game.ManhuntState.PLAYING;

public class DurationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("duration")
                .executes(context -> showDuration(context.getSource()))
        );
    }

    private static int showDuration(ServerCommandSource source) {
        if (gameState == PLAYING) {
            String hoursString;
            int hours = (int) Math.floor((double) source.getWorld().getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));
            if (hours <= 9) {
                hoursString = "0" + hours;
            } else {
                hoursString = String.valueOf(hours);
            }
            String minutesString;
            int minutes = (int) Math.floor((double) source.getWorld().getTime() % (20 * 60 * 60) / (20 * 60));
            if (minutes <= 9) {
                minutesString = "0" + minutes;
            } else {
                minutesString = String.valueOf(minutes);
            }
            String secondsString;
            int seconds = (int) Math.floor((double) source.getWorld().getTime() % (20 * 60) / (20));
            if (seconds <= 9) {
                secondsString = "0" + seconds;
            } else {
                secondsString = String.valueOf(seconds);
            }
            previousDuration = hoursString + ":" + minutesString + ":" + secondsString;
            MutableText duration = Texts.bracketedCopyable(previousDuration);
            source.sendFeedback(() -> Text.translatable("manhunt.chat.show", Text.translatable("manhunt.duration"), duration), false);
        } else if (gameState == ManhuntState.PREGAME) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.pregame").formatted(Formatting.RED), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
