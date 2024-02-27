package manhunt.mixin;

import eu.pb4.playerdata.api.PlayerDataApi;
import manhunt.util.MessageUtil;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.POSTGAME;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated.

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.getHealth() == 1.0F) {
            manhuntState(POSTGAME, dragon.getServer());
            dragon.setHealth(0.0F);
            for (ServerPlayerEntity player : dragon.getServer().getPlayerManager().getPlayerList()) {
                if (PlayerDataApi.getGlobalDataFor(player, winnerTitlePreference).equals(NbtByte.ONE)) {
                    MessageUtil.showTitle(player, "manhunt.title.runners", "manhunt.title.dragon");
                    if (!PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference).equals(NbtInt.of(0))) {
                        float volume = (float) Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference))) / 100;
                        if (volume >= 0.2f) {
                            player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, volume / 2, 2f);
                        }
                    }
                }
                if (PlayerDataApi.getGlobalDataFor(player, durationAtEndPreference).equals(NbtByte.ONE)) {
                    String hoursString;
                    int hours = (int) Math.floor((double) dragon.getWorld().getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                    if (hours <= 9) {
                        hoursString = "0" + hours;
                    } else {
                        hoursString = String.valueOf(hours);
                    }
                    String minutesString;
                    int minutes = (int) Math.floor((double) dragon.getWorld().getTime() % (20 * 60 * 60) / (20 * 60));
                    if (minutes <= 9) {
                        minutesString = "0" + minutes;
                    } else {
                        minutesString = String.valueOf(minutes);
                    }
                    String secondsString;
                    int seconds = (int) Math.floor((double) dragon.getWorld().getTime() % (20 * 60) / (20));
                    if (seconds <= 9) {
                        secondsString = "0" + seconds;
                    } else {
                        secondsString = String.valueOf(seconds);
                    }
                    MessageUtil.sendMessage(player, "manhunt.chat.duration", hoursString, minutesString, secondsString);
                }
            }
        }
    }
}