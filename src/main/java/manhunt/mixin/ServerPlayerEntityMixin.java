package manhunt.mixin;

import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static manhunt.ManhuntMod.*;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Final
    @Shadow
    public MinecraftServer server;

    private long lastDelay = System.currentTimeMillis();
    private long rate = 1000;
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (getGameState() == GameState.PLAYING) {
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
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.tracker\",\"italic\": false,\"color\": \"light_purple\"}");

                    ItemStack tracker = new ItemStack(Items.COMPASS);
                    tracker.setNbt(nbt);
                    tracker.addEnchantment(Enchantments.VANISHING_CURSE, 1);

                    player.giveItemStack(tracker);
                } else if (!config.isTrackerCompass() && System.currentTimeMillis() - lastDelay > rate) {
                    for (ItemStack itemStack : player.getInventory().main) {
                        if (itemStack.getItem().equals(Items.COMPASS) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker")) {
                            if (!itemStack.getNbt().contains("Info")) {
                                itemStack.getNbt().put("Info", new NbtCompound());
                            }

                            NbtCompound info = itemStack.getNbt().getCompound("Info");

                            if (!info.contains("Name", NbtElement.STRING_TYPE) && !getAllRunners().isEmpty()) {
                                info.putString("Name", getAllRunners().get(0).getName().getString());
                            }

                            ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(itemStack.getNbt().getCompound("Info").getString("Name"));

                            if (trackedPlayer != null) {
                                if (player.distanceTo(trackedPlayer) <= 128) {
                                    rate = 750;
                                } else if (player.distanceTo(trackedPlayer) <= 512) {
                                    rate = 1500;
                                } else if (player.distanceTo(trackedPlayer) <= 1024) {
                                    rate = 3000;
                                } else {
                                    rate = 6000;
                                }

                                updateCompass(server.getPlayerManager().getPlayer(player.getName().getString()), itemStack.getNbt(), trackedPlayer);
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
            if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                isRunner.put(player, false);
                if (player.getScoreboard().getTeam("runners").getPlayerList().size() <= 1) {
                    ManhuntMod.setGameState(GameState.POSTGAME);
                    LOGGER.info("Seed: " + player.getServer().getWorld(overworldKey).getSeed());

                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        ManhuntGame.updateGameMode(serverPlayer);
                        if (gameTitles.get(serverPlayer)) {
                            serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.hunterswon").formatted(Formatting.RED)));
                            serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.runner").formatted(Formatting.DARK_RED)));
                        }

                        if (manhuntSounds.get(serverPlayer)) {
                            serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, serverPlayer.getPos().getX(), serverPlayer.getPos().getY(), serverPlayer.getPos().getZ(), 0.5F, 2.0F, serverPlayer.getWorld().random.nextLong()));
                        }
                    }
                }
            }
        }
    }

    private static boolean hasTracker(ServerPlayerEntity player) {
        boolean n = false;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem().equals(Items.COMPASS) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker")) {
                n = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean("Tracker")) {
            n = true;
        } else if (player.getOffHandStack().getNbt() != null && player.getOffHandStack().getNbt().getBoolean("Tracker")) {
            n = true;
        }
        return n;
    }

    private static void updateCompass(ServerPlayerEntity player, NbtCompound nbt, ServerPlayerEntity trackedPlayer) {
        nbt.remove("LodestonePos");
        nbt.remove("LodestoneDimension");

        nbt.put("Info", new NbtCompound());
        if (trackedPlayer.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
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
