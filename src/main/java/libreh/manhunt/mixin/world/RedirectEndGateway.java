package libreh.manhunt.mixin.world;

import libreh.manhunt.ManhuntMod;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndGatewayBlockEntity.class)
public class RedirectEndGateway {
    @Redirect(method = "getOrCreateExitPortalPos", at = @At(value = "FIELD", target =
            "Lnet/minecraft/world/World;" + "END:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectEnd() {
        return ManhuntMod.getTheEnd().getRegistryKey();
    }
}
