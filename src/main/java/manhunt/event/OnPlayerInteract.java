package manhunt.event;

import manhunt.config.ManhuntConfig;
import manhunt.config.gui.ConfigGui;
import manhunt.config.gui.SettingsGui;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static manhunt.ManhuntMod.*;

public class OnPlayerInteract {
    public static final List<UUID> READY_LIST = new ArrayList<>();

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, World world, Hand hand) {
        var stack = player.getStackInHand(hand);
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        var huntersTeam = scoreboard.getTeam("hunters");
        var runnersTeam = scoreboard.getTeam("runners");

        if (gameState == GameState.PREGAME) {
            if (stack.getItem() == Items.PLAYER_HEAD) {
                SettingsGui.openSettingsGui((ServerPlayerEntity) player);
            }
            if (stack.getItem() == Items.RECOVERY_COMPASS) {
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(huntersTeam)) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);

                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getHuntersColor()), Text.translatable("role.manhunt.hunters").formatted(ManhuntConfig.CONFIG.getHuntersColor())), false);

                        player.playSoundToPlayer(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5F
                                , 1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team", Text.translatable("role" +
                                ".manhunt.hunter")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.RED_CONCRETE) {
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    READY_LIST.add(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.ready",
                            Text.literal(player.getNameForScoreboard()).formatted(Formatting.GREEN)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.ready").styled(style -> style.withColor(Formatting.GREEN).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);

                    if (READY_LIST.size() == server.getPlayerManager().getPlayerList().size()) {
                        int runners = 0;
                        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                            if (serverPlayer.isTeamPlayer(runnersTeam)) {
                                runners++;
                                break;
                            }
                        }
                        if (runners == 0) {
                            server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.minimum",
                                    Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
                        } else {
                            OnGameTick.startingTime = 120;
                            OnGameTick.starting = true;
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.LIME_CONCRETE) {
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    READY_LIST.remove(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.not_ready",
                            Text.literal(player.getNameForScoreboard()).formatted(Formatting.RED)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack itemStack = new ItemStack(Items.RED_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.CLOCK) {
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(runnersTeam)) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()), Text.translatable("role.manhunt.runners").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
                        player.playSoundToPlayer(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5F,
                                1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team", Text.translatable("role" +
                                ".manhunt.runner")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.getItem() == Items.COMMAND_BLOCK) {
                ConfigGui.openConfigGui((ServerPlayerEntity) player);
            }
        } else {
            if (gameState == GameState.PLAYING) {
                if (OnGameTick.paused) {
                    return TypedActionResult.fail(stack);
                } else {
                    if (OnGameTick.waitForRunner && !OnGameTick.runnerHasStarted) {
                        return TypedActionResult.fail(stack);
                    } else {
                        if (OnGameTick.headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
            if (customData != null && customData.copyNbt().getBoolean("Tracker") && ManhuntConfig.CONFIG.getTrackerType() == 2 || SettingsGui.TRACKER_TYPE.get(player.getUuid()) == 2 && player.isTeamPlayer(huntersTeam) && !OnGameTick.allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                player.getItemCooldownManager().set(stack.getItem(), 20);
                if (customData.copyNbt().getString("Name") != null) {
                    NbtCompound nbt = customData.copyNbt();
                    nbt.putString("Name", OnGameTick.allRunners.getFirst().getName().getString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }
                ServerPlayerEntity trackedPlayer =
                        server.getPlayerManager().getPlayer(customData.copyNbt().getString("Name"));
                if (trackedPlayer != null) {
                    ManhuntGame.updateCompass((ServerPlayerEntity) player, stack, trackedPlayer);
                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.1f, 0.5F);
                }
            }
            if (!ManhuntConfig.CONFIG.isLavaPvpInNether()) {
                if (world.getRegistryKey() == getTheNether().getRegistryKey() && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
        }
        return TypedActionResult.pass(stack);
    }

    public static ActionResult useBlock(PlayerEntity player, World world, Hand hand, HitResult hitResult) {
        if (gameState != GameState.PREGAME) {
            if (gameState == GameState.PLAYING) {
                if (OnGameTick.paused) {
                    return ActionResult.FAIL;
                } else {
                    if (OnGameTick.waitForRunner && !OnGameTick.runnerHasStarted) {
                        return ActionResult.FAIL;
                    } else {
                        if (OnGameTick.headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                            return ActionResult.FAIL;
                        }
                    }
                }
            }
            if (!ManhuntConfig.CONFIG.isBedExplosions()) {
                if (world.getRegistryKey() != getOverworld().getRegistryKey()) {
                    if (player.getStackInHand(hand).getTranslationKey().contains("_bed")) {
                        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                            if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                                player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED));
                                return ActionResult.FAIL;
                            }
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }
}
