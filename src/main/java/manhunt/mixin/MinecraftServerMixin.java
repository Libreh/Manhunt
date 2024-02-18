package manhunt.mixin;

import manhunt.Manhunt;
import manhunt.game.SafeIterator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BooleanSupplier;

import static manhunt.game.ManhuntGame.isPaused;

// Thanks to https://github.com/sakurawald/fuji-fabric

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void manhunt$init(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        Manhunt.LOGGER.debug("MinecraftServerMixin: $init: " + server);
        Manhunt.SERVER = server;
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"), cancellable = true)
    private void manhunt$beforeTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
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

    @Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
    private Iterator<ServerWorld> manhunt$copyBeforeTicking(Iterable<ServerWorld> instance) {
        return new SafeIterator<>((Collection<ServerWorld>) instance);
    }
}