package manhunt.mixin;

import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Final
    @Shadow
    public MinecraftServer server;
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    private long lastDelay = System.currentTimeMillis();
    private static boolean holding;
    private ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters")) && player.isAlive()) {
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
                } else if (ManhuntGame.settings.compassUpdate && System.currentTimeMillis() - lastDelay > ((long) 500)) {
                    for (ItemStack item : player.getInventory().main) {
                        if (item.getItem().equals(Items.COMPASS) && item.getNbt() != null && item.getNbt().getBoolean("Tracker")) {
                            if (!item.getNbt().contains("Info")) {
                                item.getNbt().put("Info", new NbtCompound());
                            }

                            NbtCompound info = item.getNbt().getCompound("Info");

                            if (!info.contains("Name", NbtElement.STRING_TYPE) && !ManhuntGame.allRunners.isEmpty()) {
                                info.putString("Name", ManhuntGame.allRunners.get(0).getName().getString());
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
        Scoreboard scoreboard = server.getScoreboard();

        if (player.getScoreboardTeam() != null) {
            if (player.getScoreboardTeam().isEqual(scoreboard.getTeam("runners"))) {
                if (ManhuntGame.settings.winnerTitle && server.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
                    ManhuntGame.manhuntState(ManhuntState.POSTGAME, server);
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ManhuntGame.updateGameMode(player);
                        MessageUtil.showTitle(player, "manhunt.title.hunters", "manhunt.title.dead");
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.1f, 0.5f);
                    }
                }
            }
        }
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
