package me.libreh.manhunt.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class ServerTaskExecutor implements Executor {
    private final MinecraftServer server;

    public ServerTaskExecutor(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        server.send(new ServerTask(server.getTicks() - 3, runnable));
    }
}
