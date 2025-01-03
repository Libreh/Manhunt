package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.libreh.manhunt.config.PreferencesData;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.SPAM_PREVENTION;

public class RunnerPreferencesGui {
    public static void openRunnerPreferencesGui(ServerPlayerEntity player) {
        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("config.manhunt.runner_preferences"));

        ConfigGui.playUISound(player);

        PreferencesData state = PreferencesData.get(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "bed_explosions_pvp";
        item = Items.RED_BED;
        boolvalue = state.bedExplosionsPvP;

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

        var bedExplosions = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        state.bedExplosionsPvP = !bedExplosions;

                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                }));
        slot++;

        loreList = new ArrayList<>();
        name = "nether_lava_pvp";
        item = Items.LAVA_BUCKET;
        boolvalue = state.netherLavaPvP;

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

        var lavaPvpNether = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (SPAM_PREVENTION.get(player.getUuid()) < 12)
                        SPAM_PREVENTION.put(player.getUuid(), SPAM_PREVENTION.get(player.getUuid()) + 1);
                    if (SPAM_PREVENTION.get(player.getUuid()) < 6) {
                        state.netherLavaPvP = !lavaPvpNether;

                        openRunnerPreferencesGui(player);
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
