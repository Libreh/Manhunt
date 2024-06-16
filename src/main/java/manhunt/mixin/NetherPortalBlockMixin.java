package manhunt.mixin;

import net.minecraft.block.NetherPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static manhunt.ManhuntMod.netherWorld;
import static manhunt.ManhuntMod.overworldWorld;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectOverworld() {
        return overworldWorld;
    }

    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectNether() {
        return netherWorld;
    }
}
