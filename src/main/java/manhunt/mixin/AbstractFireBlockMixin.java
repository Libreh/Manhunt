package manhunt.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    /**
     * @author Libreh
     * @reason Redirect to use Manhunt world's Nether
     */
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        if (world.getRegistryKey().getValue().getNamespace().equals("manhunt")) {
            return world.getDimensionKey() == DimensionTypes.OVERWORLD || world.getDimensionKey() == DimensionTypes.THE_NETHER;
        } else {
            return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
        }
    }
}