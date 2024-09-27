package manhunt.mixin.game;

import com.mojang.authlib.GameProfile;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntSettings;
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
    private int count;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        count++;
        if (count == 39) {
            if (ManhuntSettings.SLOW_DOWN_MANAGER.get(this.getUuid()) != 0) {
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(this.getUuid()) >= 4) {
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(this.getUuid(),
                            ManhuntSettings.SLOW_DOWN_MANAGER.get(this.getUuid()) - 4);
                } else {
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(this.getUuid(), 0);
                }
            }
            count = 0;
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
                            new LodestoneTrackerComponent(Optional.of(GlobalPos.create(ManhuntMod.overworld.getRegistryKey(), new BlockPos(0, 0, 0))), false));
                    stack.addEnchantment(server.getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.VANISHING_CURSE), 1);
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    this.giveItemStack(stack);
                } else {
                    if (System.currentTimeMillis() - lastDelay > rate && ManhuntConfig.CONFIG.getTrackerType() != 2 && ((ManhuntConfig.CONFIG.getTrackerType() == 3 && holdingTracker()) || ManhuntConfig.CONFIG.getTrackerType() == 4) || ManhuntSettings.TRACKER_TYPE.get(this.getUuid()) != 2 && ((ManhuntSettings.TRACKER_TYPE.get(this.getUuid()) == 3 && holdingTracker()) || ManhuntSettings.TRACKER_TYPE.get(this.getUuid()) == 4)) {
                        for (ItemStack stack : this.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
                                if (!stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().contains("Name") && !GameEvents.allRunners.isEmpty()) {
                                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                                    nbt.putString("Name", GameEvents.allRunners.getFirst().getName().getString());
                                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                                }

                                ServerPlayerEntity trackedPlayer =
                                        server.getPlayerManager().getPlayer(stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));

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

                                    updateCompass(player, stack, trackedPlayer);
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
        var scoreboard = server.getScoreboard();
        var runnersTeam = scoreboard.getTeam("runners");
        if (ManhuntMod.gameState != GameState.PREGAME) {
            if (this.getSpawnPointPosition() != null && this.getSpawnPointPosition() == GameEvents.PLAYER_SPAWN_POS.get(this.getUuid())) {
                ManhuntGame.setPlayerSpawn(ManhuntMod.overworld, player);
                player.setSpawnPoint(ManhuntMod.overworld.getRegistryKey(),
                        GameEvents.PLAYER_SPAWN_POS.get(this.getUuid()), 0f, true, false);
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
        if (ManhuntMod.gameState == GameState.PREGAME || GameEvents.paused) {
            ci.setReturnValue(false);
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            if (this.isTeamPlayer(attacker.getScoreboardTeam())) {
                if (ManhuntConfig.CONFIG.getFriendlyFire() != 1) {
                    if (ManhuntConfig.CONFIG.getFriendlyFire() == 2) {
                        if (!ManhuntSettings.FRIENDLY_FIRE.get(this.getUuid()) || !ManhuntSettings.FRIENDLY_FIRE.get(attacker.getUuid())) {
                            ci.setReturnValue(false);
                            attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire.per_player").formatted(Formatting.RED));
                        }
                    } else if (ManhuntConfig.CONFIG.getFriendlyFire() == 3) {
                        attacker.sendMessage(Text.translatable("chat.manhunt.friendly_fire").formatted(Formatting.RED));
                        ci.setReturnValue(false);
                    }
                }
            } else {
                if (GameEvents.headStart && attacker.isTeamPlayer(this.getServer().getScoreboard().getTeam("runners"))) {
                    ci.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "updateKilledAdvancementCriterion", at = @At("TAIL"))
    private void updateUneasyAllianceAdvancement(Entity entityKilled, int score, DamageSource damageSource,
                                                 CallbackInfo ci) {
        if (entityKilled.getType() == EntityType.GHAST && this.getServerWorld().getRegistryKey() == ManhuntMod.overworld.getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":nether/uneasy_alliance")), "killed_ghast");
        }
    }

    @Inject(method = "worldChanged", at = @At("TAIL"))
    private void updateDimensionAdvancement(ServerWorld origin, CallbackInfo ci) {
        RegistryKey<World> to = this.getServerWorld().getRegistryKey();
        if (to == ManhuntMod.theNether.getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":story/enter_the_nether")), "entered_nether");
        } else if (to == ManhuntMod.theEnd.getRegistryKey()) {
            this.getAdvancementTracker().grantCriterion(server.getAdvancementLoader().get(Identifier.of("minecraft" +
                    ":story/enter_the_end")), "entered_end");
        }
    }

    @Unique
    private void updateCompass(ServerPlayerEntity player, ItemStack stack, ServerPlayerEntity trackedPlayer) {
        NbtCompound thisTag = trackedPlayer.writeNbt(new NbtCompound());
        NbtList positions = thisTag.getList("Positions", 10);
        int i;
        for (i = 0; i < positions.size(); ++i) {
            NbtCompound compound = positions.getCompound(i);
            if (compound.getString("LodestoneDimension").equals(player.writeNbt(new NbtCompound()).getString(
                    "Dimension"))) {
                NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().copyFrom(compound);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                break;
            }
        }

        int[] is = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getIntArray("LodestonePos");

        if (is != null) {
            if (is.length >= 2) {
                BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

                stack.set(DataComponentTypes.LODESTONE_TRACKER,
                        new LodestoneTrackerComponent(Optional.of(GlobalPos.create(this.getWorld().getRegistryKey(),
                                blockPos)), false));
            }
        }
    }

    @Unique
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
}
