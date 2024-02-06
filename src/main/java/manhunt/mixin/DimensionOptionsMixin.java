package manhunt.mixin;

import manhunt.game.DimensionOptionsInterface;
import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Thanks to https://github.com/sakurawald/fuji-fabric.

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements DimensionOptionsInterface {

    @Unique
    private boolean manhunt$saveProperties = true;

    @Unique
    public void manhunt$saveProperties(boolean value) {
        this.manhunt$saveProperties = value;
    }

    @Unique
    public boolean manhunt$saveProperties() {
        return this.manhunt$saveProperties;
    }
}