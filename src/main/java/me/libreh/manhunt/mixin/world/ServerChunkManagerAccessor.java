package me.libreh.manhunt.mixin.world;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerAccessor {
    @Invoker("updateChunks")
    boolean invokeUpdateChunks();
}
