package manhunt.mixin;

import manhunt.world.interfaces.DimensionOptionsInterface;
import net.minecraft.world.dimension.DimensionOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DimensionOptions.class)
public class DimensionOptionsMixin implements DimensionOptionsInterface {

    @Unique
    private boolean manhunt$setSaveProperties = true;

    @Unique
    public void manhunt$setSaveProperties(boolean value) {
        this.manhunt$setSaveProperties = value;
    }

    @Unique
    public boolean manhunt$getSaveProperties() {
        return this.manhunt$setSaveProperties;
    }
}