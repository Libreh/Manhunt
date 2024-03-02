package manhunt.mixin;

import eu.pb4.playerdata.api.PlayerDataApi;
import manhunt.config.ManhuntConfig;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.config.ManhuntConfig.AUTO_RESET;
import static manhunt.config.ManhuntConfig.RESET_SECONDS;
import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.POSTGAME;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Final
    @Shadow
    public MinecraftServer server;

    private long lastDelay = System.currentTimeMillis();
    private ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (gameState == PLAYING) {
            if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                if (!hasTracker(server.getPlayerManager().getPlayer(player.getName().getString()))) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Tracker", true);
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("LodestoneTracked", false);
                    nbt.putString("LodestoneDimension", "manhunt:overworld");
                    nbt.putInt("HideFlags", 1);
                    nbt.put("Info", new NbtCompound());
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Tracker\",\"italic\": false,\"color\": \"light_purple\"}");

                    ItemStack tracker = new ItemStack(Items.COMPASS);
                    tracker.setNbt(nbt);
                    tracker.addEnchantment(Enchantments.VANISHING_CURSE, 1);

                    player.giveItemStack(tracker);
                } else if (!Boolean.getBoolean(String.valueOf(ManhuntConfig.MANUAL_COMPASS_UPDATE.get())) && System.currentTimeMillis() - lastDelay > ((long) 1000)) {
                    for (ItemStack item : player.getInventory().main) {
                        if (item.getItem().equals(Items.COMPASS) && item.getNbt() != null && item.getNbt().getBoolean("Tracker")) {
                            if (!item.getNbt().contains("Info")) {
                                item.getNbt().put("Info", new NbtCompound());
                            }

                            NbtCompound info = item.getNbt().getCompound("Info");

                            if (!info.contains("Name", NbtElement.STRING_TYPE) && !allRunners.isEmpty()) {
                                info.putString("Name", allRunners.get(0).getName().getString());
                            }

                            ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(item.getNbt().getCompound("Info").getString("Name"));

                            if (trackedPlayer != null) {
                                updateCompass(server.getPlayerManager().getPlayer(player.getName().getString()), item.getNbt(), trackedPlayer);
                            }
                        }
                    }
                    lastDelay = System.currentTimeMillis();
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {

        if (player.getScoreboardTeam() != null) {
            if (player.getScoreboardTeam().isEqual(player.getScoreboard().getTeam("runners")) && player.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
                manhuntState(POSTGAME, server);
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    updateGameMode(player);
                    if (PlayerDataApi.getGlobalDataFor(serverPlayer, showWinnerTitlePreference).equals(NbtByte.ONE)) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.hunterswon").formatted(Formatting.RED)));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.runnersdied").formatted(Formatting.DARK_RED)));
                        if (!PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference).equals(NbtInt.of(0))) {
                            float volume = (float) Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference))) / 100;
                            if (volume >= 0.2f) {
                                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, volume / 2, 0.5f);
                            }
                        }
                    }
                    if (PlayerDataApi.getGlobalDataFor(serverPlayer, showDurationAtEndPreference).equals(NbtByte.ONE)) {
                        String hoursString;
                        int hours = (int) Math.floor((double) player.getWorld().getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        String minutesString;
                        int minutes = (int) Math.floor((double) player.getWorld().getTime() % (20 * 60 * 60) / (20 * 60));
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String secondsString;
                        int seconds = (int) Math.floor((double) player.getWorld().getTime() % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }
                        serverPlayer.sendMessage(Text.translatable("manhunt.chat.duration", hoursString, minutesString, secondsString));
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

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;", opcode = Opcodes.GETSTATIC))
    private RegistryKey<World> redirectSpawnpointDimension() {
        return overworldRegistryKey;
    }

    private static boolean hasTracker(ServerPlayerEntity player) {
        boolean bool = false;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem().equals(Items.COMPASS) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker")) {
                bool = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean("Tracker")) {
            bool = true;
        } else if (player.getOffHandStack().getNbt() != null && player.getOffHandStack().getNbt().getBoolean("Tracker")) {
            bool = true;
        }
        return bool;
    }

    private static void updateCompass(ServerPlayerEntity player, NbtCompound nbt, ServerPlayerEntity trackedPlayer) {
        nbt.remove("LodestonePos");
        nbt.remove("LodestoneDimension");

        nbt.put("Info", new NbtCompound());
        if (trackedPlayer.getScoreboardTeam() != null && Objects.equals(trackedPlayer.getScoreboardTeam().getName(), "runners")) {
            NbtCompound playerTag = trackedPlayer.writeNbt(new NbtCompound());
            NbtList positions = playerTag.getList("Positions", 10);
            int i;
            for (i = 0; i < positions.size(); ++i) {
                NbtCompound compound = positions.getCompound(i);
                if (Objects.equals(compound.getString("LodestoneDimension"), player.writeNbt(new NbtCompound()).getString("Dimension"))) {
                    nbt.copyFrom(compound);
                    break;
                }
            }

            NbtCompound info = nbt.getCompound("Info");
            info.putLong("LastUpdateTime", player.getWorld().getTime());
            info.putString("Name", trackedPlayer.getName().getString());
            info.putString("Dimension", playerTag.getString("Dimension"));
        }
    }
}
