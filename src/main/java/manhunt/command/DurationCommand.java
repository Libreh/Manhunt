package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DurationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("duration")
                .executes(context -> showDuration(context.getSource()))
        );
    }

    private static int showDuration(ServerCommandSource source) {
        if (source.getWorld() != Manhunt.SERVER.getWorld(ManhuntGame.lobbyRegistryKey)) {
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
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.duration",  hoursString, minutesString, secondsString), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
