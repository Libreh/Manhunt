package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PingSoundCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pingsound")
                .then(argument("sound", IdentifierArgumentType.identifier())
                        .suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .executes(context -> setPingSound(context.getSource(), IdentifierArgumentType.getIdentifier(context, "sound")))
                )
        );
    }

    private static int setPingSound(ServerCommandSource source, Identifier pingSound) {
        Manhunt.table.get(source.getPlayer().getUuid()).put("pingsound", pingSound.toString());

        source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.set.to", "Ping sound", pingSound.toString()), false);

        return Command.SINGLE_SUCCESS;
    }
}
