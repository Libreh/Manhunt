package manhunt.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.nucleoid.fantasy.Fantasy;

import static manhunt.ManhuntMod.nether;
import static manhunt.ManhuntMod.overworld;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    /**
     * @author Libreh
     * @reason Use Manhunt world's nether
     */
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        if (world.getRegistryKey().getValue().getNamespace().equals(Fantasy.ID)) {
            return world == overworld || world == nether;
        } else {
            return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
        }
    }
}