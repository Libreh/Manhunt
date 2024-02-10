package manhunt.mixin;

import manhunt.game.DimensionOptionsInterface;
import manhunt.game.FilteredRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

// Thanks to https://github.com/sakurawald/fuji-fabric

@Mixin(DimensionOptionsRegistryHolder.class)
public class DimensionOptionsRegistryHolderMixin {
    @ModifyArg(method = "method_45516", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/MapCodec;forGetter(Ljava/util/function/Function;)Lcom/mojang/serialization/codecs/RecordCodecBuilder;"))
    private static Function<Object, Registry<DimensionOptions>> manhunt$swapRegistryGetter(Function<Object, Registry<DimensionOptions>> getter) {
        return (x) -> new FilteredRegistry<>(getter.apply(x), DimensionOptionsInterface.SAVE_PROPERTIES_PREDICATE);
    }
}