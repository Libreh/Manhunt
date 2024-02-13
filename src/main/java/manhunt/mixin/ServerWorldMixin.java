package manhunt.mixin;

import manhunt.Manhunt;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Redirect(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    private void dontSendWeatherPacketsToAllWorlds(PlayerManager instance, Packet<?> packet) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in
        // the rain effect will remain until the player changes dimension or reconnects.
        instance.sendToDimension(packet, this.getChunkManager().getWorld().getRegistryKey());
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    private void redirectWorld(ServerWorld world, long l) {
        MinecraftServer server = Manhunt.SERVER;
        l = world.getLevelProperties().getTimeOfDay() + 24000L;
        server.getOverworld().setTimeOfDay(l - l % 24000L);
    }
}