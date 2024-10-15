package manhunt.config.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.config.ManhuntConfig;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SettingsGui {
    public static final HashMap<UUID, Boolean> CUSTOM_TITLES = new HashMap<>();
    public static final HashMap<UUID, Boolean> CUSTOM_SOUNDS = new HashMap<>();
    public static final HashMap<UUID, Boolean> CUSTOM_PARTICLES = new HashMap<>();
    public static final HashMap<UUID, Integer> TRACKER_TYPE = new HashMap<>();
    public static final HashMap<UUID, Boolean> NIGHT_VISION = new HashMap<>();
    public static final HashMap<UUID, Boolean> FRIENDLY_FIRE = new HashMap<>();
    public static final HashMap<UUID, Boolean> BED_EXPLOSIONS = new HashMap<>();
    public static final HashMap<UUID, Boolean> LAVA_PVP_IN_NETHER = new HashMap<>();

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        settingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "player_settings";
        item = Items.PLAYER_HEAD;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(11,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false))).setLore(loreList).setSkullOwner(player.getGameProfile(), player.getServer()).setCallback((index, type, action) -> openPlayerSettingsGui(player)));

        loreList = new ArrayList<>();
        name = "runner_settings";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(15,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openRunnerSettingsGui(player)));

        settingsGui.open();
    }

    public static void openPlayerSettingsGui(ServerPlayerEntity player) {
        SimpleGui playerSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        playerSettingsGui.setTitle(Text.translatable("config.manhunt.player_settings"));

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
        boolvalue = CUSTOM_TITLES.get(player.getUuid());

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

        boolean customTitlesBool = boolvalue;
        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_TITLES.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomTitlesDefault());
                        } else {
                            CUSTOM_TITLES.put(player.getUuid(), !customTitlesBool);
                        }
                        openPlayerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.GOAT_HORN;
        boolvalue = CUSTOM_SOUNDS.get(player.getUuid());

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

        boolean customSoundsBool = boolvalue;
        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_SOUNDS.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomSoundsDefault());
                        } else {
                            CUSTOM_SOUNDS.put(player.getUuid(), !customSoundsBool);
                        }
                        openPlayerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_particles";
        item = Items.BLAZE_POWDER;
        boolvalue = CUSTOM_PARTICLES.get(player.getUuid());

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

        boolean customParticlesBool = boolvalue;
        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_PARTICLES.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomParticlesDefault());
                        } else {
                            CUSTOM_PARTICLES.put(player.getUuid(), !customParticlesBool);
                        }
                        openPlayerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "tracker_type";
        item = Items.COMPASS;
        intvalue = TRACKER_TYPE.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 2) {
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

        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> openTrackerTypeGui(player, type)));
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GOLDEN_CARROT;
        boolvalue = NIGHT_VISION.get(player.getUuid());

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

        boolean nightVisionBool = boolvalue;
        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            NIGHT_VISION.put(player.getUuid(), false);
                        } else {
                            NIGHT_VISION.put(player.getUuid(), !nightVisionBool);
                            var nightVision = NIGHT_VISION.get(player.getUuid());
                            if (nightVision) {
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                                        StatusEffectInstance.INFINITE, 255, false, false, true));
                            } else {
                                player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                            }
                        }
                        openPlayerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        boolvalue = FRIENDLY_FIRE.get(player.getUuid());

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

        boolean friendlyFireBool = boolvalue;
        playerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            FRIENDLY_FIRE.put(player.getUuid(), true);
                        } else {
                            FRIENDLY_FIRE.put(player.getUuid(), !friendlyFireBool);
                        }
                        openPlayerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        playerSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            ConfigGui.playUISound(player);
            openSettingsGui(player);
        }));

        playerSettingsGui.open();
    }

    private static void openTrackerTypeGui(ServerPlayerEntity player, ClickType clickType) {
        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (clickType == ClickType.DROP) {
                ManhuntConfig.CONFIG.setTrackerType(4);
                openSettingsGui(player);
            } else {
                var trackerTypeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                trackerTypeGui.setTitle(Text.translatable("config.manhunt.team_color"));

                ConfigGui.playUISound(player);

                trackerTypeGui.setSlot(0, new GuiElementBuilder(Items.REDSTONE).setName(Text.translatable("lore" +
                        ".manhunt.tracker_type.manual_tracking").formatted(Formatting.RED)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 2);
                    openSettingsGui(player);
                }));

                trackerTypeGui.setSlot(1, new GuiElementBuilder(Items.COMPASS).setName(Text.translatable("lore" +
                        ".manhunt.tracker_type.automatic_when_holding").formatted(Formatting.GOLD)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 3);
                    openSettingsGui(player);
                }));

                trackerTypeGui.setSlot(2, new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable(
                        "lore.manhunt.tracker_type.always_automatic").formatted(Formatting.GREEN)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 4);
                    openSettingsGui(player);
                }));

                trackerTypeGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                        ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> GlobalSettingsGui.openGlobalSettingsGui(player)));

                trackerTypeGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

    public static void openRunnerSettingsGui(ServerPlayerEntity player) {
        SimpleGui runnerSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        runnerSettingsGui.setTitle(Text.translatable("config.manhunt.runner_settings"));

        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean value;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        value = BED_EXPLOSIONS.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = value;
        runnerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            BED_EXPLOSIONS.put(player.getUuid(), ManhuntConfig.CONFIG.isBedExplosionsDefault());
                        } else {
                            BED_EXPLOSIONS.put(player.getUuid(), !bedExplosionsBool);
                        }
                        openRunnerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        value = LAVA_PVP_IN_NETHER.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("lore.manhunt.double",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt" +
                            ".off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = value;
        runnerSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            LAVA_PVP_IN_NETHER.put(player.getUuid(), ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());
                        } else {
                            LAVA_PVP_IN_NETHER.put(player.getUuid(), !lavaPvpInNetherBool);
                        }
                        openRunnerSettingsGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        runnerSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            ConfigGui.playUISound(player);
            openSettingsGui(player);
        }));

        runnerSettingsGui.open();
    }
}
