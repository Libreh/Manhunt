package manhunt.mixin;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.config.ManhuntConfig.*;
import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.POSTGAME;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated.

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.getHealth() == 1.0F) {
            MinecraftServer server = dragon.getServer();

            manhuntState(POSTGAME, server);
            dragon.setHealth(0.0F);
            if (Boolean.parseBoolean(CHANGEABLE_PREFERENCES.get())) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (PlayerDataApi.getGlobalDataFor(player, showWinnerTitle) == NbtByte.ONE) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.runnerswon").formatted(Formatting.GREEN)));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.dragondied").formatted(Formatting.DARK_GREEN)));
                        if (!PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolume).equals(NbtInt.of(0))) {
                            float volume = (float) Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolume))) / 100;
                            if (volume >= 0.2f) {
                                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, volume / 2, 2f);
                            }
                        }
                    }
                    if (PlayerDataApi.getGlobalDataFor(player, showDurationAtEnd) == NbtByte.ONE) {
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
                        previousDuration = hoursString + ":" + minutesString + ":" + secondsString;
                        MutableText duration = Texts.bracketedCopyable(previousDuration);
                        player.sendMessage(Text.translatable("manhunt.chat.show", Text.translatable("manhunt.duration"), duration), false);
                    }
                    if (PlayerDataApi.getGlobalDataFor(player, showSeedAtEnd) == (NbtByte.ONE)) {
                        previousSeed = String.valueOf(player.getServerWorld().getSeed());
                        MutableText seed = Texts.bracketedCopyable(previousSeed);
                        player.sendMessage(Text.translatable("manhunt.chat.show", Text.translatable("manhunt.seed"), seed));
                    }
                }
            } else {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (Boolean.parseBoolean(SHOW_WINNER_TITLE.get())) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.runnerswon").formatted(Formatting.GREEN)));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.dragondied").formatted(Formatting.DARK_GREEN)));
                        if (!PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolume).equals(NbtInt.of(0))) {
                            float volume = (float) Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolume))) / 100;
                            if (volume >= 0.2f) {
                                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, volume / 2, 2f);
                            }
                        }
                    }
                    if (Boolean.parseBoolean(SHOW_DURATION_AT_END.get())) {
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
                        player.sendMessage(Text.translatable("manhunt.chat.duration", hoursString, minutesString, secondsString));
                    }
                    if (Boolean.parseBoolean(SHOW_SEED_AT_END.get())) {
                        player.sendMessage(Text.translatable("manhunt.chat.seed", player.getServerWorld().getSeed()));
                    }
                }
            }

            if (Boolean.parseBoolean(AUTO_RESET.get())) {
                server.getPlayerManager().broadcast(Text.translatable("manhunt.chat.willreset", Text.literal(String.valueOf(Integer.parseInt(RESET_SECONDS.get())))).formatted(Formatting.RED), false);

                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(() -> resetGameIfAuto(server), Integer.parseInt(RESET_SECONDS.get()), TimeUnit.SECONDS);
            }
        }
    }
}