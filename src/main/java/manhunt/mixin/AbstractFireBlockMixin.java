package manhunt.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static manhunt.ManhuntMod.*;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    /**
     * @author Libreh
     * @reason Use Manhunt world's nether
     */
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        if (world.getRegistryKey().getValue().getNamespace().equals(MOD_ID)) {
            return world == overworld || world == nether;
        } else {
            return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
        }
    }
}