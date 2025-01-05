package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.libreh.manhunt.config.PlayerData;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.SPAM_PREVENTION;

public class PersonalPreferencesGui {
    public static void openPersonalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("config.manhunt.personal_preferences"));

        ConfigGui.playUISound(player);

        PlayerData data = PlayerData.get(player);

        PlayerData.STORAGE.save(player, data);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.NOTE_BLOCK;
        boolvalue = data.customSounds;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
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

        boolean customSounds = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        data.customSounds = !customSounds;
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "custom_titles";
        item = Items.OAK_SIGN;
        boolvalue = data.customTitles;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
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

        boolean customTitles = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        data.customTitles = !customTitles;
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        boolvalue = data.friendlyFire;

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

        boolean friendlyFire = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        data.friendlyFire = !friendlyFire;
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "announce_seed";
        item = Items.WHEAT_SEEDS;
        boolvalue = data.announceSeed;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
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

        boolean announceSeed = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        data.announceSeed = !announceSeed;
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "announce_duration";
        item = Items.CLOCK;
        boolvalue = data.announceDuration;

        loreList.add(Text.empty());
        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style
                .withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style
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

        boolean announceDuration = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        data.announceDuration = !announceDuration;
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));

        personalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    PreferencesGui.openPreferencesGui(player);
                }));

        personalPreferencesGui.open();
    }
}
