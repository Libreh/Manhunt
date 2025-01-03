package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.libreh.manhunt.config.ManhuntConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Constants.SPAM_PREVENTION;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class GameOptionsGui {
    public static void openGameOptionsGui(ServerPlayerEntity player) {
        SimpleGui gameOptionsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        gameOptionsGui.setTitle(Text.translatable("config.manhunt.game_options"));

        CONFIG.save();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;
        String stringvalue;

        loreList = new ArrayList<>();
        name = "preload_distance";
        item = Items.GLASS;
        intvalue = CONFIG.getPreloadDistance();

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (intvalue != 0 && intvalue != 5 && intvalue != 10) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN))
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 0) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off").formatted(Formatting.RED),
                        Text.literal("5"),
                        Text.literal("10")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 5) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off"),
                        Text.literal("5").formatted(Formatting.YELLOW),
                        Text.literal("10")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off"),
                        Text.literal("5"),
                        Text.literal("10").formatted(Formatting.GREEN)
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop")
                .styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift")
                .styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int preloadDistance = intvalue;
        gameOptionsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    CONFIG.setPreloadDistance(ManhuntConfig.preloadDistanceDefault);
                                } else {
                                    if (preloadDistance != 0 && preloadDistance != 5) {
                                        CONFIG.setPreloadDistance(0);
                                    } else {
                                        if (preloadDistance == 0) {
                                            CONFIG.setPreloadDistance(5);
                                        } else {
                                            CONFIG.setPreloadDistance(10);
                                        }
                                    }
                                }

                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui preloadDistanceGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = preloadDistance;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt.invalid_input")
                                                                .formatted(Formatting.RED));
                                                    }

                                                    CONFIG.setPreloadDistance(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };

                                preloadDistanceGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                preloadDistanceGui.setDefaultInputValue("");
                                preloadDistanceGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "preset_mode";
        item = Items.FLETCHING_TABLE;
        stringvalue = CONFIG.getPresetMode();

        Formatting formatting;
        switch (stringvalue) {
            case "free_select" -> formatting = Formatting.WHITE;
            case "equal_split" -> formatting = Formatting.BLUE;
            case "speedrun_showdown" -> formatting = Formatting.GREEN;
            case "runner_cycle" -> formatting = Formatting.YELLOW;
            case "hunter_infection" -> formatting = Formatting.RED;
            default -> formatting = Formatting.DARK_RED;
        }
        Formatting finalFormatting = formatting;
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.preset_mode." + stringvalue))
                .styled(style -> style.withColor(finalFormatting).withItalic(false)));
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop")
                .styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        gameOptionsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamPresetGui(player, type)));
        slot++;

        loreList = new ArrayList<>();
        name = "team_color";
        item = Items.WHITE_BANNER;
        boolvalue = CONFIG.isTeamColor();

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.off")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style
                .withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var teamColor = boolvalue;
        gameOptionsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamColorGui(player, type, teamColor)));
        slot++;

        loreList = new ArrayList<>();
        name = "head_start";
        item = Items.GOLDEN_BOOTS;
        intvalue = CONFIG.getHeadStartSec();

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (intvalue != 0 && intvalue != 5 && intvalue != 10) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN))
                    .styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 0) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off").formatted(Formatting.RED),
                        Text.literal("5"), Text.literal("10")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 5) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off"),
                        Text.literal("5").formatted(Formatting.YELLOW),
                        Text.literal("10")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.translatable("lore.manhunt.off"),
                        Text.literal("5"),
                        Text.literal("10").formatted(Formatting.GREEN)
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style
                .withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int headStart = intvalue;
        gameOptionsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList).hideDefaultTooltip()
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    CONFIG.setHeadStartSec(ManhuntConfig.headStartSecDefault);
                                } else {
                                    if (headStart != 0 && headStart != 5) {
                                        CONFIG.setHeadStartSec(0);
                                    } else {
                                        if (headStart == 0) {
                                            CONFIG.setHeadStartSec(5);
                                        } else {
                                            CONFIG.setHeadStartSec(10);
                                        }
                                    }
                                }

                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui headStartGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = headStart;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt.invalid_input")
                                                                .formatted(Formatting.RED));
                                                    }

                                                    CONFIG.setHeadStartSec(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };

                                headStartGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                headStartGui.setDefaultInputValue("");
                                headStartGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "time_limit";
        item = Items.CLOCK;
        intvalue = CONFIG.getTimeLimitMin();

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 90 && intvalue != 180 && intvalue != 270) {
            loreList.add(Text.translatable("lore.manhunt.single",
                    Text.literal(String.valueOf(CONFIG.getTimeLimitMin())).formatted(Formatting.GREEN)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 90) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.literal("90").formatted(Formatting.RED),
                        Text.literal("180"),
                        Text.literal("270")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 180) {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.literal("90"),
                        Text.literal("180").formatted(Formatting.YELLOW),
                        Text.literal("270")
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple",
                        Text.literal("90"),
                        Text.literal("180"),
                        Text.literal("270").formatted(Formatting.GREEN)
                ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style
                .withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int timeLimit = intvalue;
        gameOptionsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    CONFIG.setTimeLimitMin(ManhuntConfig.timeLimitMinDefault);
                                } else {
                                    if (timeLimit != 90 && timeLimit != 180) {
                                        CONFIG.setTimeLimitMin(90);
                                    } else {
                                        if (timeLimit == 90) {
                                            CONFIG.setTimeLimitMin(180);
                                        } else {
                                            CONFIG.setTimeLimitMin(270);
                                        }
                                    }
                                }

                                openGameOptionsGui(player);
                            } else {
                                AnvilInputGui timeLimitGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = timeLimit;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt.invalid_input")
                                                                .formatted(Formatting.RED));
                                                    }

                                                    CONFIG.setTimeLimitMin(value);
                                                    openGameOptionsGui(player);
                                                }));
                                    }
                                };

                                timeLimitGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                timeLimitGui.setDefaultInputValue("");
                                timeLimitGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        gameOptionsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                }));

        gameOptionsGui.open();
    }

    private static void openTeamPresetGui(ServerPlayerEntity player, ClickType clickType) {
        if (SPAM_PREVENTION.get(player.getUuid()) < 12)
            SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
        if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
            if (hasPermission(player, "manhunt.config")) {
                if (clickType == ClickType.DROP) {
                    CONFIG.setPresetMode(ManhuntConfig.presetModeDefault);
                    openGameOptionsGui(player);
                } else {
                    var presetModeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    presetModeGui.setTitle(Text.translatable("config.manhunt.team_color"));

                    ConfigGui.playUISound(player);

                    presetModeGui.setSlot(0, new GuiElementBuilder(Items.GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.free_select"))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("free_select");
                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(1, new GuiElementBuilder(Items.CYAN_STAINED_GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.equal_split").formatted(Formatting.AQUA))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("equal_split");

                                equalSplit();

                                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.equal_split",
                                        Text.translatable("role.manhunt.hunters").formatted(CONFIG.getHuntersColor()),
                                        Text.translatable("role.manhunt.runners").formatted(CONFIG.getRunnersColor())),
                                        false);

                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(2, new GuiElementBuilder(Items.LIME_STAINED_GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.speedrun_showdown").formatted(Formatting.GREEN))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("speedrun_showdown");

                                speedrunShowdown();

                                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                                        Text.literal("Everyone").formatted(CONFIG.getRunnersColor()),
                                        Text.translatable("role.manhunt.runner").formatted(CONFIG.getRunnersColor())),
                                        false);

                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(3, new GuiElementBuilder(Items.YELLOW_STAINED_GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.runner_cycle").formatted(Formatting.YELLOW))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("runner_cycle");

                                runnerCycle();

                                String runnerName = RUNNERS_TEAM.getPlayerList().iterator().next();

                                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                        Text.literal(runnerName).formatted(CONFIG.getRunnersColor()),
                                        Text.translatable("role.manhunt.runner").formatted(CONFIG.getRunnersColor())), false);

                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(4, new GuiElementBuilder(Items.RED_STAINED_GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.hunter_infection").formatted(Formatting.RED))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("hunter_infection");

                                hunterInfection();

                                String hunterName = HUNTERS_TEAM.getPlayerList().iterator().next();

                                SERVER.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                        Text.literal(hunterName).formatted(CONFIG.getHuntersColor()),
                                        Text.translatable("role.manhunt.hunter").formatted(CONFIG.getHuntersColor())),
                                        false);

                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(5, new GuiElementBuilder(Items.TINTED_GLASS)
                            .setName(Text.translatable("lore.manhunt.preset_mode.no_selection").formatted(Formatting.DARK_RED))
                            .setCallback(() -> {
                                CONFIG.setPresetMode("no_selection");
                                openGameOptionsGui(player);
                            }));

                    presetModeGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                            .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                            .setCallback(() -> openGameOptionsGui(player)));

                    presetModeGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Boolean boolvalue) {
        if (notSpamming(player)) {
            if (hasPermission(player, "manhunt.config")) {
                if (!clickType.shift) {
                    if (clickType == ClickType.DROP) {
                        CONFIG.setTeamColor(ManhuntConfig.teamColorDefault);
                    } else {
                        CONFIG.setTeamColor(!boolvalue);

                        if (CONFIG.isTeamColor()) {
                            player.getScoreboard().getTeam("hunters").setColor(CONFIG.getHuntersColor());
                            player.getScoreboard().getTeam("runners").setColor(CONFIG.getRunnersColor());
                        } else {
                            player.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
                            player.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
                        }
                    }

                    openGameOptionsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    teamColorGui.setTitle(Text.translatable("config.manhunt.team_color"));

                    CONFIG.save();
                    ConfigGui.playUISound(player);

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.empty());
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("color.minecraft." + CONFIG.getHuntersColor().name().toLowerCase())
                                    .formatted(CONFIG.getHuntersColor())
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    loreList.add(Text.empty());
                    loreList.add(Text.translatable("lore.manhunt.click_drop")
                            .styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                            .setName(Text.translatable("config.manhunt.hunters_color").formatted(CONFIG.getHuntersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (hasPermission(player, "manhunt.config")) {
                                        if (type == ClickType.DROP) {
                                            CONFIG.setHuntersColor(ManhuntConfig.huntersColorDefault);

                                            openGameOptionsGui(player);
                                        } else {
                                            var hunters = HUNTERS_TEAM;

                                            var huntersColorGui = new SimpleGui(
                                                    ScreenHandlerType.GENERIC_9X2, player, false);

                                            huntersColorGui.setTitle(Text.translatable("config.manhunt.hunters_color"));

                                            CONFIG.save();
                                            ConfigGui.playUISound(player);

                                            huntersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.RESET);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    })
                                            );

                                            huntersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray")
                                                            .formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.GRAY);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray")
                                                            .formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.DARK_GRAY);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black")
                                                            .formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.BLACK);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red")
                                                            .formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.RED);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange")
                                                            .formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.GOLD);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow")
                                                            .formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.YELLOW);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.manhunt.go_back")
                                                            .formatted(Formatting.WHITE))
                                                    .setCallback(() -> openGameOptionsGui(player)));

                                            huntersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime")
                                                            .formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.GREEN);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green")
                                                            .formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.DARK_GREEN);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan")
                                                            .formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.DARK_AQUA);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue")
                                                            .formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.BLUE);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue")
                                                            .formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.DARK_BLUE);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple")
                                                            .formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.DARK_PURPLE);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta")
                                                            .formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        CONFIG.setHuntersColor(Formatting.LIGHT_PURPLE);
                                                        hunters.setColor(CONFIG.getHuntersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            huntersColorGui.open();
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                                    }
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            }));

                    loreList = new ArrayList<>();

                    loreList.add(Text.empty());
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("color.minecraft." + CONFIG.getRunnersColor().name().toLowerCase())
                                    .formatted(CONFIG.getRunnersColor())
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    loreList.add(Text.empty());
                    loreList.add(Text.translatable("lore.manhunt.click_drop")
                            .styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK)
                            .setName(Text.translatable("config.manhunt.runners_color")
                                    .formatted(CONFIG.getRunnersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (hasPermission(player, "manhunt.config")) {
                                        if (type == ClickType.DROP) {
                                            CONFIG.setRunnersColor(ManhuntConfig.runnersColorDefault);
                                            ConfigGui.playUISound(player);
                                            openGameOptionsGui(player);
                                        } else {
                                            var runners = RUNNERS_TEAM;

                                            var runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                            runnersColorGui.setTitle(Text.translatable("config.manhunt.runners_color"));

                                            CONFIG.save();
                                            ConfigGui.playUISound(player);

                                            runnersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.RESET);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray")
                                                            .formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.GRAY);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray")
                                                            .formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.DARK_GRAY);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black")
                                                            .formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.BLACK);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red")
                                                            .formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.RED);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange")
                                                            .formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.GOLD);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow")
                                                            .formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.YELLOW);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.manhunt.go_back")
                                                            .formatted(Formatting.WHITE))
                                                    .setCallback(() -> openGameOptionsGui(player)));

                                            runnersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime")
                                                            .formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.GREEN);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green")
                                                            .formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.DARK_GREEN);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan")
                                                            .formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.DARK_AQUA);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue")
                                                            .formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.BLUE);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue")
                                                            .formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.DARK_BLUE);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple")
                                                            .formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.DARK_PURPLE);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta")
                                                            .formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        CONFIG.setRunnersColor(Formatting.LIGHT_PURPLE);
                                                        runners.setColor(CONFIG.getRunnersColor());
                                                        openGameOptionsGui(player);
                                                    }));

                                            runnersColorGui.open();
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                                    }
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            }));

                    teamColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                            .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                            .setCallback(() -> openGameOptionsGui(player)));

                    teamColorGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

}
