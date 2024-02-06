package manhunt.game;

import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

// Thanks to https://github.com/sakurawald/fuji-fabric.

@ApiStatus.Internal
public interface DimensionOptionsInterface {
    Predicate<DimensionOptions> SAVE_PROPERTIES_PREDICATE = (e) -> ((DimensionOptionsInterface) (Object) e).manhunt$saveProperties();

    void manhunt$saveProperties(boolean value);

    boolean manhunt$saveProperties();
}