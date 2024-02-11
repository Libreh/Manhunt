package manhunt.mixin;

import com.mojang.authlib.GameProfile;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Final
    @Shadow
    public MinecraftServer server;
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    private long lastDelay = System.currentTimeMillis();
    private static boolean holding;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            if (this.isTeamPlayer(server.getScoreboard().getTeam("hunters")) && this.isAlive()) {
                if (!hasTracker(server.getPlayerManager().getPlayer(this.getName().getString()))) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Tracker", true);
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("LodestoneTracked", false);
                    nbt.putString("LodestoneDimension", "manhunt:overworld");
                    nbt.putInt("HideFlags", 1);
                    nbt.put("Info", new NbtCompound());
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Tracker\",\"italic\": false,\"color\": \"light_purple\"}");

                    ItemStack stack = new ItemStack(Items.COMPASS);
                    stack.setNbt(nbt);
                    stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

                    this.giveItemStack(stack);

                    NbtCompound info = stack.getNbt().getCompound("Info");

                    if (!info.contains("Name", NbtElement.STRING_TYPE) && !ManhuntGame.allRunners.isEmpty()) {
                        info.putString("Name", ManhuntGame.allRunners.get(0).getName().getString());
                    }

                    ServerPlayerEntity trackedPlayer = this.getServer().getPlayerManager().getPlayer(info.getString("Name"));

                    updateCompass((ServerPlayerEntity) (Object)this, stack.getNbt(), trackedPlayer);
                } else if (ManhuntGame.settings.compassUpdate && System.currentTimeMillis() - lastDelay > ((long) 500)) {
                    for (ItemStack item : this.getInventory().main) {
                        if (item.getItem().equals(Items.COMPASS) && item.getNbt() != null && item.getNbt().getBoolean("Tracker")) {
                            ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(item.getNbt().getCompound("Info").getString("Name"));
                            if (trackedPlayer != null) {
                                updateCompass(server.getPlayerManager().getPlayer(this.getName().getString()), item.getNbt(), trackedPlayer);
                            }
                        }
                    }
                    lastDelay = System.currentTimeMillis();
                }


                if (holdingTracker(server.getPlayerManager().getPlayer(this.getName().getString()))) {
                    holding = true;
                    if (this.getMainHandStack().getNbt() != null && this.getMainHandStack().getNbt().getBoolean("Tracker")) {
                        NbtCompound info = this.getMainHandStack().getNbt().getCompound("Info");
                        if (server.getPlayerManager().getPlayer(info.getString("Name")) != null) {
                            showInfo(info);
                        }
                    } else if (this.getOffHandStack().getNbt() != null) {
                        NbtCompound info = this.getOffHandStack().getNbt().getCompound("Info");
                        if (server.getPlayerManager().getPlayer(info.getString("Name")) != null) {
                            showInfo(info);
                        }
                    }
                } else {
                    if (holding) {
                        this.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of("")));
                        holding = false;
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        Scoreboard scoreboard = server.getScoreboard();

        if (this.getScoreboardTeam() != null) {
            if (this.getScoreboardTeam().isEqual(scoreboard.getTeam("runners"))) {

                scoreboard.clearTeam(this.getName().getString());

                scoreboard.addScoreHolderToTeam(this.getName().getString(), scoreboard.getTeam("hunters"));

                if (ManhuntGame.settings.gameTitles && server.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                    ManhuntGame.gameState = ManhuntState.POSTGAME;
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ManhuntGame.updateGameMode(player);
                        MessageUtil.showTitle(player, "manhunt.title.hunters", "manhunt.title.dead");
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.1f, 0.5f);
                    }
                }
            }
        }
    }

    private void showInfo(NbtCompound info) {
        String dim = info.getString("Dimension");
        String dimension = "";
        if (!info.contains("Dimension")) {
            dimension = "manhunt.scoreboard.world.unknown";
        } else if (Objects.equals(dim, "manhunt:overworld")) {
            dimension = "manhunt.scoreboard.world.overworld";
        } else if (Objects.equals(dim, "manhunt:the_nether")) {
            dimension = "manhunt.scoreboard.world.the_nether";
        } else if (Objects.equals(dim, "manhunt:the_end")) {
            dimension = "manhunt.scoreboard.world.the_end";
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

    private static boolean holdingTracker(ServerPlayerEntity player) {
        boolean bool = false;
        if (player.getMainHandStack().getNbt() != null && player.getMainHandStack().getNbt().getBoolean("Tracker") && player.getMainHandStack().getNbt().getCompound("Info").contains("Name")) {
            bool = true;
        } else if (player.getOffHandStack().getNbt() != null && player.getOffHandStack().getNbt().getBoolean("Tracker") && player.getOffHandStack().getNbt().getCompound("Info").contains("Name")) {
            bool = true;
        }
        return bool;
    }
}
