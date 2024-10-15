package manhunt.mixin.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Recorder;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static manhunt.ManhuntMod.GAME_DIR;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerUnsafeSkipCloseMixin {
    @Shadow
    private boolean saving;

    @Shadow
    @Final
    private ServerNetworkIo networkIo;

    @Shadow
    private PlayerManager playerManager;

    @Shadow
    public Iterable<ServerWorld> getWorlds() {
        throw new IllegalStateException();
    }

    @Shadow
    public ResourceManager getResourceManager() {
        throw new IllegalStateException();
    }

    @Shadow
    @Final
    protected LevelStorage.Session session;

    @Shadow
    private Recorder recorder;

    @Shadow
    public abstract void forceStopRecorder();

    @Inject(at = @At(value = "HEAD"), method = "shutdown", cancellable = true)
    public void shutdown(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        try {
            // tell Fabric to invoke SERVER_STOPPING, which *might* not be the first mixin
            ServerLifecycleEvents.SERVER_STOPPING.invoker().onServerStopping(server);
        } catch (Throwable ignored) {
        }

        // many of the shutdown tasks can be skipped, since we don't care if all the world data is saved

        if (this.recorder != null && this.recorder.isActive()) {
            this.forceStopRecorder();
        }

        if (this.networkIo != null) {
            this.networkIo.stop();
        }

        this.saving = true;

        if (this.playerManager != null) {
            try {
                this.playerManager.disconnectAllPlayers();
            } catch (Throwable ignored) {
            }
        }

        this.saving = false;

        try {
            // tell Fabric to invoke SERVER_STOPPED (otherwise this would not reach its mixin)
            ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(server);
        } catch (Throwable ignored) {
        }

        // finally, delete the world files before restarting
        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("world").toFile());
        } catch (IOException ignored) {
        }

        // immediately halt the JVM (like System.exit(0) but *worse!*)
        Runtime.getRuntime().halt(0);
        ci.cancel();

        for (ServerWorld world : this.getWorlds()) {
            if (world == null) continue;
            try {
                world.close();
            } catch (IOException ignored) {
            }
        }

        this.saving = false;

        var resourceManager = this.getResourceManager();
        if (resourceManager instanceof LifecycledResourceManager) ((LifecycledResourceManager) resourceManager).close();

        try {
            this.session.close();
        } catch (IOException ignored) {
        }

        try {
            // tell Fabric to invoke SERVER_STOPPED (otherwise this would not reach its mixin)
            ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(server);
        } catch (Throwable ignored) {
        }

        // finally, delete the world files before restarting
        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("world").toFile());
        } catch (IOException ignored) {
        }
        ci.cancel();
    }

}
