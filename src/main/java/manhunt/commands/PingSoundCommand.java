package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static manhunt.Manhunt.getPlayerData;
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
        getPlayerData(source.getPlayer()).put("pingSound", pingSound.toString());

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Ping sound"), Text.literal(pingSound.toString()).formatted(Formatting.GRAY)), false);

        return Command.SINGLE_SUCCESS;
    }
}
