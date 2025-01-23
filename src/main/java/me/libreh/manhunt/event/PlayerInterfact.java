package me.libreh.manhunt.event;

import me.libreh.manhunt.commands.GeneralCommands;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
import me.libreh.manhunt.gui.ConfigGui;
import me.libreh.manhunt.gui.PreferencesGui;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import static me.libreh.manhunt.utils.Constants.READY_LIST;
import static me.libreh.manhunt.utils.Constants.SPAM_PREVENTION;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class PlayerInterfact {
    public static ActionResult useItem(PlayerEntity player, World world, Hand hand) {
        var stack = player.getStackInHand(hand);

        if (isPreGame()) {
            if (stack.getItem() == Items.PLAYER_HEAD) {
                PreferencesGui.openPreferencesGui((ServerPlayerEntity) player);
            }

            if (stack.getItem() == Items.RECOVERY_COMPASS) {
                if (SPAM_PREVENTION.get(player.getUuid()) < 8)
                    SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                if (SPAM_PREVENTION.get(player.getUuid()) < 4) {
                    if (!isHunter(player)) {
                        makeHunter(player);

                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.huntersColor),
                                Text.translatable("role.manhunt.hunters").formatted(Config.getConfig().gameOptions.teamColor.huntersColor)),
                                false);

                        player.playSoundToPlayer(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5F, 1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team",
                                Text.translatable("role.manhunt.hunter")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED), false);
                }
            }
            if (stack.getItem() == Items.RED_CONCRETE) {
                if (SPAM_PREVENTION.get(player.getUuid()) < 8)
                    SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                if (SPAM_PREVENTION.get(player.getUuid()) < 4) {
                    READY_LIST.add(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.ready", Text.literal(player.getNameForScoreboard()).formatted(Formatting.GREEN)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.ready")
                            .styled(style -> style.withColor(Formatting.GREEN).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);

                    if (READY_LIST.size() == server.getPlayerManager().getPlayerList().size()) {
                        int runners = 0;

                        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                            if (isRunner(serverPlayer)) {
                                runners++;
                                break;
                            }
                        }

                        if (runners == 0) {
                            server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.minimum",
                                    Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
                        } else {
                            GeneralCommands.executeStart();
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED), false);
                }
            }
            if (stack.getItem() == Items.LIME_CONCRETE) {
                if (SPAM_PREVENTION.get(player.getUuid()) < 8)
                    SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                if (SPAM_PREVENTION.get(player.getUuid()) < 4) {
                    READY_LIST.remove(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.not_ready", Text.literal(player.getNameForScoreboard()).formatted(Formatting.RED)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack itemStack = new ItemStack(Items.RED_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED), false);
                }
            }
            if (stack.getItem() == Items.CLOCK) {
                if (notSpamming(player)) {
                    if (!isRunner(player)) {
                        makeRunner(player);
                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(Config.getConfig().gameOptions.teamColor.runnersColor),
                                Text.translatable("role.manhunt.runners").formatted(Config.getConfig().gameOptions.teamColor.runnersColor)),
                                false);
                        player.playSoundToPlayer(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5F, 1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team", Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED), false);
                }
            }

            if (stack.getItem() == Items.COMMAND_BLOCK) {
                ConfigGui.openConfigGui((ServerPlayerEntity) player);
            }
        } else {
            if (isPlaying()) {
                if (isPaused) {
                    return ActionResult.FAIL;
                } else {
                    if (isHeadstart() && isHunter(player)) {
                        return ActionResult.FAIL;
                    }
                }
            }

            if (Config.getConfig().globalPreferences.netherLavaPvP.equals("off") || !PlayerData.get(player).netherLavaPvP) {
                if (world.getRegistryKey() == World.NETHER && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED), false);

                            return ActionResult.FAIL;
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    public static ActionResult useBlock(PlayerEntity player, World world, Hand hand, HitResult hitResult) {
        if (!isPreGame()) {
            if (isPlaying()) {
                if (isPaused) {
                    return ActionResult.FAIL;
                } else {
                    if (Config.getConfig().gameOptions.headStart != 0 && headStartTicks != 0 && isHunter(player)) {
                        return ActionResult.FAIL;
                    }
                }
            }

            if (Config.getConfig().globalPreferences.bedExplosionsPvP.equals("off") || !PlayerData.get(player).bedExplosionsPvP) {
                if (world.getRegistryKey() != World.OVERWORLD) {
                    if (player.getStackInHand(hand).getItem().getTranslationKey().contains("_bed")) {
                        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                            if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                                player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED), false);

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
