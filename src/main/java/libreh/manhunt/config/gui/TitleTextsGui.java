package libreh.manhunt.config.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
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

public class TitleTextsGui {
    public static void openTitleTextsGui(ServerPlayerEntity player) {
        SimpleGui titleTextsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        titleTextsGui.setTitle(Text.translatable("config.manhunt.title_texts"));

        ManhuntConfig.CONFIG.save();
        ConfigGui.playUISound(player);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item = Items.PAPER;

        loreList = new ArrayList<>();
        name = "start_title";

        loreList.add(Text.literal("\"" + ManhuntConfig.CONFIG.getStartTitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot,
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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
                new GuiElementBuilder(item).setName(Text.translatable("config.manhunt." + name)).setLore(loreList).setCallback((index, type, action) -> {
                    if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 12)
                        ConfigGui.SLOW_DOWN_MANAGER.put(player.getUuid(),
                                ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.config")) {
                        if (ConfigGui.SLOW_DOWN_MANAGER.get(player.getUuid()) < 6) {
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

        titleTextsGui.setSlot(8,
                new GuiElementBuilder(Items.STRUCTURE_VOID).setName(Text.translatable("text.manhunt" + ".go_back").formatted(Formatting.WHITE)).setCallback(() -> {
                    ConfigGui.playUISound(player);
                    ConfigGui.openConfigGui(player);
                }));

        titleTextsGui.open();
    }
}
