package manhunt.mixin;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static manhunt.ManhuntMod.endWorld;

@Mixin(EndGatewayBlockEntity.class)
public class EndGatewayBlockEntityMixin {
    @Redirect(method = "getOrCreateExitPortalPos", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;END:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectEnd() {
        return endWorld;
    }
}
