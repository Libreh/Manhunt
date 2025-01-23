package me.libreh.manhunt.mixin.game;

import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
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

import static me.libreh.manhunt.utils.Fields.server;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    protected void disableBedExplosions(BlockState state, World world, BlockPos pos, PlayerEntity player,
                                        BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (Config.getConfig().globalPreferences.bedExplosionsPvP.equals("off") || !PlayerData.get(player).bedExplosionsPvP) {
            if (world.getRegistryKey() != World.OVERWORLD) {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                        player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED), false);

                        cir.setReturnValue(ActionResult.FAIL);
                    }
                }
            }
        }
    }
}
