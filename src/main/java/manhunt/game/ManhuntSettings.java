package manhunt.game;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.*;

import static manhunt.ManhuntMod.*;

public class ManhuntSettings {
    public static final HashMap<UUID, Boolean> CUSTOM_TITLES = new HashMap<>();
    public static final HashMap<UUID, Boolean> CUSTOM_SOUNDS = new HashMap<>();
    public static final HashMap<UUID, Boolean> CUSTOM_PARTICLES = new HashMap<>();
    public static final HashMap<UUID, Integer> TRACKER_TYPE = new HashMap<>();
    public static final HashMap<UUID, Boolean> NIGHT_VISION = new HashMap<>();
    public static final HashMap<UUID, Boolean> FRIENDLY_FIRE = new HashMap<>();
    public static final HashMap<UUID, Boolean> BED_EXPLOSIONS = new HashMap<>();
    public static final HashMap<UUID, Boolean> LAVA_PVP_IN_NETHER = new HashMap<>();
    public static final HashMap<UUID, Integer> SLOW_DOWN_MANAGER = new HashMap<>();
    public static List<ServerPlayerEntity> playerList = new ArrayList<>();
    public static UUID mainRunnerUUID;
    public static int mainRunnerTries = 0;

    public static void openPreferencesGui(ServerPlayerEntity player) {
        SimpleGui preferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        preferencesGui.setTitle(Text.translatable("item.manhunt.preferences"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "personal_preferences";
        item = Items.PLAYER_HEAD;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(11,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false))).setLore(loreList).setSkullOwner(player.getGameProfile(), player.getServer()).setCallback((index, type, action) -> openPersonalPreferencesGui(player)));

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(15,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openRunnerPreferencesGui(player)));

        preferencesGui.open();
    }

    public static void openPersonalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("category.manhunt.personal_preferences"));

        ManhuntConfig.CONFIG.save();
        playUISound(player);

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
        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_TITLES.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomTitlesDefault());
                        } else {
                            CUSTOM_TITLES.put(player.getUuid(), !customTitlesBool);
                        }
                        openPersonalPreferencesGui(player);
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
        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_SOUNDS.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomSoundsDefault());
                        } else {
                            CUSTOM_SOUNDS.put(player.getUuid(), !customSoundsBool);
                        }
                        openPersonalPreferencesGui(player);
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
        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            CUSTOM_PARTICLES.put(player.getUuid(), ManhuntConfig.CONFIG.isCustomParticlesDefault());
                        } else {
                            CUSTOM_PARTICLES.put(player.getUuid(), !customParticlesBool);
                        }
                        openPersonalPreferencesGui(player);
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

        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> openTrackerTypePersonal(player, type)));
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
        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            NIGHT_VISION.put(player.getUuid(), false);
                        } else {
                            NIGHT_VISION.put(player.getUuid(), !nightVisionBool);
                        }
                        openPersonalPreferencesGui(player);
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
        personalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            FRIENDLY_FIRE.put(player.getUuid(), true);
                        } else {
                            FRIENDLY_FIRE.put(player.getUuid(), !friendlyFireBool);
                        }
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        personalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            playUISound(player);
            openPreferencesGui(player);
        }));

        personalPreferencesGui.open();
    }

    private static void openTrackerTypePersonal(ServerPlayerEntity player, ClickType clickType) {
        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (clickType == ClickType.DROP) {
                ManhuntConfig.CONFIG.setTrackerType(4);
                openGlobalPreferencesGui(player);
            } else {
                var trackerTypeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                trackerTypeGui.setTitle(Text.translatable("setting.manhunt.team_color"));

                playUISound(player);

                trackerTypeGui.setSlot(0, new GuiElementBuilder(Items.REDSTONE).setName(Text.translatable("lore" +
                        ".manhunt.tracker_type.manual_tracking").formatted(Formatting.RED)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 2);
                    openGlobalPreferencesGui(player);
                }));

                trackerTypeGui.setSlot(1, new GuiElementBuilder(Items.COMPASS).setName(Text.translatable("lore" +
                        ".manhunt.tracker_type.automatic_when_holding").formatted(Formatting.GOLD)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 3);
                    openGlobalPreferencesGui(player);
                }));

                trackerTypeGui.setSlot(2, new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable(
                        "lore.manhunt.tracker_type.always_automatic").formatted(Formatting.GREEN)).setCallback(() -> {
                    TRACKER_TYPE.put(player.getUuid(), 4);
                    openGlobalPreferencesGui(player);
                }));

                trackerTypeGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                        ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openGlobalPreferencesGui(player)));

                trackerTypeGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

    public static void openRunnerPreferencesGui(ServerPlayerEntity player) {
        SimpleGui runnerPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        runnerPreferencesGui.setTitle(Text.translatable("category.manhunt.runner_preferences"));

        playUISound(player);

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
        runnerPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            BED_EXPLOSIONS.put(player.getUuid(), ManhuntConfig.CONFIG.isBedExplosionsDefault());
                        } else {
                            BED_EXPLOSIONS.put(player.getUuid(), !bedExplosionsBool);
                        }
                        openRunnerPreferencesGui(player);
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
        runnerPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            LAVA_PVP_IN_NETHER.put(player.getUuid(), ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());
                        } else {
                            LAVA_PVP_IN_NETHER.put(player.getUuid(), !lavaPvpInNetherBool);
                        }
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        runnerPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            playUISound(player);
            openPreferencesGui(player);
        }));

        runnerPreferencesGui.open();
    }

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        settingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "manhunt_settings";
        item = Items.COMPARATOR;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(10,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false))).setLore(loreList).setSkullOwner(player.getGameProfile(), player.getServer()).setCallback((index, type, action) -> openManhuntSettingsGui(player)));

        loreList = new ArrayList<>();
        name = "global_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(12,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openGlobalPreferencesGui(player)));

        loreList = new ArrayList<>();
        name = "title_texts";
        item = Items.BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(14,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openTitleTextsGui(player)));

        loreList = new ArrayList<>();
        name = "mod_integrations";
        item = Items.PISTON;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(16,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openModIntegrationsGui(player)));

        settingsGui.open();
    }

    public static void openManhuntSettingsGui(ServerPlayerEntity player) {
        SimpleGui manhuntSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

        manhuntSettingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        ManhuntConfig.CONFIG.save();
        playUISound(player);

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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setSetMotd(ManhuntConfig.CONFIG.isSetMotdDefault());
                            } else {
                                ManhuntConfig.CONFIG.setSetMotd(!setMotdBool);
                            }
                            openManhuntSettingsGui(player);
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

        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> openRolePresetGui(player, type)));
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorBool)));
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setWaitForRunner(ManhuntConfig.CONFIG.isWaitForRunnerDefault());
                            } else {
                                ManhuntConfig.CONFIG.setWaitForRunner(!waitForRunnerBool);
                            }
                            openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).hideDefaultTooltip().setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
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
                                openManhuntSettingsGui(player);
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
                                                    openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).hideDefaultTooltip().setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
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
                                openManhuntSettingsGui(player);
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
                                                    openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnersGlow(ManhuntConfig.CONFIG.isRunnersGlowDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnersGlow(!runnersGlowBool);
                            }
                            openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setHuntOnDeath(ManhuntConfig.CONFIG.isHuntOnDeathDefault());
                            } else {
                                ManhuntConfig.CONFIG.setHuntOnDeath(!runnersHuntOnDeathBool);
                            }
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        manhuntSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            playUISound(player);
            openSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnersCanPause(ManhuntConfig.CONFIG.isRunnersCanPauseDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnersCanPause(!runnersCanPauseBool);
                            }
                            openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                            SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                openManhuntSettingsGui(player);
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
                                                    openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                        if (!type.shift) {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                                    SLOW_DOWN_MANAGER.put(player.getUuid(),
                                            SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
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
                                openManhuntSettingsGui(player);
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
                                                openManhuntSettingsGui(player);
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
        manhuntSettingsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setSpectateOnWin(ManhuntConfig.CONFIG.isSpectateOnWinDefault());
                            } else {
                                ManhuntConfig.CONFIG.setSpectateOnWin(!spectateWinBool);
                            }
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

        manhuntSettingsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        manhuntSettingsGui.open();
    }

    private static void openRolePresetGui(ServerPlayerEntity player, ClickType clickType) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
            if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                if (clickType == ClickType.DROP) {
                    ManhuntConfig.CONFIG.setRolePreset(ManhuntConfig.CONFIG.getRolePresetDefault());
                    openManhuntSettingsGui(player);
                } else {
                    var rolePresetGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    rolePresetGui.setTitle(Text.translatable("setting.manhunt.team_color"));

                    playUISound(player);

                    var server = player.server;
                    var scoreboard = player.getScoreboard();

                    rolePresetGui.setSlot(0, new GuiElementBuilder(Items.GLASS).setName(Text.translatable("lore" +
                            ".manhunt.role_preset.free_select")).setCallback(() -> {
                        ManhuntConfig.CONFIG.setRolePreset(1);
                        openManhuntSettingsGui(player);
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
                                openManhuntSettingsGui(player);
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
                                openManhuntSettingsGui(player);
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
                                openManhuntSettingsGui(player);
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
                                openManhuntSettingsGui(player);
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
                                openManhuntSettingsGui(player);
                            }));

                    rolePresetGui.setSlot(6, new GuiElementBuilder(Items.TINTED_GLASS).setName(Text.translatable(
                            "lore.manhunt.role_preset.no_selection").formatted(Formatting.DARK_RED)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setRolePreset(7);
                        openManhuntSettingsGui(player);
                    }));

                    rolePresetGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openManhuntSettingsGui(player)));

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
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
            if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                    openManhuntSettingsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    teamColorGui.setTitle(Text.translatable("setting.manhunt.team_color"));

                    ManhuntConfig.CONFIG.save();
                    playUISound(player);

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.literal(ManhuntConfig.CONFIG.getHuntersColor().name()).styled(style -> style.withColor(ManhuntConfig.CONFIG.getHuntersColor()).withItalic(false)));
                    loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable(
                            "setting.manhunt.hunters_color").formatted(ManhuntConfig.CONFIG.getHuntersColor())).setLore(loreList).setCallback((index, type, action) -> {
                        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                            if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setHuntersColor(ManhuntConfig.CONFIG.getHuntersColorDefault());

                                    openManhuntSettingsGui(player);
                                } else {
                                    var huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                    huntersColorGui.setTitle(Text.translatable("setting.manhunt" + ".hunters_color"));

                                    ManhuntConfig.CONFIG.save();
                                    playUISound(player);

                                    huntersColorGui.setSlot(0,
                                            new GuiElementBuilder(Items.WHITE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.white")).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setHuntersColor(Formatting.RESET);
                                                player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.CONFIG.getHuntersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    huntersColorGui.setSlot(1,
                                            new GuiElementBuilder(Items.LIGHT_GRAY_WOOL).setName(Text.translatable(
                                                    "color" +
                                                            ".minecraft.light_gray").formatted(Formatting.GRAY)).setCallback(() -> {
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
                                                    ".minecraft" +
                                                    ".red").formatted(Formatting.RED)).setCallback(() -> {
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
                                                            ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(teamColorGui::open));

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
                                                    "color" +
                                                            ".minecraft.light_blue").formatted(Formatting.BLUE)).setCallback(() -> {
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

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK).setName(Text.translatable("setting" +
                            ".manhunt.runners_color").formatted(ManhuntConfig.CONFIG.getRunnersColor())).setLore(loreList).setCallback((index, type, action) -> {
                        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                            if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.CONFIG.setRunnersColor(ManhuntConfig.CONFIG.getRunnersColorDefault());
                                    playUISound(player);
                                    openManhuntSettingsGui(player);
                                } else {
                                    var runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                    runnersColorGui.setTitle(Text.translatable("setting.manhunt" + ".runners_color"));

                                    ManhuntConfig.CONFIG.save();
                                    playUISound(player);

                                    runnersColorGui.setSlot(0,
                                            new GuiElementBuilder(Items.WHITE_WOOL).setName(Text.translatable("color" +
                                                    ".minecraft.white")).setCallback(() -> {
                                                ManhuntConfig.CONFIG.setRunnersColor(Formatting.RESET);
                                                player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.CONFIG.getRunnersColor());
                                                openTeamColorGui(player, clickType, boolvalue);
                                            }));

                                    runnersColorGui.setSlot(1,
                                            new GuiElementBuilder(Items.LIGHT_GRAY_WOOL).setName(Text.translatable(
                                                    "color" +
                                                            ".minecraft.light_gray").formatted(Formatting.GRAY)).setCallback(() -> {
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
                                                    ".minecraft" +
                                                    ".red").formatted(Formatting.RED)).setCallback(() -> {
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
                                                            ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(teamColorGui::open));

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
                                                    "color" +
                                                            ".minecraft.light_blue").formatted(Formatting.BLUE)).setCallback(() -> {
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
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openManhuntSettingsGui(player)));

                    teamColorGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
        }
    }

    public static void openGlobalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui globalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

        globalPreferencesGui.setTitle(Text.translatable("category.manhunt.global_preferences"));

        ManhuntConfig.CONFIG.save();
        playUISound(player);

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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomTitles(ManhuntConfig.CONFIG.isCustomTitlesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomTitles(!gameTitlesBool);
                            }
                            openGlobalPreferencesGui(player);
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).hideDefaultTooltip().setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomSounds(ManhuntConfig.CONFIG.isCustomSoundsDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomSounds(!manhuntSoundsBool);
                            }
                            openGlobalPreferencesGui(player);
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setCustomParticles(ManhuntConfig.CONFIG.isCustomParticlesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setCustomParticles(!customParticlesBool);
                            }
                            openGlobalPreferencesGui(player);
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

        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> openTrackerTypeGlobal(player, type)));
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setNightVision(ManhuntConfig.CONFIG.isNightVisionDefault());
                            } else {
                                ManhuntConfig.CONFIG.setNightVision(!nightVisionBool);
                            }
                            openGlobalPreferencesGui(player);
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "runner_preferences";
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("category.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setRunnerPreferences(ManhuntConfig.CONFIG.isRunnerPreferencesDefault());
                            } else {
                                ManhuntConfig.CONFIG.setRunnerPreferences(!runnerPreferencesBool);
                            }
                            openGlobalPreferencesGui(player);
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setBedExplosions(ManhuntConfig.CONFIG.isBedExplosionsDefault());
                            } else {
                                ManhuntConfig.CONFIG.setBedExplosions(!bedExplosionsBool);
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));
        slot++;

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text" +
                ".manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            playUISound(player);
            openSettingsGui(player);
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
        globalPreferencesGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("setting.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setLavaPvpInNether(ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());
                            } else {
                                ManhuntConfig.CONFIG.setLavaPvpInNether(!lavaPvpInNetherBool);
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

        globalPreferencesGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        globalPreferencesGui.open();
    }

    private static void openTrackerTypeGlobal(ServerPlayerEntity player, ClickType clickType) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
            if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                if (clickType == ClickType.DROP) {
                    ManhuntConfig.CONFIG.setTrackerType(ManhuntConfig.CONFIG.getTrackerTypeDefault());
                    openGlobalPreferencesGui(player);
                } else {
                    var trackerTypeGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    trackerTypeGui.setTitle(Text.translatable("setting.manhunt.team_color"));

                    playUISound(player);

                    trackerTypeGui.setSlot(0, new GuiElementBuilder(Items.COMPARATOR).setName(Text.translatable("lore" +
                            ".manhunt.per_player").formatted(Formatting.YELLOW)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setTrackerType(1);
                        openGlobalPreferencesGui(player);
                    }));

                    trackerTypeGui.setSlot(1, new GuiElementBuilder(Items.REDSTONE).setName(Text.translatable("lore" +
                            ".manhunt.tracker_type.manual_tracking").formatted(Formatting.RED)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setTrackerType(2);
                        openGlobalPreferencesGui(player);
                    }));

                    trackerTypeGui.setSlot(2, new GuiElementBuilder(Items.COMPASS).setName(Text.translatable("lore" +
                            ".manhunt.tracker_type.automatic_when_holding").formatted(Formatting.GOLD)).setCallback(() -> {
                        ManhuntConfig.CONFIG.setTrackerType(3);
                        openGlobalPreferencesGui(player);
                    }));

                    trackerTypeGui.setSlot(3,
                            new GuiElementBuilder(Items.RECOVERY_COMPASS).setName(Text.translatable("lore.manhunt" +
                                    ".tracker_type.always_automatic").formatted(Formatting.GREEN)).setCallback(() -> {
                                ManhuntConfig.CONFIG.setTrackerType(4);
                                openGlobalPreferencesGui(player);
                            }));

                    trackerTypeGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable(
                            "text.manhunt.go_back").formatted(Formatting.WHITE)).setCallback(() -> openGlobalPreferencesGui(player)));

                    trackerTypeGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
        }
    }

    public static void openTitleTextsGui(ServerPlayerEntity player) {
        SimpleGui titleTextsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        titleTextsGui.setTitle(Text.translatable("category.manhunt.title_texts"));

        ManhuntConfig.CONFIG.save();
        playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item = Items.PAPER;

        loreList = new ArrayList<>();
        name = "start_title";

        loreList.add(Text.literal("\"" + ManhuntConfig.CONFIG.getStartTitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("title_text.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setStartTitle(ManhuntConfig.CONFIG.getStartTitleDefault());
                            } else {
                                AnvilInputGui gameStartTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    ManhuntConfig.CONFIG.setStartTitle(input);
                                                    openTitleTextsGui(player);
                                                }));
                                    }
                                };
                                gameStartTitleGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                gameStartTitleGui.setDefaultInputValue("");
                                gameStartTitleGui.open();
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
        name = "start_subtitle";

        loreList.add(Text.literal("\"" + ManhuntConfig.CONFIG.getStartSubtitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("title_text.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setStartSubtitle(ManhuntConfig.CONFIG.getStartSubtitleDefault());
                            } else {
                                AnvilInputGui gameStartSubtitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    ManhuntConfig.CONFIG.setStartSubtitle(input);
                                                    openTitleTextsGui(player);
                                                }));
                                    }
                                };
                                gameStartSubtitleGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                gameStartSubtitleGui.setDefaultInputValue("");
                                gameStartSubtitleGui.open();
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
        name = "paused_title";

        loreList.add(Text.literal("\"" + ManhuntConfig.CONFIG.getPausedTitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("title_text.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.CONFIG.setPausedTitle(ManhuntConfig.CONFIG.getPausedTitleDefault());
                                openTitleTextsGui(player);
                            } else {
                                AnvilInputGui gamePausedTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2,
                                                new GuiElementBuilder(Items.PAPER).setName(Text.literal(input).formatted(Formatting.ITALIC)).setCallback(() -> {
                                                    ManhuntConfig.CONFIG.setPausedTitle(input);
                                                    openTitleTextsGui(player);
                                                }));
                                    }
                                };
                                gamePausedTitleGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                                gamePausedTitleGui.setDefaultInputValue("");
                                gamePausedTitleGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                }));

        titleTextsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text.manhunt" +
                ".go_back").formatted(Formatting.WHITE)).setCallback(() -> {
            playUISound(player);
            openSettingsGui(player);
        }));

        titleTextsGui.open();
    }

    public static void openModIntegrationsGui(ServerPlayerEntity player) {
        SimpleGui modIntegrationsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        modIntegrationsGui.setTitle(Text.translatable("category.manhunt.mod_integrations"));

        ManhuntConfig.CONFIG.save();
        playUISound(player);

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
            playUISound(player);
            openSettingsGui(player);
        }));

        modIntegrationsGui.open();
    }

    private static void openVanillaIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
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
                playUISound(player);

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
                                    ".difficulty" +
                                    ".hard").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                vanillaIntegrationGui.setSlot(slot,
                        new GuiElementBuilder(item).setName(Text.translatable(name).formatted(Formatting.WHITE)).setLore(loreList).setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item).setName(Text.translatable("setting" +
                        ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input"
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
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item).setName(Text.translatable("setting" +
                        ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                                        player.sendMessage(Text.translatable("chat.manhunt" +
                                                                ".invalid_input"
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
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item).setName(Text.translatable("setting" +
                        ".manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        SLOW_DOWN_MANAGER.put(player.getUuid(), SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
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
                            ChunkyAPI chunky = ChunkyProvider.get().getApi();

                            chunky.cancelTask(String.valueOf(overworld.getRegistryKey().getValue()));
                            chunky.cancelTask(String.valueOf(theNether.getRegistryKey().getValue()));
                            chunky.cancelTask(String.valueOf(theEnd.getRegistryKey().getValue()));
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var chunkyIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                chunkyIntegrationGui.setTitle(Text.translatable("integration.manhunt.chunky"));

                ManhuntConfig.CONFIG.save();
                playUISound(player);

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
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                                                    ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                    chunky.cancelTask(String.valueOf(overworld.getRegistryKey().getValue()));
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
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                                            ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                            chunky.cancelTask(String.valueOf(theNether.getRegistryKey().getValue()));
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
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                                                            ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                            chunky.cancelTask(String.valueOf(theEnd.getRegistryKey().getValue()));
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

    private static void playUISound(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER,
                player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F,
                player.getWorld().random.nextLong()));
    }
}
