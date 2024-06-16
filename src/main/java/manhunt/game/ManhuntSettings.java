package manhunt.game;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.ManhuntMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static manhunt.ManhuntMod.*;

public class ManhuntSettings {
    public static void openPreferencesGui(ServerPlayerEntity player) {
        SimpleGui preferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        preferencesGui.setTitle(Text.translatable("manhunt.item.preferences"));

        List<Text> loreList;
        String name;
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "personal_preferences";
        item = Items.PLAYER_HEAD;
        ItemStack stack = new ItemStack(item);

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(11, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openPersonalPreferencesGui(player))
        );

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(15, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openRunnerPreferencesGui(player))
        );

        preferencesGui.open();
    }

    public static void openPersonalPreferencesGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("manhunt.item.preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "game_titles";
        item = Items.OAK_SIGN;
        bool = gameTitles.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean gameTitlesBool = bool;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            gameTitles.put(player.getUuid(), config.isGameTitlesDefault());
                        } else {
                            gameTitles.put(player.getUuid(), !gameTitlesBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhunt_sounds";
        item = Items.FIRE_CHARGE;
        bool = manhuntSounds.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean manhuntSoundsBool = bool;
        personalPreferencesGui.setSlot(1, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            manhuntSounds.put(player.getUuid(), config.isManhuntSoundsDefault());
                        } else {
                            manhuntSounds.put(player.getUuid(), !manhuntSoundsBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        bool = nightVision.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean nightVisionBool = bool;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            nightVision.put(player.getUuid(), false);
                        } else {
                            nightVision.put(player.getUuid(), !nightVisionBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        bool = friendlyFire.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean friendlyFireBool = bool;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            friendlyFire.put(player.getUuid(), true);
                        } else {
                            friendlyFire.put(player.getUuid(), !friendlyFireBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        personalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("manhunt.setting.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openPreferencesGui(player);
                })
        );

        personalPreferencesGui.open();
    }

    public static void openRunnerPreferencesGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        SimpleGui runnerPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        runnerPreferencesGui.setTitle(Text.translatable("manhunt.setting.runner_preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        bool = bedExplosions.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.item_lore." + name + "_second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = bool;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            bedExplosions.put(player.getUuid(), config.isBedExplosionsDefault());
                        } else {
                            bedExplosions.put(player.getUuid(), !bedExplosionsBool);
                        }
                        config.save();
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        bool = lavaPvpInNether.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.item_lore." + name + "_second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = bool;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            lavaPvpInNether.put(player.getUuid(), config.isLavaPvpInNetherDefault());
                        } else {
                            lavaPvpInNether.put(player.getUuid(), !lavaPvpInNetherBool);
                        }
                        config.save();
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        runnerPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("manhunt.setting.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openPreferencesGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                })
        );

        runnerPreferencesGui.open();
    }

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        settingsGui.setTitle(Text.translatable("manhunt.item.settings"));

        List<Text> loreList;
        String name;
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "game_settings";
        item = Items.COMPARATOR;
        ItemStack stack = new ItemStack(item);

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(11, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openGameSettingsGui(player))
        );

        loreList = new ArrayList<>();
        name = "global_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(15, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openGlobalPreferencesGui(player))
        );

        settingsGui.open();
    }

    public static void openGlobalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui globalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        globalPreferencesGui.setTitle(Text.translatable("manhunt.setting.global_preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;
        int integer;

        loreList = new ArrayList<>();
        name = "game_titles";
        item = Items.OAK_SIGN;
        bool = config.isGameTitles();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var gameTitlesBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setGameTitles(config.isGameTitlesDefault());
                            } else {
                                config.setGameTitles(!gameTitlesBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhunt_sounds";
        item = Items.FIRE_CHARGE;
        bool = config.isManhuntSounds();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var manhuntSoundsBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setManhuntSounds(config.isManhuntSoundsDefault());
                            } else {
                                config.setManhuntSounds(!manhuntSoundsBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        bool = config.isNightVision();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var nightVisionBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setNightVision(config.isNightVisionDefault());
                            } else {
                                config.setNightVision(!nightVisionBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        integer = config.getFriendlyFire();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 1) {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("manhunt.item_lore." + name + ".always").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore." + name + ".per_player"), Text.translatable("manhunt.item_lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer == 2) {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("manhunt.item_lore." + name + ".always"), Text.translatable("manhunt.item_lore." + name + ".per_player").formatted(Formatting.YELLOW), Text.translatable("manhunt.item_lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("manhunt.item_lore." + name + ".always"), Text.translatable("manhunt.item_lore." + name + ".per_player"), Text.translatable("manhunt.item_lore." + name + ".never").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        int friendlyFireInt = integer;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setFriendlyFire(config.getFriendlyFireDefault());
                            } else {
                                if (friendlyFireInt == 1) {
                                    config.setFriendlyFire(2);
                                } else if (friendlyFireInt == 2) {
                                    config.setFriendlyFire(3);
                                } else {
                                    config.setFriendlyFire(1);
                                }
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        bool = config.isBedExplosions();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.item_lore." + name + "_second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setBedExplosions(config.isBedExplosionsDefault());
                            } else {
                                config.setBedExplosions(!bedExplosionsBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        bool = config.isLavaPvpInNether();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.item_lore." + name + "_second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setLavaPvpInNether(config.isLavaPvpInNetherDefault());
                            } else {
                                config.setLavaPvpInNether(!lavaPvpInNetherBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;
        bool = config.isRunnerPreferences();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnerPreferencesBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnerPreferences(config.isRunnerPreferencesDefault());
                            } else {
                                config.setRunnerPreferences(!runnerPreferencesBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("manhunt.setting.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                })
        );

        globalPreferencesGui.open();
    }

    public static void openGameSettingsGui(ServerPlayerEntity player) {
        SimpleGui gameSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gameSettingsGui.setTitle(Text.translatable("manhunt.item.settings"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;
        int integer;

        loreList = new ArrayList<>();
        name = "team_preset";
        item = Items.COBBLESTONE;
        integer = config.getTeamPreset();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 1) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.free_select").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.team_preset.runner_cycle")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.hunter_infection"), Text.translatable("manhunt.item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer == 2) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.free_select"), Text.translatable("manhunt.item_lore.team_preset.runner_cycle").formatted(Formatting.YELLOW)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.hunter_infection"), Text.translatable("manhunt.item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer == 3) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.free_select"), Text.translatable("manhunt.item_lore.team_preset.runner_cycle")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.hunter_infection").formatted(Formatting.GOLD), Text.translatable("manhunt.item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.free_select"), Text.translatable("manhunt.item_lore.team_preset.runner_cycle")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.team_preset.hunter_infection"), Text.translatable("manhunt.item_lore.team_preset.speedrun_showdown").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var teamPresetInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setTeamPreset(config.getTeamPresetDefault());
                            } else {
                                if (teamPresetInt == 1) {
                                    config.setTeamPreset(2);

                                    if (playerList == null || playerList.isEmpty()) {
                                        playerList = new ArrayList<>(player.getServer().getPlayerManager().getPlayerList());
                                    }

                                    for (ServerPlayerEntity serverPlayer : playerList) {
                                        if (playerList.get(0) == null) {
                                            playerList.remove(playerList.get(0));
                                        }
                                    }

                                    ServerPlayerEntity runner = playerList.get(0);

                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(runner.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

                                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.one_role", Text.literal(runner.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runner")), false);

                                    playerList.remove(runner);
                                } else if (teamPresetInt == 2) {
                                    config.setTeamPreset(3);

                                    List<ServerPlayerEntity> players = new ArrayList<>(player.getServer().getPlayerManager().getPlayerList());

                                    Collections.shuffle(players);

                                    ServerPlayerEntity hunter = players.get(0);

                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(hunter.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

                                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.one_role", Text.literal(hunter.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("manhunt.role.hunter")), false);
                                } else if (teamPresetInt == 3) {
                                    config.setTeamPreset(4);

                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                                    }

                                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.set_role", Text.literal("Everyone").formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runner")), false);
                                } else {
                                    config.setTeamPreset(1);
                                }
                            }
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "preload_chunks";
        item = Items.GRASS_BLOCK;
        bool = config.isPreloadChunks();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var preloadChunksItem = item;
        var preloadChunksBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setPreloadChunks(config.isPreloadChunksDefault());
                            } else {
                                config.setPreloadChunks(!preloadChunksBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);

                            if (!preloadChunksBool) {
                                schedulePreload(player.getServer());
                            } else {
                                if (isChunkyIntegration()) {
                                    ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                    chunky.cancelTask("manhunt:overworld");
                                    chunky.cancelTask("manhunt:the_nether");
                                }
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "automatic_compass";
        item = Items.COMPASS;
        bool = config.isAutomaticCompass();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var automaticCompassItem = item;
        var automaticCompassBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setAutomaticCompass(config.isAutomaticCompassDefault());
                            } else {
                                config.setAutomaticCompass(!automaticCompassBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "team_color";
        item = Items.WHITE_BANNER;
        bool = config.isTeamColor();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        var teamColorBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorBool))
        );
        slot++;

        loreList = new ArrayList<>();
        name = "team_suffix";
        item = Items.LEATHER_CHESTPLATE;
        bool = config.isTeamSuffix();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var teamSuffixBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setTeamSuffix(config.isTeamSuffixDefault());
                            } else {
                                config.setTeamSuffix(!teamSuffixBool);

                                if (!teamSuffixBool) {
                                    player.getScoreboard().getTeam("hunters").setSuffix(Text.translatable("manhunt.chat.team_suffix", Text.literal("H").formatted(config.getHuntersColor())).formatted(Formatting.GRAY));
                                    player.getScoreboard().getTeam("runners").setSuffix(Text.translatable("manhunt.chat.team_suffix", Text.literal("R").formatted(config.getRunnersColor())).formatted(Formatting.GRAY));
                                } else {
                                    player.getScoreboard().getTeam("hunters").setSuffix(Text.literal(""));
                                    player.getScoreboard().getTeam("runners").setSuffix(Text.literal(""));
                                }
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_headstart";
        item = Items.GOLDEN_BOOTS;
        integer = config.getRunnerHeadstart();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 10 && integer != 20 && integer != 30) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 10) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("10").formatted(Formatting.RED), Text.literal("20"), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 20) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("10"), Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("10"), Text.literal("20"), Text.literal("30").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        int runnerHeadstartInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    config.setRunnerHeadstart(config.getRunnerHeadstartDefault());
                                } else {
                                    if (runnerHeadstartInt != 10 && runnerHeadstartInt != 20) {
                                        config.setRunnerHeadstart(10);
                                    } else {
                                        if (runnerHeadstartInt == 10) {
                                            config.setRunnerHeadstart(20);
                                        } else {
                                            config.setRunnerHeadstart(30);
                                        }
                                    }
                                }

                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openGameSettingsGui(player);
                            } else {
                                AnvilInputGui runnerHeadstartGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = runnerHeadstartInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                    }

                                                    config.setRunnerHeadstart(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };

                                runnerHeadstartGui.setTitle(Text.translatable("manhunt.setting.enter_value"));
                                runnerHeadstartGui.setDefaultInputValue("");
                                runnerHeadstartGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "time_limit";
        item = Items.CLOCK;
        integer = config.getTimeLimit();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 30 && integer != 60 && integer != 90) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 30) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("30").formatted(Formatting.RED), Text.literal("60"), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 60) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("30"), Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("30"), Text.literal("60"), Text.literal("90").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        int timeLimitInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    config.setTimeLimit(config.getTimeLimitDefault());
                                } else {
                                    if (timeLimitInt != 30 && timeLimitInt != 60) {
                                        config.setTimeLimit(30);
                                    } else {
                                        if (timeLimitInt == 30) {
                                            config.setTimeLimit(60);
                                        } else {
                                            config.setTimeLimit(90);
                                        }
                                    }
                                }

                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openGameSettingsGui(player);
                            } else {
                                AnvilInputGui timeLimitGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = timeLimitInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                    }

                                                    config.setTimeLimit(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };

                                timeLimitGui.setTitle(Text.translatable("manhunt.setting.enter_value"));
                                timeLimitGui.setDefaultInputValue("");
                                timeLimitGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_glow";
        item = Items.SPECTRAL_ARROW;
        bool = config.isRunnersGlow();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersGlowBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersGlow(config.isRunnersGlowDefault());
                            } else {
                                config.setRunnersGlow(!runnersGlowBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        gameSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("manhunt.setting.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "difficulty";
        item = Items.CREEPER_HEAD;
        Difficulty difficulty = config.getDifficulty();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (difficulty == Difficulty.EASY) {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (difficulty == Difficulty.NORMAL) {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW), Text.translatable("options.difficulty.hard")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("options.difficulty").formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setDifficulty(config.getGameDifficultyDefault());
                            } else {
                                if (difficulty == Difficulty.EASY) {
                                    config.setDifficulty(Difficulty.NORMAL);
                                } else if (difficulty == Difficulty.NORMAL) {
                                    config.setDifficulty(Difficulty.HARD);
                                } else {
                                    config.setDifficulty(Difficulty.EASY);
                                }
                            }
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "world_border";
        item = Items.PRISMARINE_WALL;
        integer = config.getWorldBorder();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 2816 && integer != 5888 && integer != 59999968) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.literal(String.valueOf(integer)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 2816) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 5888) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1st ring"), Text.literal("2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1st ring"), Text.literal("2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        int worldBorderInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    config.setWorldBorder(config.getWorldBorderDefault());
                                } else {
                                    if (worldBorderInt != 2816 && worldBorderInt != 5888) {
                                        config.setWorldBorder(2816);
                                    } else {
                                        if (worldBorderInt == 2816) {
                                            config.setWorldBorder(5888);
                                        } else {
                                            config.setWorldBorder(59999968);
                                        }
                                    }
                                }

                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openGameSettingsGui(player);
                            } else {
                                AnvilInputGui worldBorderGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = worldBorderInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                    }

                                                    config.setWorldBorder(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };

                                worldBorderGui.setTitle(Text.translatable("manhunt.setting.enter_value"));
                                worldBorderGui.setDefaultInputValue("");
                                worldBorderGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spawn_radius";
        item = Items.BEDROCK;
        integer = config.getSpawnRadius();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer != 0 && integer != 5 && integer != 10) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.literal(String.valueOf(integer)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 0) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("0").formatted(Formatting.RED), Text.literal("5"), Text.literal("10")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 5) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("0"), Text.literal("5").formatted(Formatting.YELLOW), Text.literal("10")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("0"), Text.literal("5"), Text.literal("10").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        int spawnRadiusInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    config.setSpawnRadius(config.getSpawnRadiusDefault());
                                } else {
                                    if (spawnRadiusInt != 0 && spawnRadiusInt != 5) {
                                        config.setSpawnRadius(0);
                                    } else {
                                        if (spawnRadiusInt == 0) {
                                            config.setSpawnRadius(5);
                                        } else {
                                            config.setSpawnRadius(10);
                                        }
                                    }
                                }

                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openGameSettingsGui(player);
                            } else {
                                AnvilInputGui spawnRadiusGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = spawnRadiusInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                    }

                                                    config.setSpawnRadius(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };

                                spawnRadiusGui.setTitle(Text.translatable("manhunt.setting.enter_value"));
                                spawnRadiusGui.setDefaultInputValue("");
                                spawnRadiusGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spectate_on_win";
        item = Items.SPYGLASS;
        bool = config.isSpectateOnWin();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var spectateWinBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setSpectateOnWin(config.isSpectateWinDefault());
                            } else {
                                config.setSpectateOnWin(!spectateWinBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spectators_generate_chunks";
        item = Items.STONE;
        bool = config.isSpectatorsGenerateChunks();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var spectatorsGenerateChunksBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setSpectatorsGenerateChunks(config.isSpectatorsGenerateChunksDefault());
                            } else {
                                config.setSpectatorsGenerateChunks(!spectatorsGenerateChunksBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_hunt_on_death";
        item = Items.SKELETON_SKULL;
        bool = config.isRunnersHuntOnDeath();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersHuntOnDeathBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersHuntOnDeath(config.isRunnersHuntOnDeathDefault());
                            } else {
                                config.setRunnersHuntOnDeath(!runnersHuntOnDeathBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_can_pause";
        item = Items.ICE;
        bool = config.isRunnersCanPause();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on").formatted(Formatting.GREEN), Text.translatable("manhunt.item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.item_lore.double", Text.translatable("manhunt.item_lore.on"), Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersCanPauseBool = bool;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersCanPause(config.isRunnerCanPauseDefault());
                            } else {
                                config.setRunnersCanPause(!runnersCanPauseBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "pause_time_on_leave";
        item = Items.IRON_BARS;
        integer = config.getPauseTimeOnLeave();

        loreList.add(Text.translatable("manhunt.item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.translatable("manhunt.item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 1 && integer != 2 && integer != 5) {
            loreList.add(Text.translatable("manhunt.item_lore.single", Text.literal(String.valueOf(integer)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 1) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1").formatted(Formatting.RED), Text.literal("2"), Text.literal("5")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 2) {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1"), Text.literal("2").formatted(Formatting.YELLOW), Text.literal("5")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.item_lore.triple", Text.literal("1"), Text.literal("2"), Text.literal("5").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.item_lore.both", Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)), Text.translatable("manhunt.item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false))));

        int pauseTimeOnLeaveInt = integer;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt.setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    config.setPauseTimeOnLeave(config.getPauseTimeOnLeaveDefault());
                                } else {
                                    if (pauseTimeOnLeaveInt != 1 && pauseTimeOnLeaveInt != 2) {
                                        config.setPauseTimeOnLeave(1);
                                    } else {
                                        if (pauseTimeOnLeaveInt == 1) {
                                            config.setPauseTimeOnLeave(2);
                                        } else {
                                            config.setPauseTimeOnLeave(5);
                                        }
                                    }
                                }

                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openGameSettingsGui(player);
                            } else {
                                AnvilInputGui pauseTimeOnLeaveGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    int value = pauseTimeOnLeaveInt;
                                                    try {
                                                        value = Integer.parseInt(input);
                                                    } catch (NumberFormatException e) {
                                                        player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                    }

                                                    config.setPauseTimeOnLeave(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };

                                pauseTimeOnLeaveGui.setTitle(Text.translatable("manhunt.setting.enter_value"));
                                pauseTimeOnLeaveGui.setDefaultInputValue("");
                                pauseTimeOnLeaveGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        gameSettingsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE));
        gameSettingsGui.setSlot(26, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE));

        gameSettingsGui.open();
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
        if (ManhuntMod.checkPermission(player, "manhunt.settings")) {
            if (slowDownManager.get(player.getUuid()) < 4) {
                if (!clickType.shift) {
                    if (clickType == ClickType.DROP) {
                        config.setTeamColor(config.isTeamColorDefault());
                    } else {
                        config.setTeamColor(!bool);
                        if (!bool) {
                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                        } else {
                            player.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
                            player.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
                        }
                    }

                    config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openGameSettingsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.translatable("manhunt.item_lore.hunters_color").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                            .setName(Text.translatable("manhunt.setting.hunters_color").formatted(config.getHuntersColor()))
                            .setLore(loreList)
                            .setCallback(() -> {
                                SimpleGui huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                huntersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                        .setName(Text.translatable("color.minecraft.white"))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.RESET);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                        .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.GRAY);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                        .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.DARK_GRAY);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                        .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.BLACK);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                        .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.RED);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                        .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.GOLD);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                        .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.YELLOW);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                        .setName(Text.translatable("manhunt.setting.go_back").formatted(Formatting.WHITE))
                                        .setCallback(teamColorGui::open)
                                );

                                huntersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                        .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.GREEN);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                        .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.DARK_GREEN);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                        .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.DARK_AQUA);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                        .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.BLUE);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                        .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.DARK_BLUE);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                        .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.DARK_PURPLE);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                        .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                        .setCallback(() -> {
                                            config.setHuntersColor(Formatting.LIGHT_PURPLE);
                                            config.save();
                                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                huntersColorGui.open();
                            })
                    );

                    loreList = new ArrayList<>();

                    loreList.add(Text.translatable("manhunt.item_lore.runners_color").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    loreList.add(Text.translatable("manhunt.item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK)
                            .setName(Text.translatable("manhunt.setting.runners_color").formatted(config.getRunnersColor()))
                            .setLore(loreList)
                            .setCallback(() -> {
                                SimpleGui runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                runnersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                        .setName(Text.translatable("color.minecraft.white"))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.RESET);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                        .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.GRAY);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                        .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.DARK_GRAY);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                        .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.BLACK);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                        .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.RED);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                        .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.GOLD);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                        .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.YELLOW);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                        .setName(Text.translatable("manhunt.goback").formatted(Formatting.WHITE))
                                        .setCallback(teamColorGui::open)
                                );

                                runnersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                        .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.GREEN);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                        .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.DARK_GREEN);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                        .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.DARK_AQUA);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                        .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.BLUE);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                        .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.DARK_BLUE);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                        .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.DARK_PURPLE);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                        .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                        .setCallback(() -> {
                                            config.setRunnersColor(Formatting.LIGHT_PURPLE);
                                            config.save();
                                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                            openTeamColorGui(player, clickType, bool);
                                        })
                                );

                                runnersColorGui.open();
                            })
                    );

                    teamColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                            .setName(Text.translatable("manhunt.goback").formatted(Formatting.WHITE))
                            .setCallback(() -> openGameSettingsGui(player))
                    );

                    teamColorGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("manhunt.chat.no_permission").formatted(Formatting.RED));
        }
    }
}
