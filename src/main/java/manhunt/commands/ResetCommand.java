package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

import static manhunt.game.ManhuntGame.*;
import static net.minecraft.server.command.CommandManager.literal;

public class ResetCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("reset")
                .executes(context -> resetCommand(context.getSource()))
        );
    }

    private static int resetCommand(ServerCommandSource source) {
        if (gameState == ManhuntState.PLAYING) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(source.getServer(), source.getServer().getWorld(overworldRegistryKey));
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(source.getServer(), source.getServer().getWorld(theNetherRegistryKey));
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(source.getServer(), source.getServer().getWorld(theEndRegistryKey));

            Path worldDirectory = FabricLoader.getInstance().getGameDir();

            try {
                FileUtils.deleteDirectory(worldDirectory.resolve("world/DIM1").toFile());
                FileUtils.deleteDirectory(worldDirectory.resolve("world/DIM-1").toFile());
            } catch (IOException e) {
                Manhunt.LOGGER.warn("Failed to delete world directory", e);
                try {
                    FileUtils.forceDeleteOnExit(worldDirectory.resolve("world/DIM1").toFile());
                    FileUtils.forceDeleteOnExit(worldDirectory.resolve("world/DIM-1").toFile());
                } catch (IOException ignored) {
                }
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
