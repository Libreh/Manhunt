package manhunt.mixin;

import manhunt.Manhunt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

import static manhunt.game.ManhuntGame.isPaused;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void $init(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Manhunt.LOGGER.debug("MinecraftServerMixin: $init: " + server);
        Manhunt.SERVER = server;
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"), cancellable = true)
    private void beforeTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;

        if (!isPaused()) {
            return;
        }

        ci.cancel();

        if (server instanceof MinecraftDedicatedServer) {
            MinecraftDedicatedServer dedicatedServer = (MinecraftDedicatedServer) server;
            dedicatedServer.executeQueuedCommands();
        }
    }
}
