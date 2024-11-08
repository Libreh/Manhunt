package libreh.manhunt.mixin.game;

import com.mojang.authlib.GameProfile;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.config.gui.ConfigGui;
import libreh.manhunt.config.gui.SettingsGui;
import libreh.manhunt.event.OnGameTick;
import libreh.manhunt.game.GameState;
import libreh.manhunt.game.ManhuntGame;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Final
    @Shadow
    public MinecraftServer server;

    @Shadow
    public abstract void attack(Entity target);

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Shadow
    public abstract PlayerAdvancementTracker getAdvancementTracker();

    @Shadow
    public abstract @Nullable BlockPos getSpawnPointPosition();

    @Unique
    private final ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
    @Unique
    private long lastDelay = System.currentTimeMillis();
    @Unique
    private long rate = 1000;
    @Unique
    private int tickCount;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        tickCount++;
        if (tickCount == 39) {
            if (ConfigGui.SLOW_DOWN_MANAGER.get(this.getUuid()) != 0) {
                if (ConfigGui.SLOW_DOWN_MANAGER.get(this.getUuid()) >= 4) {
                    ConfigGui.SLOW_DOWN_MANAGER.put(this.getUuid(),
                            ConfigGui.SLOW_DOWN_MANAGER.get(this.getUuid()) - 4);
                } else {
                    ConfigGui.SLOW_DOWN_MANAGER.put(this.getUuid(), 0);
                }
            }
            tickCount = 0;
        }

        if (ManhuntMod.gameState != GameState.PREGAME) {
            if (this.isTeamPlayer(this.getScoreboard().getTeam("hunters"))) {
                if (!hasTracker(player)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Tracker", true);
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.COMPASS);
                    stack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.manhunt.tracker"));
                    stack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.LODESTONE_TRACKER,
                            new LodestoneTrackerComponent(Optional.of(GlobalPos.create(ManhuntMod.getOverworld().getRegistryKey(), new BlockPos(0, 0, 0))), false));
                    stack.addEnchantment(server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), 1);
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    this.giveItemStack(stack);
                } else {
                    if (System.currentTimeMillis() - lastDelay > rate && ManhuntConfig.CONFIG.getTrackerType() != 2 && ((ManhuntConfig.CONFIG.getTrackerType() == 3 && holdingTracker()) || ManhuntConfig.CONFIG.getTrackerType() == 4) || SettingsGui.TRACKER_TYPE.get(this.getUuid()) != 2 && ((SettingsGui.TRACKER_TYPE.get(this.getUuid()) == 3 && holdingTracker()) || SettingsGui.TRACKER_TYPE.get(this.getUuid()) == 4)) {
                        removeDuplicateTrackers(player);

                        ItemStack trackerItem = null;

                        for (ItemStack stack : this.getInventory().main) {
                            if (isItemTracker(stack)) {
                                trackerItem = stack;
                                break;
                            }
                        }

                        if (trackerItem == null) {
                            var stack = this.playerScreenHandler.getCursorStack();
                            if (isItemTracker(stack)) {
                                trackerItem = stack;
                            }
                        }

                        if (trackerItem == null) {
                            var stack = this.getOffHandStack();
                            if (isItemTracker(stack)) {
                                trackerItem = stack;
                            }
                        }

                        if (trackerItem != null) {
                            updateCompass(player, trackerItem);
                        }

                        lastDelay = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeath(DamageSource source, CallbackInfo ci) {
        var scoreboard = server.getScoreboard();
        var runnersTeam = scoreboard.getTeam("runners");
        if (ManhuntMod.gameState != GameState.PREGAME) {
            if (this.getSpawnPointPosition() != null && this.getSpawnPointPosition() == OnGameTick.PLAYER_SPAWN_POS.get(this.getUuid())) {
                ManhuntGame.setPlayerSpawn(ManhuntMod.getOverworld(), player);
                player.setSpawnPoint(ManhuntMod.getOverworld().getRegistryKey(),
                        OnGameTick.PLAYER_SPAWN_POS.get(this.getUuid()), 0.0F, true, false);
            }
            if (ManhuntMod.gameState == GameState.PLAYING && this.isTeamPlayer(runnersTeam)) {
                if (runnersTeam.getPlayerList().size() == 1) {
                    ManhuntGame.end(server, true);
                }
            }
        }
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void pvpMixin(PlayerEntity attacker, CallbackInfoReturnable<Boolean> ci) {
        if (ManhuntMod.gameState == GameState.PREGAME || OnGameTick.paused) {
            ci.setReturnValue(false);
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            if (this.isTeamPlayer(attacker.getScoreboardTeam())) {
                if (ManhuntConfig.CONFIG.getFriendlyFire() != 1) {
                    if (ManhuntConfig.CONFIG.getFriendlyFire() == 2) {
                        if (!SettingsGui.FRIENDLY_FIRE.get(this.getUuid()) || !SettingsGui.FRIENDLY_FIRE.get(attacker.getUuid())) {
                            ci.setReturnValue(false);
                            attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire.per_player").formatted(Formatting.RED), false);
                        }
                    } else if (ManhuntConfig.CONFIG.getFriendlyFire() == 3) {
                        attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire").formatted(Formatting.RED), false);
                        ci.setReturnValue(false);
                    }
                }
            } else {
                if (OnGameTick.headStart && attacker.isTeamPlayer(this.getServer().getScoreboard().getTeam("runners"))) {
                    ci.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "updateKilledAdvancementCriterion", at = @At("TAIL"))
    private void updateUneasyAllianceAdvancement(Entity entityKilled, int score, DamageSource damageSource,
                                                 CallbackInfo ci) {
        if (entityKilled.getType() == EntityType.GHAST && this.getServerWorld().getRegistryKey() == ManhuntMod.getOverworld().getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":nether/uneasy_alliance")), "killed_ghast");
        }
    }

    @Inject(method = "worldChanged", at = @At("TAIL"))
    private void updateDimensionAdvancement(ServerWorld origin, CallbackInfo ci) {
        RegistryKey<World> to = this.getServerWorld().getRegistryKey();
        if (to == ManhuntMod.getTheNether().getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":story/enter_the_nether")), "entered_nether");
        } else if (to == ManhuntMod.getTheEnd().getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":story/enter_the_end")), "entered_end");
        }
    }

    @Unique
    private void updateCompass(ServerPlayerEntity player, ItemStack trackerItem) {
        if (!trackerItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt().contains("Name") && !OnGameTick.allRunners.isEmpty()) {
            NbtCompound nbt = trackerItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
            nbt.putString("Name", OnGameTick.allRunners.getFirst().getName().getString());
            trackerItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        ServerPlayerEntity trackedPlayer =
                server.getPlayerManager().getPlayer(trackerItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));

        if (trackedPlayer != null) {
            if (this.distanceTo(trackedPlayer) <= 128) {
                rate = 500;
            } else if (this.distanceTo(trackedPlayer) <= 512) {
                rate = 1000;
            } else if (this.distanceTo(trackedPlayer) <= 1024) {
                rate = 2000;
            } else {
                rate = 4000;
            }

            NbtCompound thisTag = trackedPlayer.writeNbt(new NbtCompound());
            NbtList positions = thisTag.getList("Positions", 10);
            int i;
            for (i = 0; i < positions.size(); ++i) {
                NbtCompound compound = positions.getCompound(i);
                if (compound.getString("LodestoneDimension").equals(player.writeNbt(new NbtCompound()).getString(
                        "Dimension"))) {
                    NbtCompound nbt = trackerItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt().copyFrom(compound);
                    trackerItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                    break;
                }
            }

            int[] is = trackerItem.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getIntArray("LodestonePos");

            if (is != null) {
                if (is.length >= 2) {
                    BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

                    trackerItem.set(DataComponentTypes.LODESTONE_TRACKER,
                            new LodestoneTrackerComponent(Optional.of(GlobalPos.create(this.getWorld().getRegistryKey(),
                                    blockPos)), false));
                }
            }
        }
    }

    @Unique
    private static boolean hasTracker(ServerPlayerEntity player) {
        var tracker =
                player.getInventory().contains(itemStack -> itemStack.get(DataComponentTypes.CUSTOM_DATA) != null && itemStack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker"));

        if (player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA) != null && player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
            tracker = true;
        }

        return tracker;
    }

    @Unique
    private boolean holdingTracker() {
        boolean holdingTracker = false;
        if (this.getMainHandStack().get(DataComponentTypes.CUSTOM_DATA) != null && this.getMainHandStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
            holdingTracker = true;
        } else if (this.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA) != null && this.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
            holdingTracker = true;
        }

        return holdingTracker;
    }

    @Unique
    private static void removeDuplicateTrackers(PlayerEntity player) {
        int trackerCount = 0;

        if (isItemTracker(player.playerScreenHandler.getCursorStack()) || isItemTracker(player.getOffHandStack())) {
            trackerCount++;
        }

        removeTrackers(player, trackerCount);
    }

    @Unique
    private static boolean isItemTracker(ItemStack stack) {
        return stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker");
    }

    @Unique
    private static void removeTrackers(PlayerEntity player, int trackerCount) {
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.main) {
            if (isItemTracker(stack)) {
                trackerCount++;
                if (trackerCount > 1) {
                    inventory.removeOne(stack);
                    trackerCount--;
                } else if (stack.getCount() > 1) {
                    var slot = inventory.getSlotWithStack(stack);
                    stack.setCount(1);
                    inventory.setStack(slot, stack);
                    trackerCount--;
                }
            }
        }
    }
}
