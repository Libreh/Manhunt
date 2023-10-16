package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class DurationCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("duration")
                .executes(context -> sendTeamCoords(context.getSource()))
        );
    }

    private static int sendTeamCoords(ServerCommandSource source) {
        source.getPlayer().sendMessage(Text.translatable("manhunt.chat.duration", Math.floor((double) source.getServer().getTicks() / (20 * 60 * 60 * 24)), Math.floor((double) source.getServer().getTicks() / (20 * 60 * 60)), source.getServer().getTicks() / (20 * 60)));

        return Command.SINGLE_SUCCESS;
    }
}
