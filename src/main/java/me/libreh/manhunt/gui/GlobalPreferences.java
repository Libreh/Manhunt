package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.libreh.manhunt.config.Config;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Methods.hasPermission;

public class GlobalPreferences {
    public static void openGlobalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui globalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        globalPreferencesGui.setTitle(Text.translatable("config.manhunt.global_preferences"));

        Config.saveConfig();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        String stringvalue;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.NOTE_BLOCK;
        stringvalue = Config.getConfig().globalPreferences.customSounds;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("always")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(PER_PLAYER)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.never").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        String customSounds = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.customSounds = Config.getConfig().customSoundsDefault;
                            } else {
                                if (customSounds.equals("always")) {
                                    Config.getConfig().globalPreferences.customSounds = PER_PLAYER;
                                } else if (customSounds.equals(PER_PLAYER)) {
                                    Config.getConfig().globalPreferences.customSounds = "never";
                                } else {
                                    Config.getConfig().globalPreferences.customSounds = "always";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_titles";
        item = Items.OAK_SIGN;
        stringvalue = Config.getConfig().globalPreferences.customTitles;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("always")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(PER_PLAYER)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.never").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        String customTitles = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.customTitles = Config.getConfig().customTitlesDefault;
                            } else {
                                if (customTitles.equals("always")) {
                                    Config.getConfig().globalPreferences.customTitles = PER_PLAYER;
                                } else if (customTitles.equals(PER_PLAYER)) {
                                    Config.getConfig().globalPreferences.customTitles = "never";
                                } else {
                                    Config.getConfig().globalPreferences.customTitles = "always";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        stringvalue = Config.getConfig().globalPreferences.friendlyFire;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("always")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(PER_PLAYER)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        String friendlyFire = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                       if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.friendlyFire = Config.getConfig().friendlyFireDefault;
                            } else {
                                if (friendlyFire.equals("always")) {
                                    Config.getConfig().globalPreferences.friendlyFire = PER_PLAYER;
                                } else if (friendlyFire.equals(PER_PLAYER)) {
                                    Config.getConfig().globalPreferences.friendlyFire = "never";
                                } else {
                                    Config.getConfig().globalPreferences.friendlyFire = "always";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "bed_explosions_pvp";
        item = Items.RED_BED;
        stringvalue = Config.getConfig().globalPreferences.bedExplosionsPvP;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("on")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.runners_preference"),
                    Text.translatable("lore.manhunt.off")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(RUNNERS_PREFERENCE)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.runners_preference").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.off")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsPvP = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.bedExplosionsPvP = Config.getConfig().bedExplosionsPvPDefault;
                            } else {
                                if (bedExplosionsPvP.equals("on")) {
                                    Config.getConfig().globalPreferences.bedExplosionsPvP = RUNNERS_PREFERENCE;
                                } else if (bedExplosionsPvP.equals(RUNNERS_PREFERENCE)) {
                                    Config.getConfig().globalPreferences.bedExplosionsPvP = "off";
                                } else {
                                    Config.getConfig().globalPreferences.bedExplosionsPvP = "on";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                }));

        loreList = new ArrayList<>();
        name = "nether_lava_pvp";
        item = Items.LAVA_BUCKET;
        stringvalue = Config.getConfig().globalPreferences.netherLavaPvP;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("on")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.runners_preference"),
                    Text.translatable("lore.manhunt.off")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(RUNNERS_PREFERENCE)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.runners_preference").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.off")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.on"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.off").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop")
                .styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var netherLavaPvP = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.netherLavaPvP = Config.getConfig().netherLavaPvPDefault;
                            } else {
                                if (netherLavaPvP.equals("on")) {
                                    Config.getConfig().globalPreferences.netherLavaPvP = RUNNERS_PREFERENCE;
                                } else if (netherLavaPvP.equals(RUNNERS_PREFERENCE)) {
                                    Config.getConfig().globalPreferences.netherLavaPvP = "off";
                                } else {
                                    Config.getConfig().globalPreferences.netherLavaPvP = "on";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "announce_seed";
        item = Items.WHEAT_SEEDS;
        stringvalue = Config.getConfig().globalPreferences.announceSeed;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("always")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(PER_PLAYER)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.never").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        String announceSeed = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.announceSeed = Config.getConfig().announceSeedDefault;
                            } else {
                                if (announceSeed.equals("always")) {
                                    Config.getConfig().globalPreferences.announceSeed = PER_PLAYER;
                                } else if (announceSeed.equals(PER_PLAYER)) {
                                    Config.getConfig().globalPreferences.announceSeed = "never";
                                } else {
                                    Config.getConfig().globalPreferences.announceSeed = "always";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "announce_duration";
        item = Items.CLOCK;
        stringvalue = Config.getConfig().globalPreferences.announceDuration;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.empty());
        if (stringvalue.equals("always")) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always").formatted(Formatting.GREEN),
                    Text.translatable("lore.manhunt.per_player"),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (stringvalue.equals(PER_PLAYER)) {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.per_player").formatted(Formatting.YELLOW),
                    Text.translatable("lore.manhunt.never")
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple",
                    Text.translatable("lore.manhunt.always"),
                    Text.translatable("lore.manhunt.runners_preferences"),
                    Text.translatable("lore.manhunt.never").formatted(Formatting.RED)
            ).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        String announceDuration = stringvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        if (hasPermission(player, "manhunt.config")) {
                            if (type == ClickType.DROP) {
                                Config.getConfig().globalPreferences.announceDuration = Config.getConfig().announceDurationDefault;
                            } else {
                                if (announceDuration.equals("always")) {
                                    Config.getConfig().globalPreferences.announceDuration = PER_PLAYER;
                                } else if (announceDuration.equals(PER_PLAYER)) {
                                    Config.getConfig().globalPreferences.announceDuration = "never";
                                } else {
                                    Config.getConfig().globalPreferences.announceDuration = "always";
                                }
                            }
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                })
        );

        globalPreferencesGui.open();
    }
}
