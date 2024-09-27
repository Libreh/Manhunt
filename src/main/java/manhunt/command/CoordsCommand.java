package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoordsCommand {
    public static final List<MutableText> HUNTER_COORDS = new ArrayList<>();
    public static final List<MutableText> RUNNER_COORDS = new ArrayList<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("coords")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.gameState != GameState.PREGAME)
                .executes(context -> sendCoordsMessage(context.getSource(), ""))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> sendCoordsMessage(context.getSource(),
                                StringArgumentType.getString(context, "message")))
                )
        );
    }

    private static int sendCoordsMessage(ServerCommandSource source, String message) {
        var player = source.getPlayer();
        var server = source.getServer();
        var scoreboard = server.getScoreboard();
        boolean isHunter = player.isTeamPlayer(scoreboard.getTeam("hunters"));

        Formatting formatting = Formatting.WHITE;
        if (ManhuntConfig.CONFIG.isTeamColor()) {
            if (isHunter) {
                formatting = ManhuntConfig.CONFIG.getHuntersColor();
            } else {
                formatting = ManhuntConfig.CONFIG.getRunnersColor();
            }
        }

        String team = "hunters";
        if (!isHunter) {
            team = "runners";
        }
        var yaw = player.getYaw();
        var text = createYawText(yaw);
        var biome = "unknown";
        var biomeKey = player.getWorld().getBiome(player.getBlockPos());
        if (biomeKey.getKey().isPresent()) {
            biome = Text.translatable("biome.minecraft." + biomeKey.getKey().get().getValue().getPath()).getString();
        }
        message = getDirectionFromYaw(yaw) + text[0] + text[1] + " (" + biome + ") " + message;

        if (isHunter) {
            for (ServerPlayerEntity serverPlayer : GameEvents.allHunters) {
                serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates",
                        Text.literal(team).formatted(formatting),
                        Text.literal(player.getNameForScoreboard()).formatted(formatting),
                        Text.literal(String.valueOf(player.getBlockX())),
                        Text.literal(String.valueOf(player.getBlockY())),
                        Text.literal(String.valueOf(player.getBlockZ())), Text.literal(message)));
            }
        } else {
            for (ServerPlayerEntity serverPlayer : GameEvents.allRunners) {
                serverPlayer.sendMessage(Text.translatable("chat.manhunt.send_coordinates",
                        Text.literal(team).formatted(formatting),
                        Text.literal(player.getNameForScoreboard()).formatted(formatting),
                        Text.literal(String.valueOf(player.getBlockX())),
                        Text.literal(String.valueOf(player.getBlockY())),
                        Text.literal(String.valueOf(player.getBlockZ())), Text.literal(message)));
            }
        }

        if (isHunter) {
            HUNTER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates",
                    Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(player.getBlockX())),
                    Text.literal(String.valueOf(player.getBlockY())),
                    Text.literal(String.valueOf(player.getBlockZ())), Text.literal(message)));
        } else {
            RUNNER_COORDS.add(Text.translatable("chat.manhunt.save_coordinates",
                    Text.literal(player.getNameForScoreboard()).formatted(formatting),
                    Text.literal(" " + new Date().getTime() + " "), Text.literal(String.valueOf(player.getBlockX())),
                    Text.literal(String.valueOf(player.getBlockY())),
                    Text.literal(String.valueOf(player.getBlockZ())), Text.literal(message)));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String[] createYawText(double yaw) {
        String[][] directions = {{"", "+"}, {"-", "+"}, {"-", ""}, {"-", "-"}, {"", "-"}, {"+", "-"}, {"+", ""}, {"+"
                , "+"}};

        return directions[(int) Math.round(yaw / 45.0F) & 7];
    }

    public static String getDirectionFromYaw(double degrees) {
        String direction;
        String[] directions = {"S", "SW", "W", "NW", "N", "NE", "E", "SE", "S"};
        if (degrees > 0) direction = directions[(int) Math.round(degrees / 45)];
        else {
            int index = (int) Math.round(degrees / 45) * -1;
            direction = directions[8 - index];
        }
        return direction;
    }
}
