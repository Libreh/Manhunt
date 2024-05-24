package manhunt.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    private void redirectWorld(ServerWorld world, long l) {
        MinecraftServer server = world.getServer();
        l = world.getLevelProperties().getTimeOfDay() + 24000L;
        server.getOverworld().setTimeOfDay(l - l % 24000L);
    }
}