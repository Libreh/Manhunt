package libreh.manhunt.mixin.world;

import libreh.manhunt.ManhuntMod;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;" +
            "OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectOverworld() {
        return ManhuntMod.getOverworld().getRegistryKey();
    }

    @Redirect(method = "createTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;" +
            "NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectNether() {
        return ManhuntMod.getTheNether().getRegistryKey();
    }
}
