package manhunt.config.gui;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConfigGui {
    public static final HashMap<UUID, Integer> SLOW_DOWN_MANAGER = new HashMap<>();

    public static void openConfigGui(ServerPlayerEntity player) {
        SimpleGui gameConfigGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gameConfigGui.setTitle(Text.translatable("item.manhunt.config"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "game_options";
        item = Items.COMPARATOR;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        gameConfigGui.setSlot(10,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false))).setLore(loreList).setSkullOwner(player.getGameProfile(), player.getServer()).setCallback((index, type, action) -> GameOptionsGui.openGameOptionsGui(player)));

        loreList = new ArrayList<>();
        name = "global_settings";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        gameConfigGui.setSlot(12,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> GlobalSettingsGui.openGlobalSettingsGui(player)));

        loreList = new ArrayList<>();
        name = "title_texts";
        item = Items.BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        gameConfigGui.setSlot(14,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> TitleTextsGui.openTitleTextsGui(player)));

        loreList = new ArrayList<>();
        name = "mod_integrations";
        item = Items.PISTON;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        gameConfigGui.setSlot(16,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> ModIntegrationsGui.openModIntegrationsGui(player)));

        gameConfigGui.open();
    }

    public static void playUISound(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER,
                player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F,
                player.getWorld().random.nextLong()));
    }
}
