package me.libreh.manhunt.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigGui {
    public static void openConfigGui(ServerPlayerEntity player) {
        SimpleGui configGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        configGui.setTitle(Text.translatable("item.manhunt.config"));

        String name;
        int slot = 0;
        Item item;

        name = "game_options";
        item = Items.COMPARATOR;

        configGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name)
                        .styled(style -> style.withColor(Formatting.WHITE).withItalic(false)))
                .setCallback((index, type, action) -> GameOptionsGui.openGameOptionsGui(player)));
        slot++;

        name = "global_preferences";
        item = Items.BOOK;

        configGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setCallback((index, type, action) -> GlobalPreferencesGui.openGlobalPreferencesGui(player)));
        slot++;

        name = "mod_integrations";
        item = Items.PISTON;

        configGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("config.manhunt." + name))
                .setCallback((index, type, action) -> ModIntegrationsGui.openModIntegrationsGui(player)));

        configGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    ConfigGui.playUISound(player);
                    player.closeHandledScreen();
                }));

        configGui.open();
    }

    public static void playUISound(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER,
                player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(),
                0.5F, 1.0F, player.getWorld().random.nextLong()));
    }
}