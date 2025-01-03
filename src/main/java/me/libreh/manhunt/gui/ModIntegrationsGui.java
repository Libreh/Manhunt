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
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.SPAM_PREVENTION;
import static me.libreh.manhunt.utils.Methods.hasPermission;

public class ModIntegrationsGui {
    public static void openModIntegrationsGui(ServerPlayerEntity player) {
        SimpleGui modIntegrationsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        modIntegrationsGui.setTitle(Text.translatable("config.manhunt.mod_integrations"));

        ManhuntConfig.CONFIG.save();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "vanilla";
        item = Items.GRASS_BLOCK;
        boolvalue = ManhuntConfig.CONFIG.isVanilla();

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

        var vanillaBool = boolvalue;
        modIntegrationsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->
                        openVanillaIntegrationGui(player, type, vanillaBool)));

        modIntegrationsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                }));

        modIntegrationsGui.open();
    }

    private static void openVanillaIntegrationGui(ServerPlayerEntity player, ClickType clickType, boolean vanilla) {
        if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (hasPermission(player, "manhunt.config")) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.CONFIG.setVanilla(ManhuntConfig.vanillaDefault);
                    } else {
                        ManhuntConfig.CONFIG.setVanilla(!vanilla);
                    }
                    openModIntegrationsGui(player);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var vanillaIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                vanillaIntegrationGui.setTitle(Text.translatable("config.manhunt.vanilla"));

                ManhuntConfig.CONFIG.save();
                ConfigGui.playUISound(player);

                List<Text> loreList;
                String name;
                int slot = 0;
                Item item;
                boolean boolvalue;
                int intvalue;

                loreList = new ArrayList<>();
                name = "options.difficulty";
                item = Items.CREEPER_HEAD;
                Difficulty difficulty = ManhuntConfig.CONFIG.getDifficulty();

                loreList.add(Text.empty());
                if (difficulty == Difficulty.EASY) {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN),
                            Text.translatable("options.difficulty.normal"),
                            Text.translatable("options.difficulty.hard")
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (difficulty == Difficulty.NORMAL) {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy"),
                            Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW),
                            Text.translatable("options.difficulty.hard")
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy"),
                            Text.translatable("options.difficulty.normal"),
                            Text.translatable("options.difficulty" + ".hard").formatted(Formatting.RED)
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.empty());
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                        .withColor(Formatting.AQUA).withItalic(false)));

                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable(name).formatted(Formatting.WHITE))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (hasPermission(player, "manhunt.config")) {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.CONFIG.setDifficulty(ManhuntConfig.difficultyDefault);
                                    } else {
                                        if (difficulty == Difficulty.EASY) {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.NORMAL);
                                        } else if (difficulty == Difficulty.NORMAL) {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.HARD);
                                        } else {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.EASY);
                                        }
                                    }

                                    openVanillaIntegrationGui(player, clickType, vanilla);
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        }));
                slot++;

                loreList = new ArrayList<>();
                name = "world_border";
                item = Items.PRISMARINE_WALL;
                intvalue = ManhuntConfig.CONFIG.getWorldBorder();

                loreList.add(Text.empty());
                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 5632 && intvalue != 11776 && intvalue != 59999968) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 5632) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("1st ring").formatted(Formatting.RED),
                                Text.literal("2nd ring"),
                                Text.literal("Maximum")
                        ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 11776) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("1st ring"),
                                Text.literal("2nd ring").formatted(Formatting.YELLOW),
                                Text.literal("Maximum")
                        ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("1st ring"),
                                Text.literal("2nd ring"),
                                Text.literal("Maximum").formatted(Formatting.GREEN)
                        ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.empty());
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                        .withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style
                        .withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                int worldBorder = intvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("config.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                                SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                            if (hasPermission(player, "manhunt.config")) {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.CONFIG.setWorldBorder(ManhuntConfig.worldBorderDefault);
                                        } else {
                                            if (worldBorder != 5632 && worldBorder != 11776) {
                                                ManhuntConfig.CONFIG.setWorldBorder(5632);
                                            } else {
                                                if (worldBorder == 5632) {
                                                    ManhuntConfig.CONFIG.setWorldBorder(11776);
                                                } else {
                                                    ManhuntConfig.CONFIG.setWorldBorder(59999968);
                                                }
                                            }
                                        }

                                        openVanillaIntegrationGui(player, clickType, vanilla);
                                    } else {
                                        AnvilInputGui worldBorderGui = new AnvilInputGui(player, false) {
                                            @Override
                                            public void onInput(String input) {
                                                this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                        .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                        .setCallback(() -> {
                                                            int value = worldBorder;
                                                            try {
                                                                value = Integer.parseInt(input);
                                                            } catch (NumberFormatException e) {
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input")
                                                                        .formatted(Formatting.RED));
                                                            }

                                                            ManhuntConfig.CONFIG.setWorldBorder(value);
                                                            openVanillaIntegrationGui(player, clickType, vanilla);
                                                        }));
                                            }
                                        };

                                        worldBorderGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                        worldBorderGui.setDefaultInputValue("");
                                        worldBorderGui.open();
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
                name = "spawn_radius";
                item = Items.BEDROCK;
                intvalue = ManhuntConfig.CONFIG.getSpawnRadius();

                loreList.add(Text.empty());
                if (intvalue != 0 && intvalue != 5 && intvalue != 10) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)
                    ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("0").formatted(Formatting.RED),
                                Text.literal("5"),
                                Text.literal("10")
                        ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 5) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("0"),
                                Text.literal("5").formatted(Formatting.YELLOW),
                                Text.literal("10")
                        ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("0"),
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

                int spawnRadiusInt = intvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("config.manhunt." + name))
                        .setLore(loreList).setCallback((index, type, action) -> {
                            if (hasPermission(player, "manhunt.config")) {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.CONFIG.setSpawnRadius(ManhuntConfig.spawnRadiusDefault);
                                        } else {
                                            if (spawnRadiusInt != 0 && spawnRadiusInt != 5) {
                                                ManhuntConfig.CONFIG.setSpawnRadius(0);
                                            } else {
                                                if (spawnRadiusInt == 0) {
                                                    ManhuntConfig.CONFIG.setSpawnRadius(5);
                                                } else {
                                                    ManhuntConfig.CONFIG.setSpawnRadius(10);
                                                }
                                            }
                                        }

                                        openVanillaIntegrationGui(player, clickType, vanilla);
                                    } else {
                                        AnvilInputGui spawnRadiusGui = new AnvilInputGui(player, false) {
                                            @Override
                                            public void onInput(String input) {
                                                this.setSlot(2, new GuiElementBuilder(Items.PAPER).setName(Text.literal(input)
                                                        .formatted(Formatting.ITALIC)).setCallback(() -> {
                                                            int value = spawnRadiusInt;
                                                            try {
                                                                value = Integer.parseInt(input);
                                                            } catch (NumberFormatException e) {
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input")
                                                                        .formatted(Formatting.RED));
                                                            }

                                                            ManhuntConfig.CONFIG.setSpawnRadius(value);
                                                            openVanillaIntegrationGui(player, clickType, vanilla);
                                                        }));
                                            }
                                        };

                                        spawnRadiusGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                        spawnRadiusGui.setDefaultInputValue("");
                                        spawnRadiusGui.open();
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
                name = "spectators_generate_chunks";
                item = Items.SNOW_BLOCK;
                boolvalue = ManhuntConfig.CONFIG.isSpectatorsGenerateChunks();

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

                var spectatorsGenerateChunks = boolvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("config.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                                SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                            if (hasPermission(player, "manhunt.config")) {
                                if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.CONFIG.setSpectatorsGenerateChunks(ManhuntConfig.spectatorsGenerateChunksDefault);
                                    } else {
                                        ManhuntConfig.CONFIG.setSpectatorsGenerateChunks(!spectatorsGenerateChunks);
                                    }

                                    openVanillaIntegrationGui(player, clickType, vanilla);
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        }));

                vanillaIntegrationGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                        .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                        .setCallback(() -> openModIntegrationsGui(player)));

                vanillaIntegrationGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }
}
