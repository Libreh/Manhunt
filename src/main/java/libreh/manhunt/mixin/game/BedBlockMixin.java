package libreh.manhunt.mixin.game;

import libreh.manhunt.config.ManhuntConfig;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static libreh.manhunt.ManhuntMod.getOverworld;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"))
    protected void disableBedExplosions(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                        BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!ManhuntConfig.CONFIG.isBedExplosions()) {
            if (world.getRegistryKey() != getOverworld().getRegistryKey()) {
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                        player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED), false);
                        cir.setReturnValue(ActionResult.FAIL);
                    }
                }
            }
        }
    }
}
