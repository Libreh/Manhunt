package libreh.manhunt.config.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class GlobalSettingsGui {
    public static void openGlobalSettingsGui(ServerPlayerEntity player) {
        SimpleGui globalSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

        globalSettingsGui.setTitle(Text.translatable("config.manhunt.global_settings"));

        ManhuntConfig.CONFIG.save();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;

        loreList = new ArrayList<>();
        name = "custom_titles";
        item = Items.OAK_SIGN;
        boolvalue = ManhuntConfig.CONFIG.isCustomTitles();

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

        var gameTitlesBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomTitles(ManhuntConfig.CONFIG.isCustomTitlesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomTitles(!gameTitlesBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.GOAT_HORN;
        boolvalue = ManhuntConfig.CONFIG.isCustomSounds();

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

        var manhuntSoundsBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomSounds(ManhuntConfig.CONFIG.isCustomSoundsDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomSounds(!manhuntSoundsBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_particles";
        item = Items.BLAZE_POWDER;
        boolvalue = ManhuntConfig.CONFIG.isCustomParticles();

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

        var customParticlesBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomParticles(ManhuntConfig.CONFIG.isCustomParticlesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomParticles(!customParticlesBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "tracker_type";
        item = Items.COMPASS;
        intvalue = ManhuntConfig.CONFIG.getTrackerType();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.per_player")).styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.tracker_type" +
                    ".manual_tracking")).styled(style -> style.withColor(Formatting.RED).withItalic(false)));
        } else if (intvalue == 3) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.tracker_type" +
                    ".automatic_when_holding")).styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.tracker_type" +
                    ".always_automatic")).styled(style -> style.withColor(Formatting.GREEN).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openTrackerTypeGui(player, type)));
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        boolvalue = ManhuntConfig.CONFIG.isNightVision();

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

        var nightVisionBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setNightVision(ManhuntConfig.CONFIG.isNightVisionDefault());
                            } else {
                                ManhuntConfig.CONFIG.setNightVision(!nightVisionBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        intvalue = ManhuntConfig.CONFIG.getFriendlyFire();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always"
            ).formatted(Formatting.GREEN), Text.translatable("lore.manhunt.per_player"), Text.translatable("lore" +
                    ".manhunt." + name + ".never")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always"
            ), Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW), Text.translatable("lore" +
                    ".manhunt." + name + ".never")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always"
                    ), Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt." + name + ".never").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        int friendlyFireInt = intvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setFriendlyFire(ManhuntConfig.CONFIG.getFriendlyFireDefault());
                            } else {
                                if (friendlyFireInt == 1) {
                                    ManhuntConfig.CONFIG.setFriendlyFire(2);
                                } else if (friendlyFireInt == 2) {
                                    ManhuntConfig.CONFIG.setFriendlyFire(3);
                                } else {
                                    ManhuntConfig.CONFIG.setFriendlyFire(1);
                                }
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "runner_settings";
        item = Items.WRITABLE_BOOK;
        boolvalue = ManhuntConfig.CONFIG.isRunnerPreferences();

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

        var runnerPreferencesBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnerPreferences(ManhuntConfig.CONFIG.isRunnerPreferencesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnerPreferences(!runnerPreferencesBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        boolvalue = ManhuntConfig.CONFIG.isBedExplosions();

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

        var bedExplosionsBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setBedExplosions(ManhuntConfig.CONFIG.isBedExplosionsDefault());
                            } else {
                                ManhuntConfig.CONFIG.setBedExplosions(!bedExplosionsBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        globalSettingsGui.setSlot(8,
                new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" + ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        boolvalue = ManhuntConfig.CONFIG.isLavaPvpInNether();

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

        var lavaPvpInNetherBool = boolvalue;
        globalSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setLavaPvpInNether(ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());
                            } else {
                                ManhuntConfig.CONFIG.setLavaPvpInNether(!lavaPvpInNetherBool);
                            }
                            openGlobalSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

        globalSettingsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        globalSettingsGui.open();
    }

    private static void openTrackerTypeGui(ServerPlayerEntity player, ClickType clickType) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
            if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                if (clickType == ClickType.DROP) {
                    ManhuntConfig.CONFIG.setTrackerType(ManhuntConfig.CONFIG.getTrackerTypeDefault());
                    openGlobalSettingsGui(player);
                } else {
                    var trackerTypeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    trackerTypeGui.setTitle(Text.translatable("config.manhunt.team_color"));

                    ConfigGui.playUISound(player);

                    trackerTypeGui.setSlot(0, new GuiElementBuilder(Items.COMPARATOR).setName(Text.translatable("lore"
                            + ".manhunt.per_player").formatted(Formatting.YELLOW)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setTrackerType(1);
                        openGlobalSettingsGui(player);
                    }));

                    trackerTypeGui.setSlot(1,
                            new GuiElementBuilder(Items.REDSTONE).setName(Text.translatable("lore" + ".manhunt" +
                                    ".tracker_type" +
                                    ".manual_tracking").formatted(Formatting.RED)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setTrackerType(2);
                                openGlobalSettingsGui(player);
                            }));

                    trackerTypeGui.setSlot(2,
                            new GuiElementBuilder(Items.COMPASS).setName(Text.translatable("lore" + ".manhunt" +
                                    ".tracker_type" +
                                    ".automatic_when_holding").formatted(Formatting.GOLD)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setTrackerType(3);
                                openGlobalSettingsGui(player);
                            }));

                    trackerTypeGui.setSlot(3,
                            new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable("lore.manhunt" +
                                    ".tracker_type.always_automatic").formatted(Formatting.GREEN)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setTrackerType(4);
                                openGlobalSettingsGui(player);
                            }));

                    trackerTypeGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openGlobalSettingsGui(player)));

                    trackerTypeGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
        }
    }
}
