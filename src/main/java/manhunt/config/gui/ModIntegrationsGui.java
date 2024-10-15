package manhunt.config.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.ManhuntGame;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.List;

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

        var vanillaBool = boolvalue;
        modIntegrationsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("integration.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> openVanillaIntegrationGui(player, type, vanillaBool)));
        slot++;

        loreList = new ArrayList<>();
        name = "chunky";
        item = Items.NETHER_STAR;
        boolvalue = ManhuntConfig.CONFIG.isChunky();

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

        var chunkyBool = boolvalue;
        modIntegrationsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("integration.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> openChunkyIntegrationGui(player, type, chunkyBool)));

        modIntegrationsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            ConfigGui.playUISound(player);
            ConfigGui.openConfigGui(player);
        }));

        modIntegrationsGui.open();
    }

    private static void openVanillaIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.CONFIG.setVanilla(ManhuntConfig.CONFIG.isVanillaDefault());
                    } else {
                        ManhuntConfig.CONFIG.setVanilla(!bool);
                    }
                    openModIntegrationsGui(player);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var vanillaIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                vanillaIntegrationGui.setTitle(Text.translatable("integration.manhunt.vanilla"));

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

                if (difficulty == Difficulty.EASY) {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN), Text.translatable(
                                    "options.difficulty.normal"), Text.translatable("options.difficulty" + ".hard")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (difficulty == Difficulty.NORMAL) {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy"),
                            Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW),
                            Text.translatable(
                                    "options.difficulty.hard")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    loreList.add(Text.translatable("lore.manhunt.triple",
                            Text.translatable("options.difficulty.easy"),
                            Text.translatable("options.difficulty" + ".normal"), Text.translatable("options" +
                                    ".difficulty"
                                    + ".hard").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                vanillaIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable(name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.CONFIG.setDifficulty(ManhuntConfig.CONFIG.getDifficultyDefault());
                                    } else {
                                        if (difficulty == Difficulty.EASY) {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.NORMAL);
                                        } else if (difficulty == Difficulty.NORMAL) {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.HARD);
                                        } else {
                                            ManhuntConfig.CONFIG.setDifficulty(Difficulty.EASY);
                                        }
                                    }
                                    openVanillaIntegrationGui(player, clickType, bool);
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

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 5632 && intvalue != 11776 && intvalue != 59999968) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 5632) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"),
                                Text.literal(
                                        "Maximum")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 11776) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1st ring"), Text.literal(
                                "2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1st ring"), Text.literal(
                                "2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                int worldBorderInt = intvalue;
                vanillaIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable("setting" + ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                        ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.CONFIG.setWorldBorder(ManhuntConfig.CONFIG.getWorldBorderDefault());
                                        } else {
                                            if (worldBorderInt != 5632 && worldBorderInt != 11776) {
                                                ManhuntConfig.CONFIG.setWorldBorder(5632);
                                            } else {
                                                if (worldBorderInt == 5632) {
                                                    ManhuntConfig.CONFIG.setWorldBorder(11776);
                                                } else {
                                                    ManhuntConfig.CONFIG.setWorldBorder(59999968);
                                                }
                                            }
                                        }
                                        openVanillaIntegrationGui(player, clickType, bool);
                                    } else {
                                        AnvilInputGui worldBorderGui = new AnvilInputGui(player, false) {
                                            @Override
                                            public void onInput(String input) {
                                                this.setSlot(2,
                                                        new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                            int value = worldBorderInt;
                                                            try {
                                                                value = Integer.parseInt(input);
                                                            } catch (NumberFormatException e) {
                                                                player.sendMessage(Text.translatable("chat.manhunt" + ".invalid_input"
                                                                ).formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.CONFIG.setWorldBorder(value);
                                                            openVanillaIntegrationGui(player, clickType, bool);
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

                if (intvalue != 0 && intvalue != 5 && intvalue != 10) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("0").formatted(Formatting.RED), Text.literal("5"), Text.literal("10")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 5) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("0"),
                                Text.literal("5").formatted(Formatting.YELLOW), Text.literal("10")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("0"), Text.literal("5"),
                                Text.literal("10").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                int spawnRadiusInt = intvalue;
                vanillaIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable("setting" + ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.CONFIG.setSpawnRadius(ManhuntConfig.CONFIG.getSpawnRadiusDefault());
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
                                        openVanillaIntegrationGui(player, clickType, bool);
                                    } else {
                                        AnvilInputGui spawnRadiusGui = new AnvilInputGui(player, false) {
                                            @Override
                                            public void onInput(String input) {
                                                this.setSlot(2,
                                                        new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                            int value = spawnRadiusInt;
                                                            try {
                                                                value = Integer.parseInt(input);
                                                            } catch (NumberFormatException e) {
                                                                player.sendMessage(Text.translatable("chat.manhunt" + ".invalid_input"
                                                                ).formatted(Formatting.RED));
                                                            }

                                                            ManhuntConfig.CONFIG.setSpawnRadius(value);
                                                            openVanillaIntegrationGui(player, clickType, bool);
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

                if (boolvalue) {
                    loreList.add(Text.translatable("lore.manhunt.double",
                            Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore" +
                                    ".manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                var spectatorsGenerateChunksBool = boolvalue;
                vanillaIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable("setting" + ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                        ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.CONFIG.setSpectatorsGenerateChunks(ManhuntConfig.CONFIG.isSpectatorsGenerateChunksDefault());
                                    } else {
                                        ManhuntConfig.CONFIG.setSpectatorsGenerateChunks(!spectatorsGenerateChunksBool);
                                    }
                                    openVanillaIntegrationGui(player, clickType, bool);
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        }));

                vanillaIntegrationGui.setSlot(8,
                        new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openModIntegrationsGui(player)));

                vanillaIntegrationGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

    private static void openChunkyIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.CONFIG.setChunky(ManhuntConfig.CONFIG.isChunkyDefault());
                    } else {
                        ManhuntConfig.CONFIG.setChunky(!bool);
                    }
                    openModIntegrationsGui(player);

                    if (!bool) {
                        ManhuntMod.schedulePreload(player.getServer());
                    } else {
                        if (ManhuntGame.chunkyLoaded) {
                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky cancel");
                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky confirm");
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var chunkyIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                chunkyIntegrationGui.setTitle(Text.translatable("integration.manhunt.chunky"));

                ManhuntConfig.CONFIG.save();
                ConfigGui.playUISound(player);

                List<Text> loreList;
                String name;
                int slot = 0;
                Item item;
                int intvalue;

                loreList = new ArrayList<>();
                name = "flat_world_preset.minecraft.overworld";
                item = Items.GRASS_BLOCK;
                intvalue = ManhuntConfig.CONFIG.getOverworld();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 2000 && intvalue != 4000 && intvalue != 8000) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 2000) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("2000").formatted(Formatting.RED), Text.literal("4000"), Text.literal(
                                        "8000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 4000) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("2000"), Text.literal(
                                "4000").formatted(Formatting.YELLOW), Text.literal("8000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("2000"), Text.literal(
                                "4000"), Text.literal("8000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var overworldInt = intvalue;
                chunkyIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable(name)).setLore(loreList).setCallback((index,
                                                                                                                    type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                                if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.CONFIG.setOverworld(ManhuntConfig.CONFIG.getOverworldDefault());
                                        } else {
                                            if (overworldInt != 2000 && overworldInt != 4000) {
                                                ManhuntConfig.CONFIG.setOverworld(2000);
                                            } else {
                                                if (overworldInt == 2000) {
                                                    ManhuntConfig.CONFIG.setOverworld(4000);
                                                } else {
                                                    ManhuntConfig.CONFIG.setOverworld(8000);
                                                }
                                            }
                                        }
                                        openChunkyIntegrationGui(player, clickType, bool);
                                    } else {
                                        AnvilInputGui overworldGui = new AnvilInputGui(player, false) {
                                            @Override
                                            public void onInput(String input) {
                                                this.setSlot(2,
                                                        new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                            int value = overworldInt;
                                                            try {
                                                                value = Integer.parseInt(input);
                                                            } catch (NumberFormatException e) {
                                                                player.sendMessage(Text.translatable("chat.manhunt" + ".invalid_input"
                                                                ).formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.CONFIG.setOverworld(value);
                                                            openChunkyIntegrationGui(player, clickType, bool);

                                                            if (value == 0) {
                                                                ManhuntMod.schedulePreload(player.getServer());
                                                            } else {
                                                                if (ManhuntGame.chunkyLoaded) {
                                                                    player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky cancel");
                                                                    player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky confirm");
                                                                }
                                                            }
                                                        }));
                                            }
                                        };
                                        overworldGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                        overworldGui.setDefaultInputValue("");
                                        overworldGui.open();
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
                name = "the_nether";
                item = Items.NETHERRACK;
                intvalue = ManhuntConfig.CONFIG.getTheNether();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 250) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal(
                                        "1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 500) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"),
                                Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"),
                                Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var netherInt = intvalue;
                chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item).setName(Text.translatable("world" +
                        ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setTheNether(ManhuntConfig.CONFIG.getTheNetherDefault());
                                } else {
                                    if (netherInt != 250 && netherInt != 500) {
                                        ManhuntConfig.CONFIG.setTheNether(250);
                                    } else {
                                        if (netherInt == 250) {
                                            ManhuntConfig.CONFIG.setTheNether(500);
                                        } else {
                                            ManhuntConfig.CONFIG.setTheNether(1000);
                                        }
                                    }
                                }
                                openChunkyIntegrationGui(player, clickType, bool);
                            } else {
                                AnvilInputGui netherGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    int value = netherInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input"
                                                        ).formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.CONFIG.setTheNether(value);
                                                    openChunkyIntegrationGui(player, clickType, bool);

                                                    if (value == 0) {
                                                        ManhuntMod.schedulePreload(player.getServer());
                                                    } else {
                                                        if (ManhuntGame.chunkyLoaded) {
                                                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky cancel");
                                                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky confirm");
                                                        }
                                                    }
                                                }));
                                    }
                                };
                                netherGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                netherGui.setDefaultInputValue("");
                                netherGui.open();
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
                name = "the_end";
                item = Items.END_STONE;
                intvalue = ManhuntConfig.CONFIG.getTheEnd();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                    loreList.add(Text.translatable("lore.manhunt.single",
                            Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 250) {
                        loreList.add(Text.translatable("lore.manhunt.triple",
                                Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal(
                                        "1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 500) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"),
                                Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"),
                                Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var endInt = intvalue;
                chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item).setName(Text.translatable("world" +
                        ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setTheEnd(ManhuntConfig.CONFIG.getTheEndDefault());
                                } else {
                                    if (endInt != 250 && endInt != 500) {
                                        ManhuntConfig.CONFIG.setOverworld(250);
                                    } else {
                                        if (endInt == 250) {
                                            ManhuntConfig.CONFIG.setTheEnd(500);
                                        } else {
                                            ManhuntConfig.CONFIG.setTheEnd(1000);
                                        }
                                    }
                                }
                                openChunkyIntegrationGui(player, clickType, bool);
                            } else {
                                AnvilInputGui endGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    int value = endInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input"
                                                        ).formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.CONFIG.setTheEnd(value);
                                                    openChunkyIntegrationGui(player, clickType, bool);

                                                    if (value == 0) {
                                                        ManhuntMod.schedulePreload(player.getServer());
                                                    } else {
                                                        if (ManhuntGame.chunkyLoaded) {
                                                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky cancel");
                                                            player.getServer().getCommandManager().executeWithPrefix(player.getServer().getCommandSource(), "chunky confirm");
                                                        }
                                                    }
                                                }));
                                    }
                                };
                                endGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                endGui.setDefaultInputValue("");
                                endGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

                chunkyIntegrationGui.setSlot(8,
                        new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openModIntegrationsGui(player)));

                chunkyIntegrationGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }
}
