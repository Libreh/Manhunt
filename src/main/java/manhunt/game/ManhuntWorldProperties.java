package manhunt.game;

import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;

// Thanks to https://github.com/sakurawald/fuji-fabric.

/**
 * The only purpose of this class is to warp the seed.
 **/
@SuppressWarnings("LombokGetterMayBeUsed")
public final class ManhuntWorldProperties extends UnmodifiableLevelProperties {

    private final long seed;

    public ManhuntWorldProperties(SaveProperties saveProperties, long seed) {
        super(saveProperties, saveProperties.getMainWorldProperties());
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }
}