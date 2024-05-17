package manhunt.mixin;

import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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
    private void tick(CallbackInfo ci) {
        if (getGameState() == GameState.PLAYING) {
            if (player.getScoreboardTeam().getName().equals("hunters")) {
                if (!hasTracker(player)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Tracker", true);
                    nbt.putBoolean("Remove", true);
                    nbt.putString("Name", "");

                    ItemStack trackerStack = new ItemStack(Items.COMPASS);
                    trackerStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("manhunt.tracker"));
                    trackerStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    trackerStack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(overworldWorld.getRegistryKey(), new BlockPos(0, 0, 0))), false));
                    trackerStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    trackerStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.giveItemStack(trackerStack);
                } else {
                    if (config.isAutomaticCompass() && System.currentTimeMillis() - lastDelay > rate && allRunners != null && !allRunners.isEmpty()) {
                        for (ItemStack itemStack : player.getInventory().main) {
                            if (itemStack.getItem().equals(Items.COMPASS) && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null && itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
                                ServerPlayerEntity trackedPlayer = allRunners.get(0);

                                if (!itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name").isEmpty()) {
                                    trackedPlayer = server.getPlayerManager().getPlayer(itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));
                                } else {
                                    NbtCompound nbt = itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                                    nbt.putString("Name", trackedPlayer.getName().getString());
                                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                                }

                                if (player.distanceTo(trackedPlayer) <= 128) {
                                    rate = 750;
                                } else if (player.distanceTo(trackedPlayer) <= 512) {
                                    rate = 1500;
                                } else if (player.distanceTo(trackedPlayer) <= 1024) {
                                    rate = 3000;
                                } else {
                                    rate = 6000;
                                }

                                updateCompass(player, itemStack, trackedPlayer);
                            }
                        }
                        lastDelay = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource source, CallbackInfo ci) {
        if (player.getScoreboardTeam().getName().equals("runners") && player.getScoreboard().getTeam("runners").getPlayerList().size() <= 1) {
            ManhuntGame.endGame(server, true, false);
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void Manhunt$pvpMixin(PlayerEntity attacker, CallbackInfoReturnable<Boolean> ci) {
        if (getGameState() == GameState.PREGAME) {
            ci.setReturnValue(false);
        } else {
            if (player.isTeamPlayer(attacker.getScoreboardTeam())) {
                if (config.getFriendlyFire() == 2) {
                    ci.setReturnValue(false);
                } else if (config.getFriendlyFire() == 1) {
                    if (!friendlyFire.get(player.getUuid()) || !friendlyFire.get(attacker.getUuid())) {
                        ci.setReturnValue(false);
                    }
                }
            }
        }
    }

    private static boolean hasTracker(ServerPlayerEntity player) {
        boolean tracker = false;

        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem() == Items.COMPASS && itemStack.get(DataComponentTypes.CUSTOM_DATA) != null && itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
                tracker = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA) != null && player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
            tracker = true;
        } else if (player.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA) != null && player.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
            tracker = true;
        }

        return tracker;
    }

    private static void updateCompass(ServerPlayerEntity player, ItemStack stack, ServerPlayerEntity trackedPlayer) {
        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(trackedPlayer.getWorld().getRegistryKey(), trackedPlayer.getBlockPos())), false));
    }
}
