package manhunt.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// Thanks to https://github.com/Tater-Certified/CMP

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {

    /**
     * @author QPCrummer
     * @reason Redirect to use a Partition's Nether
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