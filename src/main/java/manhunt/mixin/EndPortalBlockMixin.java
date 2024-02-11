package manhunt.mixin;

import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// Thanks to https://github.com/Tater-Certified/Carpet-Sky-Additionals

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    /**
     * @author QPCrummer
     * @reason This will do for now
     */
    @Overwrite
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world instanceof ServerWorld && entity.canUsePortals() && VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ())), state.getOutlineShape(world, pos), BooleanBiFunction.AND)) {
            ServerWorld serverWorld;
            BlockPos blockPos;
            if (world.getRegistryKey() == ManhuntGame.theEndRegistryKey) {
                serverWorld = entity.getServer().getWorld(ManhuntGame.overworldRegistryKey);
                if (entity instanceof ServerPlayerEntity) {
                    blockPos = ((ServerPlayerEntity) entity).getSpawnPointPosition();
                    serverWorld = entity.getServer().getWorld(((ServerPlayerEntity) entity).getSpawnPointDimension());
                    if (blockPos == null) {
                        blockPos = new BlockPos(8, 64, 9);
                    }
                } else {
                    blockPos = ManhuntGame.worldSpawnPos;
                }
            } else {
                serverWorld = entity.getServer().getWorld(ManhuntGame.theEndRegistryKey);
                serverWorld.setSpawnPos(ServerWorld.END_SPAWN_POS, 0);
                ServerWorld.createEndSpawnPlatform(serverWorld);
                blockPos = ServerWorld.END_SPAWN_POS;
            }
            if (entity instanceof ServerPlayerEntity) {
                MinecraftServer server = Manhunt.SERVER;
                server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent().withLevel(2), "advancement grant " + entity.getName().getString() + " only minecraft:story/enter_the_end");
            }
            TeleportTarget teleportTarget = getTeleportTarget(entity, blockPos);
            FabricDimensions.teleport(entity,serverWorld,teleportTarget);
        }
    }

    private TeleportTarget getTeleportTarget(Entity entity, BlockPos teleport_pos) {
        return new TeleportTarget(new Vec3d((double)teleport_pos.getX() + 0.5, teleport_pos.getY(), (double)teleport_pos.getZ() + 0.5), entity.getVelocity(), 90.0F, 0.0F);
    }
}