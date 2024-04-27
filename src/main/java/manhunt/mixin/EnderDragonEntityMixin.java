package manhunt.mixin;

import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static manhunt.ManhuntMod.*;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.getHealth() == 1) {
            MinecraftServer server = dragon.getServer();

            ManhuntMod.setGameState(GameState.POSTGAME);
            dragon.setHealth(0);
            LOGGER.info("Seed: " + server.getWorld(overworldKey).getSeed());

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ManhuntGame.updateGameMode(player);

                if (gameTitles.get(player)) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.runnerswon").formatted(Formatting.GREEN)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.enderdragon").formatted(Formatting.DARK_GREEN)));
                }

                if (manhuntSounds.get(player)) {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 2.0F, player.getWorld().random.nextLong()));
                }
            }
        }
    }
}