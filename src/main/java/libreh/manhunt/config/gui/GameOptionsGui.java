package libreh.manhunt.config.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class GameOptionsGui {
    public static List<ServerPlayerEntity> playerList = new ArrayList<>();
    public static UUID mainRunnerUUID;
    public static int mainRunnerTries = 0;

    public static void openGameOptionsGui(ServerPlayerEntity player) {
        SimpleGui gameOptionsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

        gameOptionsGui.setTitle(Text.translatable("config.manhunt.game_options"));

        ManhuntConfig.CONFIG.save();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;

        loreList = new ArrayList<>();
        name = "set_motd";
        item = Items.REPEATING_COMMAND_BLOCK;
        boolvalue = ManhuntConfig.CONFIG.isSetMotd();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var setMotdBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setSetMotd(ManhuntConfig.CONFIG.isSetMotdDefault());
                            } else {
                                ManhuntConfig.CONFIG.setSetMotd(!setMotdBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "role_preset";
        item = Items.FLETCHING_TABLE;
        intvalue = ManhuntConfig.CONFIG.getRolePreset();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".free_select")).styled(style -> style.withColor(Formatting.WHITE).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".equal_split")).styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        } else if (intvalue == 3) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".speedrun_showdown")).styled(style -> style.withColor(Formatting.GREEN).withItalic(false)));
        } else if (intvalue == 4) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".runner_cycle")).styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
        } else if (intvalue == 5) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".main_runner")).styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
        } else if (intvalue == 6) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".hunter_infection")).styled(style -> style.withColor(Formatting.RED).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset" +
                    ".no_selection")).styled(style -> style.withColor(Formatting.DARK_RED).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> openRolePresetGui(player, type)));
        slot++;

        loreList = new ArrayList<>();
        name = "team_color";
        item = Items.WHITE_BANNER;
        boolvalue = ManhuntConfig.CONFIG.isTeamColor();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var teamColorBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorBool)));
        slot++;

        loreList = new ArrayList<>();
        name = "wait_for_runner";
        item = Items.SAND;
        boolvalue = ManhuntConfig.CONFIG.isWaitForRunner();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var waitForRunnerBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setWaitForRunner(ManhuntConfig.CONFIG.isWaitForRunnerDefault());
                            } else {
                                ManhuntConfig.CONFIG.setWaitForRunner(!waitForRunnerBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "hunter_release_time";
        item = Items.NETHERITE_BOOTS;
        intvalue = ManhuntConfig.CONFIG.getHunterReleaseTime();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 10 && intvalue != 20 && intvalue != 30) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 10) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10").formatted(Formatting.RED),
                        Text.literal("20"), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 20) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"),
                        Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"), Text.literal("20"),
                        Text.literal("30").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int hunterReleaseTimeInt = intvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).hideDefaultTooltip().setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setHunterReleaseTime(ManhuntConfig.CONFIG.getHunterReleaseTimeDefault());
                                } else {
                                    if (hunterReleaseTimeInt != 10 && hunterReleaseTimeInt != 20) {
                                        ManhuntConfig.CONFIG.setHunterReleaseTime(10);
                                    } else {
                                        if (hunterReleaseTimeInt == 10) {
                                            ManhuntConfig.CONFIG.setHunterReleaseTime(20);
                                        } else {
                                            ManhuntConfig.CONFIG.setHunterReleaseTime(30);
                                        }
                                    }
                                }
                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui hunterReleaseTimeGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    int value = hunterReleaseTimeInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input").formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.CONFIG.setHunterReleaseTime(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };
                                hunterReleaseTimeGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                hunterReleaseTimeGui.setDefaultInputValue("");
                                hunterReleaseTimeGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "runner_head_start";
        item = Items.GOLDEN_BOOTS;
        intvalue = ManhuntConfig.CONFIG.getRunnerHeadStart();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 10 && intvalue != 20 && intvalue != 30) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 10) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10").formatted(Formatting.RED),
                        Text.literal("20"), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 20) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"),
                        Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"), Text.literal("20"),
                        Text.literal("30").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int runnerHeadstartInt = intvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).hideDefaultTooltip().setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setRunnerHeadStart(ManhuntConfig.CONFIG.getRunnerHeadStartDefault());
                                } else {
                                    if (runnerHeadstartInt != 10 && runnerHeadstartInt != 20) {
                                        ManhuntConfig.CONFIG.setRunnerHeadStart(10);
                                    } else {
                                        if (runnerHeadstartInt == 10) {
                                            ManhuntConfig.CONFIG.setRunnerHeadStart(20);
                                        } else {
                                            ManhuntConfig.CONFIG.setRunnerHeadStart(30);
                                        }
                                    }
                                }
                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui runnerHeadstartGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    int value = runnerHeadstartInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input").formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.CONFIG.setRunnerHeadStart(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };
                                runnerHeadstartGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                runnerHeadstartGui.setDefaultInputValue("");
                                runnerHeadstartGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "runners_glow";
        item = Items.GLOWSTONE;
        boolvalue = ManhuntConfig.CONFIG.isRunnersGlow();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersGlowBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnersGlow(ManhuntConfig.CONFIG.isRunnersGlowDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnersGlow(!runnersGlowBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "hunt_on_death";
        item = Items.SKELETON_SKULL;
        boolvalue = ManhuntConfig.CONFIG.isHuntOnDeath();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersHuntOnDeathBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setHuntOnDeath(ManhuntConfig.CONFIG.isHuntOnDeathDefault());
                            } else {
                                ManhuntConfig.CONFIG.setHuntOnDeath(!runnersHuntOnDeathBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        gameOptionsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            ConfigGui.playUISound(player);
            ConfigGui.openConfigGui(player);
        }));
        slot++;

        loreList = new ArrayList<>();
        name = "runners_can_pause";
        item = Items.BLUE_ICE;
        boolvalue = ManhuntConfig.CONFIG.isRunnersCanPause();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersCanPauseBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnersCanPause(ManhuntConfig.CONFIG.isRunnersCanPauseDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnersCanPause(!runnersCanPauseBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "leave_pause_time";
        item = Items.PRISMARINE;
        intvalue = ManhuntConfig.CONFIG.getLeavePauseTime();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 1 && intvalue != 2 && intvalue != 5) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 1) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1").formatted(Formatting.GREEN),
                        Text.literal("2"), Text.literal("5")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 2) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1"),
                        Text.literal("2").formatted(Formatting.YELLOW), Text.literal("5")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1"), Text.literal("2"),
                        Text.literal("5").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int pauseTimeOnLeaveInt = intvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                            ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                    ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setLeavePauseTime(ManhuntConfig.CONFIG.getLeavePauseTimeDefault());
                                } else {
                                    if (pauseTimeOnLeaveInt != 1 && pauseTimeOnLeaveInt != 2) {
                                        ManhuntConfig.CONFIG.setLeavePauseTime(1);
                                    } else {
                                        if (pauseTimeOnLeaveInt == 1) {
                                            ManhuntConfig.CONFIG.setLeavePauseTime(2);
                                        } else {
                                            ManhuntConfig.CONFIG.setLeavePauseTime(5);
                                        }
                                    }
                                }
                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui pauseTimeOnLeaveGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    int value = pauseTimeOnLeaveInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input").formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.CONFIG.setLeavePauseTime(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };
                                pauseTimeOnLeaveGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                pauseTimeOnLeaveGui.setDefaultInputValue("");
                                pauseTimeOnLeaveGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "time_limit";
        item = Items.CLOCK;
        intvalue = ManhuntConfig.CONFIG.getTimeLimit();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 30 && intvalue != 60 && intvalue != 90) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.literal(String.valueOf(ManhuntConfig.CONFIG.getTimeLimit())).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 30) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30").formatted(Formatting.RED),
                        Text.literal("60"), Text.literal("90")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 60) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30"),
                        Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30"), Text.literal("60"),
                        Text.literal("90").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int timeLimitInt = intvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (!type.shift) {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setTimeLimit(ManhuntConfig.CONFIG.getTimeLimitDefault());
                                } else {
                                    if (timeLimitInt != 30 && timeLimitInt != 60) {
                                        ManhuntConfig.CONFIG.setTimeLimit(30);
                                    } else {
                                        if (timeLimitInt == 30) {
                                            ManhuntConfig.CONFIG.setTimeLimit(60);
                                        } else {
                                            ManhuntConfig.CONFIG.setTimeLimit(90);
                                        }
                                    }
                                }
                                openGameOptionsGui(player);
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        } else {
                            AnvilInputGui timeLimitGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2,
                                            new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                int value = timeLimitInt;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                }
                                                ManhuntConfig.CONFIG.setTimeLimit(value);
                                                openGameOptionsGui(player);
                                            }));
                                }
                            };
                            timeLimitGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                            timeLimitGui.setDefaultInputValue("");
                            timeLimitGui.open();
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "spectate_on_win";
        item = Items.SPYGLASS;
        boolvalue = ManhuntConfig.CONFIG.isSpectateOnWin();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var spectateWinBool = boolvalue;
        gameOptionsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setSpectateOnWin(ManhuntConfig.CONFIG.isSpectateOnWinDefault());
                            } else {
                                ManhuntConfig.CONFIG.setSpectateOnWin(!spectateWinBool);
                            }
                            openGameOptionsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

        gameOptionsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        gameOptionsGui.open();
    }

    private static void openRolePresetGui(ServerPlayerEntity player, ClickType clickType) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                if (clickType == ClickType.DROP) {
                    ManhuntConfig.CONFIG.setRolePreset(ManhuntConfig.CONFIG.getRolePresetDefault());
                    openGameOptionsGui(player);
                } else {
                    var rolePresetGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    rolePresetGui.setTitle(Text.translatable("config.manhunt.team_color"));

                    ConfigGui.playUISound(player);

                    var server = player.server;
                    var scoreboard = player.getScoreboard();

                    rolePresetGui.setSlot(0, new GuiElementBuilder(Items.GLASS).setName(Text.translatable("lore" +
                            ".manhunt.role_preset.free_select")).setCallback(() -> {
                        ManhuntConfig.CONFIG.setRolePreset(1);
                        openGameOptionsGui(player);
                    }));

                    rolePresetGui.setSlot(1,
                            new GuiElementBuilder(Items.CYAN_STAINED_GLASS).setName(Text.translatable("lore.manhunt" +
                                    ".role_preset.equal_split").formatted(Formatting.AQUA)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setRolePreset(2);
                                List<ServerPlayerEntity> players =
                                        new ArrayList<>(server.getPlayerManager().getPlayerList());
                                Collections.shuffle(players);

                                String team = "hunters";
                                for (ServerPlayerEntity serverPlayer : players) {
                                    if (team.equals("hunters")) {
                                        team = "runners";
                                    } else {
                                        team = "hunters";
                                    }
                                    scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(),
                                            scoreboard.getTeam(team));
                                }

                                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.equal_split",
                                        Text.translatable("role.manhunt.hunters").formatted(ManhuntConfig.CONFIG.getHuntersColor()),
                                        Text.translatable("role.manhunt.runners").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
                                openGameOptionsGui(player);
                            }));

                    rolePresetGui.setSlot(2,
                            new GuiElementBuilder(Items.LIME_STAINED_GLASS).setName(Text.translatable("lore.manhunt" +
                                    ".role_preset.speedrun_showdown").formatted(Formatting.GREEN)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setRolePreset(3);
                                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                    scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(),
                                            scoreboard.getTeam(
                                                    "runners"));
                                }

                                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                                                Text.literal(
                                                        "Everyone").formatted(ManhuntConfig.CONFIG.getRunnersColor()),
                                                Text.translatable(
                                                        "role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())),
                                        false);
                                openGameOptionsGui(player);
                            }));

                    rolePresetGui.setSlot(3,
                            new GuiElementBuilder(Items.YELLOW_STAINED_GLASS).setName(Text.translatable("lore.manhunt" +
                                    ".role_preset.runner_cycle").formatted(Formatting.YELLOW)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setRolePreset(4);
                                playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                                playerList.removeIf(Objects::isNull);
                                Collections.shuffle(playerList);

                                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                    scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(),
                                            scoreboard.getTeam(
                                                    "hunters"));
                                }
                                ServerPlayerEntity runner = playerList.getFirst();
                                scoreboard.addScoreHolderToTeam(runner.getNameForScoreboard(), scoreboard.getTeam(
                                        "runners"));
                                playerList.remove(runner);
                                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                        Text.literal(runner.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()), Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
                                openGameOptionsGui(player);
                            }));

                    rolePresetGui.setSlot(4,
                            new GuiElementBuilder(Items.ORANGE_STAINED_GLASS).setName(Text.translatable("lore.manhunt" +
                                    ".role_preset.main_runner").formatted(Formatting.GOLD)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setRolePreset(5);
                                playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                                playerList.removeIf(Objects::isNull);
                                Collections.shuffle(playerList);

                                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                    scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(),
                                            scoreboard.getTeam(
                                                    "hunters"));
                                }

                                ServerPlayerEntity runner = playerList.getFirst();
                                if (mainRunnerTries != 0) {
                                    mainRunnerTries--;
                                } else {
                                    if (mainRunnerUUID != null) {
                                        runner = server.getPlayerManager().getPlayer(mainRunnerUUID);
                                    } else {
                                        mainRunnerUUID = playerList.getFirst().getUuid();
                                    }
                                    mainRunnerTries = 3;
                                }
                                scoreboard.addScoreHolderToTeam(runner.getNameForScoreboard(), scoreboard.getTeam(
                                        "runners"));
                                playerList.remove(runner);
                                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                        Text.literal(runner.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()), Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
                                openGameOptionsGui(player);
                            }));

                    rolePresetGui.setSlot(5,
                            new GuiElementBuilder(Items.RED_STAINED_GLASS).setName(Text.translatable("lore.manhunt" +
                                    ".role_preset.hunter_infection").formatted(Formatting.RED)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setRolePreset(6);
                                List<ServerPlayerEntity> players =
                                        new ArrayList<>(server.getPlayerManager().getPlayerList());
                                Collections.shuffle(players);
                                ServerPlayerEntity hunter = players.getFirst();

                                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                    scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(),
                                            scoreboard.getTeam(
                                                    "runners"));
                                }

                                scoreboard.addScoreHolderToTeam(hunter.getNameForScoreboard(), scoreboard.getTeam(
                                        "hunters"));
                                server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                        Text.literal(hunter.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getHuntersColor()), Text.translatable("role.manhunt.hunter").formatted(ManhuntConfig.CONFIG.getHuntersColor())), false);
                                openGameOptionsGui(player);
                            }));

                    rolePresetGui.setSlot(6, new GuiElementBuilder(Items.TINTED_GLASS).setName(Text.translatable(
                            "lore.manhunt.role_preset.no_selection").formatted(Formatting.DARK_RED)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setRolePreset(7);
                        openGameOptionsGui(player);
                    }));

                    rolePresetGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openGameOptionsGui(player)));

                    rolePresetGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
        }
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Boolean boolvalue) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                if (!clickType.shift) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.CONFIG.setTeamColor(ManhuntConfig.CONFIG.isTeamColorDefault());
                    } else {
                        ManhuntConfig.CONFIG.setTeamColor(!boolvalue);

                        if (ManhuntConfig.CONFIG.isTeamColor()) {
                            player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                            player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                        } else {
                            player.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
                            player.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
                        }
                    }
                    openGameOptionsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    teamColorGui.setTitle(Text.translatable("config.manhunt.team_color"));

                    ManhuntConfig.CONFIG.save();
                    ConfigGui.playUISound(player);

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.literal(ManhuntConfig.CONFIG.getHuntersColor().name()).styled(style -> style.withColor(ManhuntConfig.CONFIG.getHuntersColor()).withItalic(false)));
                    loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable(
                            "config.manhunt.hunters_color").formatted(ManhuntConfig.CONFIG.getHuntersColor())).setLore(loreList).setCallback((index, type, action) -> {
                        if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setHuntersColor(ManhuntConfig.CONFIG.getHuntersColorDefault());

                                    openGameOptionsGui(player);
                                } else {
                                    var huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                    huntersColorGui.setTitle(Text.translatable("config.manhunt" + ".hunters_color"));

                                    ManhuntConfig.CONFIG.save();
                                    ConfigGui.playUISound(player);

                                    huntersColorGui.setSlot(0,
                                            new GuiElementBuilder(Items.WHITE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.white")).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.RESET);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(1,
                                            new GuiElementBuilder(Items.LIGHT_GRAY_WOOL).setName(Text.translatable(
                                                    "color" + ".minecraft.light_gray").formatted(Formatting.GRAY)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.GRAY);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(2,
                                            new GuiElementBuilder(Items.GRAY_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.gray").formatted(Formatting.DARK_GRAY)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.DARK_GRAY);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(3,
                                            new GuiElementBuilder(Items.BLACK_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.black").formatted(Formatting.BLACK)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.BLACK);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(4,
                                            new GuiElementBuilder(Items.RED_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft" + ".red").formatted(Formatting.RED)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.RED);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(5,
                                            new GuiElementBuilder(Items.ORANGE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.orange").formatted(Formatting.GOLD)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.GOLD);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(6,
                                            new GuiElementBuilder(Items.YELLOW_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.yellow").formatted(Formatting.YELLOW)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.YELLOW);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(8,
                                            new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                                                    "text" +
                                                            ".text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(teamColorGui::open));

                                    huntersColorGui.setSlot(9,
                                            new GuiElementBuilder(Items.LIME_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.lime").formatted(Formatting.GREEN)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.GREEN);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(10,
                                            new GuiElementBuilder(Items.GREEN_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.green").formatted(Formatting.DARK_GREEN)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.DARK_GREEN);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(11,
                                            new GuiElementBuilder(Items.CYAN_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.cyan").formatted(Formatting.DARK_AQUA)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.DARK_AQUA);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(12,
                                            new GuiElementBuilder(Items.LIGHT_BLUE_WOOL).setName(Text.translatable(
                                                    "color" + ".minecraft.light_blue").formatted(Formatting.BLUE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.BLUE);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(13,
                                            new GuiElementBuilder(Items.BLUE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.blue").formatted(Formatting.DARK_BLUE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.DARK_BLUE);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(14,
                                            new GuiElementBuilder(Items.PURPLE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.purple").formatted(Formatting.DARK_PURPLE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.DARK_PURPLE);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(15,
                                            new GuiElementBuilder(Items.MAGENTA_WOOL).setName(Text.translatable(
                                                    "color" +
                                                            ".minecraft.magenta").formatted(Formatting.LIGHT_PURPLE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.LIGHT_PURPLE);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.open();
                                }
                            }
                        }
                    }));

                    loreList = new ArrayList<>();

                    loreList.add(Text.literal(ManhuntConfig.CONFIG.getRunnersColor().name()).styled(style -> style.withColor(ManhuntConfig.CONFIG.getRunnersColor()).withItalic(false)));
                    loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK).setName(Text.translatable("config.manhunt.runners_color").formatted(ManhuntConfig.CONFIG.getRunnersColor())).setLore(loreList).setCallback((index, type, action) -> {
                        if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setRunnersColor(ManhuntConfig.CONFIG.getRunnersColorDefault());
                                    ConfigGui.playUISound(player);
                                    openGameOptionsGui(player);
                                } else {
                                    var runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                    runnersColorGui.setTitle(Text.translatable("config.manhunt" + ".runners_color"));

                                    ManhuntConfig.CONFIG.save();
                                    ConfigGui.playUISound(player);

                                    runnersColorGui.setSlot(0,
                                            new GuiElementBuilder(Items.WHITE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.white")).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.RESET);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(1,
                                            new GuiElementBuilder(Items.LIGHT_GRAY_WOOL).setName(Text.translatable(
                                                    "color" + ".minecraft.light_gray").formatted(Formatting.GRAY)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.GRAY);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(2,
                                            new GuiElementBuilder(Items.GRAY_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.gray").formatted(Formatting.DARK_GRAY)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.DARK_GRAY);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(3,
                                            new GuiElementBuilder(Items.BLACK_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.black").formatted(Formatting.BLACK)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.BLACK);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(4,
                                            new GuiElementBuilder(Items.RED_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft" + ".red").formatted(Formatting.RED)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.RED);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(5,
                                            new GuiElementBuilder(Items.ORANGE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.orange").formatted(Formatting.GOLD)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.GOLD);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(6,
                                            new GuiElementBuilder(Items.YELLOW_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.yellow").formatted(Formatting.YELLOW)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.YELLOW);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(8,
                                            new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                                                    "text" +
                                                            ".text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(teamColorGui::open));

                                    runnersColorGui.setSlot(9,
                                            new GuiElementBuilder(Items.LIME_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.lime").formatted(Formatting.GREEN)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.GREEN);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(10,
                                            new GuiElementBuilder(Items.GREEN_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.green").formatted(Formatting.DARK_GREEN)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.DARK_GREEN);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(11,
                                            new GuiElementBuilder(Items.CYAN_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.cyan").formatted(Formatting.DARK_AQUA)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.DARK_AQUA);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(12,
                                            new GuiElementBuilder(Items.LIGHT_BLUE_WOOL).setName(Text.translatable(
                                                    "color" + ".minecraft.light_blue").formatted(Formatting.BLUE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.BLUE);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(13,
                                            new GuiElementBuilder(Items.BLUE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.blue").formatted(Formatting.DARK_BLUE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.DARK_BLUE);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(14,
                                            new GuiElementBuilder(Items.PURPLE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.purple").formatted(Formatting.DARK_PURPLE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.DARK_PURPLE);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(15,
                                            new GuiElementBuilder(Items.MAGENTA_WOOL).setName(Text.translatable(
                                                    "color" +
                                                            ".minecraft.magenta").formatted(Formatting.LIGHT_PURPLE)).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.LIGHT_PURPLE);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.open();
                                }
                            }
                        }
                    }));

                    teamColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openGameOptionsGui(player)));

                    teamColorGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
        }
    }

}
