package manhunt.mixin;

import manhunt.ManhuntMod;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectOverworld() {
        return ManhuntMod.getOverworld().getRegistryKey();
    }

    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;END:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectEnd() {
        return ManhuntMod.getTheEnd().getRegistryKey();
    }
}