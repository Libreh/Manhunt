package me.libreh.manhunt.mixin.world;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkLoadingManager.class)
public interface ServerChunkLoadingManagerAccessor {
    @Invoker("getChunkHolder")
    ChunkHolder invokeGetChunkHolder(long pos);
}
