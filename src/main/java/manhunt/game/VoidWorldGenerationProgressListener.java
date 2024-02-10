package manhunt.game;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

// Thanks to https://github.com/sakurawald/fuji-fabric

public class VoidWorldGenerationProgressListener implements WorldGenerationProgressListener {
    public static final VoidWorldGenerationProgressListener INSTANCE = new VoidWorldGenerationProgressListener();

    @Override
    public void start(ChunkPos spawnPos) {

    }

    @Override
    public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}