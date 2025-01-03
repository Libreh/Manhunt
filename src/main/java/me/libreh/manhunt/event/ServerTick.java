package me.libreh.manhunt.event;

import me.libreh.manhunt.command.ResetCommand;
import me.libreh.manhunt.command.game.pause.UnpauseCommand;
import me.libreh.manhunt.config.PreferencesData;
import me.libreh.manhunt.game.GameState;
import me.libreh.manhunt.world.ServerWorldController;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.random.Random;

import java.util.Set;
import java.util.concurrent.Future;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class ServerTick {
    public static void serverTick(MinecraftServer server) {
        if (firstReset) {
            firstReset = false;

            ServerWorldController.resetWorlds(ResetCommand.seed);
        }

        if (gameState == GameState.PRELOADING) {
            int doneCount = 0;
            for (Future<?> future : chunkFutureList) {
                if (future.isDone()) {
                    doneCount++;
                }
            }

            int totalCount = chunkFutureList.size();
            float percentage = (doneCount * 100f) / totalCount;
            String formattedPercentage = String.format("%.0f", percentage) + "%";
            Text message = Text.translatable("chat.manhunt.preloading", formattedPercentage, doneCount, totalCount);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!STARTED_PARKOUR.containsKey(player.getUuid()) || !STARTED_PARKOUR.get(player.getUuid())) {
                    player.sendMessage(message, true);
                }
            }
        }

        if (isPreGame()) {
            if (canStart) {
                int runners = 0;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (isRunner(player)) {
                        runners++;
                        break;
                    }
                }

                if (runners != 0) {
                    changeState(GameState.STARTED);
                } else {
                    canStart = false;

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.minimum",
                            Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
                }
            }
        } else if (isPlaying()) {
            if (shouldEnd) {
                huntersWin();
            }

            tickCount++;
            if (tickCount == 19) {
                if (isPaused) {
                    pauseTicks -= 20;

                    if (pauseTicks <= 0) {
                        UnpauseCommand.unpauseGame();

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(false));
                        }
                    } else {
                        String hoursString;
                        int hours = (int) Math.floor((double) pauseTicks % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        String minutesString;
                        int minutes = (int) Math.floor((double) pauseTicks % (20 * 60 * 60) / (20 * 60));
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String secondsString;
                        int seconds = (int) Math.floor((double) pauseTicks % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 5));
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt.paused").formatted(Formatting.YELLOW)));
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("chat.manhunt.time.triple",
                                            hoursString, minutesString, secondsString).formatted(Formatting.GOLD)));
                        }
                    }
                } else {
                    if (CONFIG.getTimeLimitMin() != 0) {
                        timeLimitTicks -= 20;

                        int hours = (int) Math.floor((double) timeLimitTicks % (20 * 60 * 60 * 24) / (20 * 60 * 60));

                        String hoursString;
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }

                        int minutes = (int) Math.floor((double) timeLimitTicks % (20 * 60 * 60) / (20 * 60));

                        String minutesString;
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }

                        int seconds = (int) Math.floor((double) timeLimitTicks % (20 * 60) / (20));

                        String secondsString;
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        if (headStartTicks == 0 && CONFIG.getTimeLimitMin() != 0) {
                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.triple",
                                        hoursString, minutesString, secondsString).styled(style -> style.withBold(true)), true);
                            }
                        }

                        if (timeLimitTicks <= 0) {
                            huntersWin();
                        }
                    }

                    if (isHeadstart()) {
                        headStartTicks -= 20;
                        int seconds = (int) Math.floor((double) headStartTicks % (20 * 60) / (20));
                        int color;
                        float pitch = 0.0F;

                        if (seconds >= 10) {
                            color = 5766999;
                        } else {
                            pitch = 1.0F;
                            if (seconds == 9) {
                                color = 8185892;
                            } else if (seconds == 8) {
                                color = 9687040;
                            } else if (seconds == 7) {
                                color = 10729472;
                            } else if (seconds == 6) {
                                color = 11378688;
                            } else if (seconds == 5) {
                                color = 11765504;
                                pitch = 0.9F;
                            } else if (seconds == 4) {
                                color = 11955456;
                                pitch = 0.8F;
                            } else if (seconds == 3) {
                                color = 11883008;
                                pitch = 0.7F;
                            } else if (seconds == 2) {
                                color = 11547648;
                                pitch = 0.6F;
                            } else if (seconds == 1) {
                                color = 11010048;
                                pitch = 0.5F;
                            } else {
                                color = 5766999;
                            }
                        }

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.sendMessage(Text.translatable("chat.manhunt.head_start",
                                    Text.literal(String.valueOf(seconds))).styled(style -> style.withColor(color)), true);
                            if (pitch != 0.0F) {
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.MASTER, 0.5F, pitch);
                            }
                        }

                        if (headStartTicks == 0) {
                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable("chat.manhunt.hunters_released").formatted(Formatting.GOLD), true);
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.MASTER, 0.5F, 0.5F);
                            }
                        }
                    }
                }

                tickCount = 0;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            var playerUuid = player.getUuid();
            var data = PreferencesData.get(player);

            if (JOIN_LIST.contains(playerUuid) && !player.notInAnyWorld) {
                JOIN_LIST.remove(playerUuid);

                updateGameMode(player);

                if (isPlaying()) {
                    if (isPaused) {
                        if (isRunner(player) && RUNNERS_TEAM.getPlayerList().size() == 1) {
                            UnpauseCommand.unpauseGame();
                        } else if (!PAUSE_LEAVE_LIST.contains(playerUuid)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5F);

                            SAVED_POS.put(playerUuid, player.getPos());
                            SAVED_YAW.put(playerUuid, player.getYaw());
                            SAVED_PITCH.put(playerUuid, player.getPitch());

                            player.getHungerManager().readNbt(SAVED_HUNGER.get(playerUuid));
                            SAVED_AIR.put(playerUuid, player.getAir());

                            if (!player.getStatusEffects().isEmpty()) {
                                SAVED_EFFECTS.put(playerUuid, player.getStatusEffects());
                            }

                            lockPlayer(player);

                            player.clearStatusEffects();
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS,
                                    StatusEffectInstance.INFINITE, 255,
                                    false, false, false));
                            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                                    StatusEffectInstance.INFINITE, 255,
                                    false, false, false));
                        }
                    } else {
                        if (PAUSE_LEAVE_LIST.contains(playerUuid)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                        }
                    }
                }
            }

            if (isPreGame()) {
                if (RESET_LIST.contains(playerUuid) && !player.notInAnyWorld && !player.isDead()) {
                    RESET_LIST.remove(playerUuid);

                    updateGameMode(player);
                }

                if (!hasItem(player, Items.PLAYER_HEAD)) {
                    var nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    var stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.preferences")
                            .styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    removeDuplicateItems(player, Items.PLAYER_HEAD, 0);
                }

                if (CONFIG.getPresetMode().equals("free_select")) {
                    if (!hasItem(player, Items.RECOVERY_COMPASS)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_hunters")
                                .styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        removeDuplicateItems(player, Items.RECOVERY_COMPASS, 2);
                    }

                    if (!hasItem(player, Items.CLOCK)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_runners")
                                .styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        removeDuplicateItems(player, Items.CLOCK, 6);
                    }

                    if (!hasItem(player, Items.RED_CONCRETE) && !hasItem(player, Items.LIME_CONCRETE)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.RED_CONCRETE);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.not_ready")
                                .styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(4, stack);
                    } else {
                        if (hasItem(player, Items.RED_CONCRETE)) {
                            removeDuplicateItems(player, Items.RED_CONCRETE, 4);
                        }

                        if (hasItem(player, Items.LIME_CONCRETE)) {
                            removeDuplicateItems(player, Items.LIME_CONCRETE, 4);
                        }
                    }
                } else {
                    clearItem(player, Items.RECOVERY_COMPASS);
                    clearItem(player, Items.RED_CONCRETE);
                    clearItem(player, Items.LIME_CONCRETE);
                    clearItem(player, Items.CLOCK);
                }

                if (!hasItem(player, Items.COMMAND_BLOCK)) {
                    var nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    var stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.config")
                            .styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    removeDuplicateItems(player, Items.COMMAND_BLOCK, 8);
                }

                if (!canStart && !Permissions.check(player, "manhunt.parkour")) {
                    if (player.getZ() < -2 && player.getWorld().getRegistryKey() == LOBBY_REGISTRY_KEY) {
                        PARKOUR_TIMER.putIfAbsent(playerUuid, 0);
                        STARTED_PARKOUR.putIfAbsent(playerUuid, false);
                        FINISHED_PARKOUR.putIfAbsent(playerUuid, false);

                        int tick = PARKOUR_TIMER.get(playerUuid);

                        var sec = (int) Math.floor((double) (tick % (20 * 60)) / (20));

                        String secStr;
                        if (sec < 10) {
                            secStr = "0" + sec;
                        } else {
                            secStr = String.valueOf(sec);
                        }

                        var ms = (int) Math.floor((double) (tick * 5) % 100);

                        String msStr;
                        if (ms < 10) {
                            msStr = "0" + ms;
                        } else if (ms > 99) {
                            msStr = "00";
                        } else {
                            msStr = String.valueOf(ms);
                        }

                        if (!FINISHED_PARKOUR.get(playerUuid)) {
                            if (!STARTED_PARKOUR.get(playerUuid) && player.getZ() < -3 && !(player.getZ() < -6)) {
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1.0F, 1.0F);
                                STARTED_PARKOUR.put(playerUuid, true);
                            }

                            if (STARTED_PARKOUR.get(playerUuid)) {
                                if (player.getZ() < -3) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr), true);
                                    PARKOUR_TIMER.put(playerUuid, PARKOUR_TIMER.get(playerUuid) + 1);
                                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 255, false, false, false));

                                    if (player.getZ() < -24 && player.getZ() > -26 && player.getX() < -6 && player.getY() >= -4 && player.getY() < 8) {
                                        player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr).formatted(Formatting.GREEN), true);
                                        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1.0F, 2.0F);
                                        FINISHED_PARKOUR.put(playerUuid, true);
                                    }
                                }
                            }
                        }

                        if (STARTED_PARKOUR.get(playerUuid)) {
                            if (sec > 59 || (player.getZ() > -3 || player.getY() < -2 || (player.getZ() < -27 && player.getY() < 6))) {
                                if (!FINISHED_PARKOUR.get(playerUuid)) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr).formatted(Formatting.RED), true);
                                }

                                parkourReset(player);
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.RECORDS, LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z, 1.0F, 0.5F, Random.create().nextLong()));
                            }
                        }
                    } else if ((player.getZ() > 16 && player.getY() < -5) || (player.getZ() < 16 && (player.getY() < -3 || player.getY() > 10))) {
                        teleportToLobby(player);
                    }
                }
            }

            if (isPlaying()) {
                if (START_LIST.contains(playerUuid) && !player.notInAnyWorld) {
                    START_LIST.remove(playerUuid);

                    PLAY_LIST.add(playerUuid);

                    updateGameMode(player);


                    if (CONFIG.getCustomSounds().equals("always") || data.customSounds) {
                        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.MASTER, 0.5F, 2.0F);
                    }
                    if (CONFIG.getCustomTitles().equals("always") || data.customTitles) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt.start")));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("title.manhunt.glhf").formatted(Formatting.GRAY)));
                    }
                }

                if (PAUSE_LEAVE_LIST.contains(playerUuid)) {
                    PAUSE_LEAVE_LIST.remove(playerUuid);

                    resetAttributes(player);

                    player.clearStatusEffects();
                    if (SAVED_EFFECTS.containsKey(playerUuid)) {
                        for (StatusEffectInstance statusEffect : SAVED_EFFECTS.get(playerUuid)) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                }

                if (isPaused) {
                    if (SAVED_POS.containsKey(playerUuid) && SAVED_YAW.containsKey(playerUuid) && SAVED_PITCH.containsKey(playerUuid)) {
                        player.teleport(server.getWorld(player.getWorld().getRegistryKey()),
                                SAVED_POS.get(playerUuid).getX(), SAVED_POS.get(playerUuid).getY(), SAVED_POS.get(playerUuid).getZ(),
                                Set.of(), SAVED_YAW.get(playerUuid), SAVED_PITCH.get(playerUuid), true);
                    } else {
                        SAVED_POS.put(playerUuid, player.getPos());
                        SAVED_YAW.put(playerUuid, player.getYaw());
                        SAVED_PITCH.put(playerUuid, player.getPitch());
                    }
                } else {
                    if (isHeadstart()) {
                        freezeHunters();
                    }
                }
            }
        }
    }

    public static void freezeHunters() {
        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            var playerUuid = player.getUuid();

            if (isHunter(player)) {
                if (!SPAWN_POS.containsKey(playerUuid)) {
                    setPlayerSpawn(player);
                }

                if (SPAWN_POS.containsKey(playerUuid)) {
                    var spawnPos = SPAWN_POS.get(playerUuid);

                    player.teleport(player.getServerWorld(), spawnPos.x, spawnPos.y, spawnPos.z,
                            Set.of(), 0.0F, 0.0F, true);
                }
            }
        }
    }

    private static boolean hasItem(PlayerEntity player, Item item) {
        return player.getInventory().contains(itemStack -> itemStack.getItem() == item);
    }

    private static void removeDuplicateItems(PlayerEntity player, Item item, int slot) {
        var itemCount = 0;

        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == item) {
                itemCount++;

                if (player.getInventory().getSlotWithStack(stack) != slot) {
                    player.getInventory().removeOne(stack);
                    break;
                } else if (itemCount > 1) {
                    player.getInventory().removeOne(stack);
                    itemCount--;
                } else if (stack.getCount() > 1) {
                    stack.setCount(1);
                    player.getInventory().setStack(slot, stack);
                }
            }
        }

        if (itemCount != 0) {
            if (player.playerScreenHandler.getCursorStack().getItem() == item) {
                itemCount++;

                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() == item) {
                        if (itemCount > 1) {
                            player.getInventory().removeOne(stack);
                            itemCount--;
                        }
                    }
                }
            } else if (player.getOffHandStack().getItem() == item) {
                itemCount++;

                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() == item) {
                        if (itemCount > 1) {
                            player.getInventory().removeOne(stack);
                            itemCount--;
                        }
                    }
                }
            }
        }
    }

    private static void clearItem(PlayerEntity player, Item item) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == item) {
                player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
            }
        }

        if (player.playerScreenHandler.getCursorStack().getItem() == item) {
            player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
        }

        if (player.getOffHandStack().getItem() == item) {
            player.getInventory().removeOne(player.getOffHandStack());
        }
    }
}
