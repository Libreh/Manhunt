package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PreferencesGui {
    public static void openPreferencesGui(ServerPlayerEntity player) {
        SimpleGui preferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        preferencesGui.setTitle(Text.translatable("item.manhunt.preferences"));

        String name;
        int slot = 0;
        Item item;

        name = "personal_preferences";
        item = Items.PLAYER_HEAD;

        preferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name)
                        .styled(style -> style.withColor(Formatting.WHITE).withItalic(false)))
                .setCallback((index, type, action) -> PersonalPreferencesGui.openPersonalPreferencesGui(player)));
        slot++;

        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;

        preferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setCallback((index, type, action) -> RunnerPreferencesGui.openRunnerPreferencesGui(player)));

        preferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    player.closeHandledScreen();
                }));

        preferencesGui.open();
    }
}
