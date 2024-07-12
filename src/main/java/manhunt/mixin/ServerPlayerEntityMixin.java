package manhunt.mixin;

import com.mojang.authlib.GameProfile;
import manhunt.game.GameState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static manhunt.ManhuntMod.*;
import static manhunt.game.ManhuntGame.gameOver;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Final
    @Shadow
    public MinecraftServer server;
    @Unique
    private long lastDelay = System.currentTimeMillis();
    @Unique
    private long rate = 1000;
    @Unique
    private int count;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        count++;
        if (count == 39) {
            if (slowDownManager.get(this.getUuid()) != 0) {
                if (slowDownManager.get(this.getUuid()) >= 4) {
                    slowDownManager.put(this.getUuid(), slowDownManager.get(this.getUuid()) - 4);
                } else {
                    slowDownManager.put(this.getUuid(), 0);
                }
            }
            count = 0;
        }

        if (state == GameState.PLAYING) {
            if (this.isTeamPlayer(this.getScoreboard().getTeam("hunters"))) {
                if (!hasTracker((ServerPlayerEntity) (Object)this)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Tracker", true);
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.COMPASS);
                    stack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.manhunt.tracker"));
                    stack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(getOverworld().getRegistryKey(), new BlockPos(0, 0, 0))), false));
                    stack.addEnchantment(server.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), 1);
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    this.giveItemStack(stack);
                } else {
                    if (config.isAutomaticCompass() && System.currentTimeMillis() - lastDelay > rate) {
                        for (ItemStack stack : this.getInventory().main) {
                            if (stack.getItem().equals(Items.COMPASS) && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
                                if (!stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().contains("Name") && !allRunners.isEmpty()) {
                                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                                    nbt.putString("Name", allRunners.get(0).getName().getString());
                                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                                }

                                ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));

                                if (trackedPlayer != null) {
                                    if (this.distanceTo(trackedPlayer) <= 128) {
                                        rate = 750;
                                    } else if (this.distanceTo(trackedPlayer) <= 512) {
                                        rate = 1500;
                                    } else if (this.distanceTo(trackedPlayer) <= 1024) {
                                        rate = 3000;
                                    } else {
                                        rate = 6000;
                                    }

                                    updateCompass((ServerPlayerEntity) (Object)this, stack, trackedPlayer);
                                }
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
        if (this.isTeamPlayer(this.getScoreboard().getTeam("runners"))) {
            if (server.getScoreboard().getTeam("runners").getPlayerList().size() == 1 && state == GameState.PLAYING) {
                gameOver(server, true);
            }
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void pvpMixin(PlayerEntity attacker, CallbackInfoReturnable<Boolean> ci) {
        if (state == GameState.PREGAME || paused) {
            ci.setReturnValue(false);
        } else {
            if (this.isTeamPlayer(attacker.getScoreboardTeam()) && config.getFriendlyFire() != 1) {
                if (config.getFriendlyFire() == 2) {
                    if (!friendlyFire.get(this.getUuid()) || !friendlyFire.get(attacker.getUuid())) {
                        ci.setReturnValue(false);
                        attacker.sendMessage(Text.translatable("chat.friendly_fire.per_player").formatted(Formatting.RED));
                    }
                } else if (config.getFriendlyFire() == 3) {
                    attacker.sendMessage(Text.translatable("chat.friendly_fire").formatted(Formatting.RED));
                    ci.setReturnValue(false);
                }
            }
        }
    }

    private void updateCompass(ServerPlayerEntity player, ItemStack stack, ServerPlayerEntity trackedPlayer) {
        NbtCompound thisTag = trackedPlayer.writeNbt(new NbtCompound());
        NbtList positions = thisTag.getList("Positions", 10);
        int i;
        for (i = 0; i < positions.size(); ++i) {
            NbtCompound compound = positions.getCompound(i);
            if (compound.getString("LodestoneDimension").equals(player.writeNbt(new NbtCompound()).getString("Dimension"))) {
                NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().copyFrom(compound);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                break;
            }
        }

        int[] is = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getIntArray("LodestonePos");

        BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(this.getWorld().getRegistryKey(), blockPos)), false));
    }

    private static boolean hasTracker(ServerPlayerEntity player) {
        boolean tracker = false;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.COMPASS && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
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
}
