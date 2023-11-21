package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static manhunt.game.ManhuntGame.currentRole;
import static net.minecraft.server.command.CommandManager.literal;

public class HunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setHunter(ServerCommandSource source, ServerPlayerEntity player) {
        currentRole.put(player.getUuid(), "hunter");

        source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.role.hunter", player.getName().getString()), false);

        return Command.SINGLE_SUCCESS;
    }
}
