package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .executes(context -> setOneselfHunter(context.getSource()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setSomeoneHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneselfHunter(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        ManhuntGame.currentRole.put(player.getUuid(), "hunter");

        MessageUtil.sendBroadcast("manhunt.chat.role.hunter", player.getName().getString());

        return Command.SINGLE_SUCCESS;
    }

    private static int setSomeoneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
            ManhuntGame.currentRole.put(player.getUuid(), "hunter");

            MessageUtil.sendBroadcast("manhunt.chat.role.hunter", player.getName().getString());
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
