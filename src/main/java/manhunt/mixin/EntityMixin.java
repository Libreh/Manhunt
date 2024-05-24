package manhunt.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static manhunt.ManhuntMod.nether;
import static manhunt.ManhuntMod.overworld;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    protected abstract Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder);

    @Shadow
    protected abstract Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect);

    @Shadow
    public abstract Vec3d getVelocity();

    @Redirect(method = "tickPortal", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectPortalOverworldRegistryKey() {
        return overworld.getRegistryKey();
    }

    @Redirect(method = "tickPortal", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectPortalNetherRegistryKey() {
        return nether.getRegistryKey();
    }

    @Redirect(method = "getTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectTeleportOverworldRegistryKey() {
        return overworld.getRegistryKey();
    }

    @Redirect(method = "getTeleportTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;NETHER:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectTeleportNetherRegistryKey() {
        return nether.getRegistryKey();
    }

    @Inject(method = "tickPortal()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;moveToWorld(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    private void giveAdvancement(CallbackInfo ci) {
        Entity entity = (Entity) (Object)this;
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).getAdvancementTracker().grantCriterion(entity.getServer().getAdvancementLoader().get(new Identifier("minecraft:story/enter_the_nether")), "entered_nether");
        }
    }
}