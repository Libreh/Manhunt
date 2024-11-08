package libreh.manhunt.mixin.world;

import libreh.manhunt.ManhuntMod;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractFireBlock.class)
public class LightNetherPortalMixin {
    @Redirect(method = "isOverworldOrNether", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;" +
            "OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private static RegistryKey<World> redirectOverworld() {
        return ManhuntMod.getOverworld().getRegistryKey();
    }

    @Redirect(method = "isOverworldOrNether", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;" +
            "NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private static RegistryKey<World> redirectNether() {
        return ManhuntMod.getTheNether().getRegistryKey();
    }
}