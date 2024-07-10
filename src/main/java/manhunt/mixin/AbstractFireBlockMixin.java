package manhunt.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static manhunt.ManhuntMod.getOverworld;
import static manhunt.ManhuntMod.getTheNether;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    @Redirect(method = "isOverworldOrNether", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private static RegistryKey<World> redirectOverworld() {
        return getOverworld().getRegistryKey();
    }

    @Redirect(method = "isOverworldOrNether", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private static RegistryKey<World> redirectNether() {
        return getTheNether().getRegistryKey();
    }
}