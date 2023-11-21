package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.server.command.ServerCommandSource;

import static manhunt.game.ManhuntGame.lobbyRegistryKey;
import static net.minecraft.server.command.CommandManager.literal;

public class DurationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("duration")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        if (source.getWorld() != source.getServer().getWorld(lobbyRegistryKey)) {
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
