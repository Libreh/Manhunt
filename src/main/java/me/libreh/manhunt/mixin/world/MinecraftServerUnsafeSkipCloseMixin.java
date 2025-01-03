package me.libreh.manhunt.mixin.world;

import me.libreh.manhunt.utils.Methods;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerUnsafeSkipCloseMixin {
    @Unique
    private final Logger log = LoggerFactory.getLogger(MinecraftServerUnsafeSkipCloseMixin.class);

    @Inject(at = @At(value = "HEAD"), method = "shutdown", cancellable = true)
    public void shutdownUnsafe(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;

        try {
            // tell Fabric to invoke SERVER_STOPPED (otherwise this would not reach its mixin)
            ServerLifecycleEvents.SERVER_STOPPED.invoker().onServerStopped(server);
        } catch (Throwable e) {
            log.error("Error on SERVER_STOPPED", e);
        }

        // finally, delete the world files before restarting
        Methods.deleteWorld();

        // immediately halt the JVM (like System.exit(0) but *worse!*)
        Runtime.getRuntime().halt(0);

        ci.cancel();
    }
}