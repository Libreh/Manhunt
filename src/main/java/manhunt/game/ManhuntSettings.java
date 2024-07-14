package manhunt.game;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static manhunt.ManhuntMod.*;

public class ManhuntSettings {
    public static void openPreferencesGui(ServerPlayerEntity player) {
        SimpleGui preferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        preferencesGui.setTitle(Text.translatable("item.manhunt.preferences"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "personal_preferences";
        item = Items.PLAYER_HEAD;

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(11, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openPersonalPreferencesGui(player))
        );

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(15, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openRunnerPreferencesGui(player))
        );

        preferencesGui.open();
    }

    public static void openPersonalPreferencesGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("item.manhunt.preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "game_titles";
        item = Items.OAK_SIGN;
        boolvalue = gameTitles.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean gameTitlesBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            gameTitles.put(player.getUuid(), config.isGameTitlesDefault());
                        } else {
                            gameTitles.put(player.getUuid(), !gameTitlesBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhunt_sounds";
        item = Items.FIRE_CHARGE;
        boolvalue = manhuntSounds.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean manhuntSoundsBool = boolvalue;
        personalPreferencesGui.setSlot(1, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            manhuntSounds.put(player.getUuid(), config.isManhuntSoundsDefault());
                        } else {
                            manhuntSounds.put(player.getUuid(), !manhuntSoundsBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        boolvalue = nightVision.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean nightVisionBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            nightVision.put(player.getUuid(), false);
                        } else {
                            nightVision.put(player.getUuid(), !nightVisionBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        boolvalue = friendlyFire.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        boolean friendlyFireBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 4) {
                        if (type == ClickType.DROP) {
                            friendlyFire.put(player.getUuid(), true);
                        } else {
                            friendlyFire.put(player.getUuid(), !friendlyFireBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        personalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                    openPreferencesGui(player);
                })
        );

        personalPreferencesGui.open();
    }

    public static void openRunnerPreferencesGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui runnerPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        runnerPreferencesGui.setTitle(Text.translatable("menu_category.runner_preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean value;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        value = bedExplosions.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = value;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
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
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        value = lavaPvpInNether.get(player.getUuid());

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = value;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
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
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        runnerPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openPreferencesGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                })
        );

        runnerPreferencesGui.open();
    }

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        settingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        List<Text> loreList;
        String name;
        Item item;

        loreList = new ArrayList<>();
        name = "game_settings";
        item = Items.COMPARATOR;

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(10, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name).setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openGameSettingsGui(player))
        );

        loreList = new ArrayList<>();
        name = "global_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

        settingsGui.setSlot(12, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openGlobalPreferencesGui(player))
        );

        loreList = new ArrayList<>();
        name = "title_texts";
        item = Items.BOOK;

        settingsGui.setSlot(14, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTitleTextsGui(player))
        );

        loreList = new ArrayList<>();
        name = "mod_integrations";
        item = Items.PISTON;

        settingsGui.setSlot(16, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openModIntegrationsGui(player))
        );

        settingsGui.open();
    }

    public static void openGameSettingsGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui gameSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        gameSettingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("Remove", true);

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;

        loreList = new ArrayList<>();
        name = "set_motd";
        item = Items.REPEATING_COMMAND_BLOCK;
        boolvalue = config.isSetMotd();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var setMotdBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setSetMotd(config.isSetMotdDefault());
                            } else {
                                config.setSetMotd(!setMotdBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "team_preset";
        item = Items.FLETCHING_TABLE;
        intvalue = config.getTeamPreset();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.free_select").formatted(Formatting.GREEN), Text.translatable("item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.runner_cycle"), Text.translatable("item_lore.team_preset.hunter_infection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.team_preset.no_selection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.free_select"), Text.translatable("item_lore.team_preset.speedrun_showdown").formatted(Formatting.YELLOW)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.runner_cycle"), Text.translatable("item_lore.team_preset.hunter_infection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.team_preset.no_selection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 3) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.free_select"), Text.translatable("item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.runner_cycle").formatted(Formatting.GOLD), Text.translatable("item_lore.team_preset.hunter_infection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.team_preset.no_selection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 4) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.free_select"), Text.translatable("item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.runner_cycle"), Text.translatable("item_lore.team_preset.hunter_infection").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.team_preset.no_selection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.free_select"), Text.translatable("item_lore.team_preset.speedrun_showdown")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.team_preset.runner_cycle"), Text.translatable("item_lore.team_preset.hunter_infection")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.team_preset.no_selection").formatted(Formatting.DARK_RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var teamPresetInt = intvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setTeamPreset(config.getTeamPresetDefault());
                            } else {
                                if (teamPresetInt == 1) {
                                    config.setTeamPreset(2);
                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                        );
                                    }

                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.set_role", Text.literal("Everyone").formatted(config.getRunnersColor()), Text.translatable("role.manhunt.runner")), false);
                                } else if (teamPresetInt == 2) {
                                    config.setTeamPreset(3);
                                    if (playerList == null || playerList.isEmpty()) {
                                        playerList = new ArrayList<>(player.getServer().getPlayerManager().getPlayerList());
                                    }

                                    for (ServerPlayerEntity serverPlayer : playerList) {
                                        ServerPlayerEntity serverPlayerEntity = playerList.get(0);
                                        if (serverPlayerEntity == null) {
                                            playerList.remove(serverPlayer);
                                        }
                                    }

                                    ServerPlayerEntity runner = playerList.get(0);

                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                                        );
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(
                                            runner.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                    );
                                    playerList.remove(runner);
                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.one_role", Text.literal(runner.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("role.manhunt.runner")), false);
                                } else if (teamPresetInt == 3) {
                                    config.setTeamPreset(4);
                                    List<ServerPlayerEntity> players = new ArrayList<>(player.getServer().getPlayerManager().getPlayerList());
                                    Collections.shuffle(players);
                                    ServerPlayerEntity hunter = players.get(0);

                                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                        );
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(
                                            hunter.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                                    );
                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.one_role", Text.literal(hunter.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("role.manhunt.hunter").formatted(config.getHuntersColor())), false);
                                } else if (teamPresetInt == 4) {
                                    config.setTeamPreset(5);
                                } else {
                                    config.setTeamPreset(1);
                                }
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "automatic_compass";
        item = Items.COMPASS;
        boolvalue = config.isAutomaticCompass();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var automaticCompassBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setAutomaticCompass(config.isAutomaticCompassDefault());
                            } else {
                                config.setAutomaticCompass(!automaticCompassBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "team_color";
        item = Items.WHITE_BANNER;
        boolvalue = config.isTeamColor();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var teamColorBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorBool))
        );
        slot++;

        loreList = new ArrayList<>();
        name = "nametag_color";
        item = Items.NAME_TAG;
        boolvalue = config.isNametagColor();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var teamSuffixBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setNametagColor(config.isNameTagDefault());
                            } else {
                                config.setNametagColor(!teamSuffixBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_headstart";
        item = Items.GOLDEN_BOOTS;
        intvalue = config.getRunnerHeadstart();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 10 && intvalue != 20 && intvalue != 30) {
            loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 10) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("10").formatted(Formatting.RED), Text.literal("20"), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 20) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("10"), Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("10"), Text.literal("20"), Text.literal("30").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int runnerHeadstartInt = intvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) -> {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
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
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
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
                                                        player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    config.setRunnerHeadstart(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };
                                runnerHeadstartGui.setTitle(Text.translatable("text.enter_value"));
                                runnerHeadstartGui.setDefaultInputValue("");
                                runnerHeadstartGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "time_limit";
        item = Items.CLOCK;
        intvalue = config.getTimeLimit();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 30 && intvalue != 60 && intvalue != 90) {
            loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 30) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("30").formatted(Formatting.RED), Text.literal("60"), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 60) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("30"), Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("30"), Text.literal("60"), Text.literal("90").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int timeLimitInt = intvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
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
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
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
                                                        player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    config.setTimeLimit(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };
                                timeLimitGui.setTitle(Text.translatable("text.enter_value"));
                                timeLimitGui.setDefaultInputValue("");
                                timeLimitGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_glow";
        item = Items.GLOWSTONE;
        boolvalue = config.isRunnersGlow();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersGlowBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersGlow(config.isRunnersGlowDefault());
                            } else {
                                config.setRunnersGlow(!runnersGlowBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        gameSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "world_border";
        item = Items.PRISMARINE_WALL;
        intvalue = config.getWorldBorder();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 5632 && intvalue != 11776 && intvalue != 59999968) {
            loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 5632) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 11776) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1st ring"), Text.literal("2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1st ring"), Text.literal("2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int worldBorderInt = intvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    config.setWorldBorder(config.getWorldBorderDefault());
                                } else {
                                    if (worldBorderInt != 5632 && worldBorderInt != 11776) {
                                        config.setWorldBorder(5632);
                                    } else {
                                        if (worldBorderInt == 5632) {
                                            config.setWorldBorder(11776);
                                        } else {
                                            config.setWorldBorder(59999968);
                                        }
                                    }
                                }
                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
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
                                                        player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    config.setWorldBorder(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };
                                worldBorderGui.setTitle(Text.translatable("text.enter_value"));
                                worldBorderGui.setDefaultInputValue("");
                                worldBorderGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spectate_on_win";
        item = Items.SPYGLASS;
        boolvalue = config.isSpectateOnWin();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var spectateWinBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setSpectateOnWin(config.isSpectateWinDefault());
                            } else {
                                config.setSpectateOnWin(!spectateWinBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_hunt_on_death";
        item = Items.SKELETON_SKULL;
        boolvalue = config.isRunnersHuntOnDeath();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersHuntOnDeathBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersHuntOnDeath(config.isRunnersHuntOnDeathDefault());
                            } else {
                                config.setRunnersHuntOnDeath(!runnersHuntOnDeathBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_can_pause";
        item = Items.BLUE_ICE;
        boolvalue = config.isRunnersCanPause();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnersCanPauseBool = boolvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnersCanPause(config.isRunnersCanPauseDefault());
                            } else {
                                config.setRunnersCanPause(!runnersCanPauseBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGameSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_leaving_pause_time";
        item = Items.PRISMARINE;
        intvalue = config.getRunnerLeavingPauseTime();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".third").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 1 && intvalue != 2 && intvalue != 5) {
            loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 1) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1").formatted(Formatting.GREEN), Text.literal("2"), Text.literal("5")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 2) {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1"), Text.literal("2").formatted(Formatting.YELLOW), Text.literal("5")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("item_lore.triple", Text.literal("1"), Text.literal("2"), Text.literal("5").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int pauseTimeOnLeaveInt = intvalue;
        gameSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (!type.shift) {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    config.setRunnerLeavingPauseTime(config.getRunnerLeavingPauseTimeDefault());
                                } else {
                                    if (pauseTimeOnLeaveInt != 1 && pauseTimeOnLeaveInt != 2) {
                                        config.setRunnerLeavingPauseTime(1);
                                    } else {
                                        if (pauseTimeOnLeaveInt == 1) {
                                            config.setRunnerLeavingPauseTime(2);
                                        } else {
                                            config.setRunnerLeavingPauseTime(5);
                                        }
                                    }
                                }
                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
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
                                                        player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    config.setRunnerLeavingPauseTime(value);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openGameSettingsGui(player);
                                                })
                                        );
                                    }
                                };
                                pauseTimeOnLeaveGui.setTitle(Text.translatable("text.enter_value"));
                                pauseTimeOnLeaveGui.setDefaultInputValue("");
                                pauseTimeOnLeaveGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        gameSettingsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));
        gameSettingsGui.setSlot(26, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        gameSettingsGui.open();
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Boolean boolvalue) {
        if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
        if (checkPermission(player, "manhunt.settings")) {
            if (slowDownManager.get(player.getUuid()) < 4) {
                if (!clickType.shift) {
                    if (clickType == ClickType.DROP) {
                        config.setTeamColor(config.isTeamColorDefault());
                    } else {
                        config.setTeamColor(!boolvalue);

                        if (config.isTeamColor()) {
                            player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                            player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                        } else {
                            player.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
                            player.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
                        }
                    }
                    config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                    openGameSettingsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.literal(config.getHuntersColor().name()).setStyle(Style.EMPTY.withColor(config.getHuntersColor()).withItalic(false)));
                    loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                            .setName(Text.translatable("setting.hunters_color").formatted(config.getHuntersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (checkPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 4) {
                                        if (type == ClickType.DROP) {
                                            config.setHuntersColor(config.getHuntersColorDefault());

                                            config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                            openGameSettingsGui(player);
                                        } else {
                                            SimpleGui huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                            huntersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.RESET);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.GRAY);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.DARK_GRAY);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.BLACK);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.RED);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.GOLD);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.YELLOW);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                                                    .setCallback(teamColorGui::open)
                                            );

                                            huntersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.GREEN);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.DARK_GREEN);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.DARK_AQUA);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.BLUE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.DARK_BLUE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.DARK_PURPLE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        config.setHuntersColor(Formatting.LIGHT_PURPLE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.open();
                                        }
                                    }
                                }
                            })
                    );

                    loreList = new ArrayList<>();

                    loreList.add(Text.literal(config.getRunnersColor().name()).setStyle(Style.EMPTY.withColor(config.getRunnersColor()).withItalic(false)));
                    loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK)
                            .setName(Text.translatable("setting.runners_color").formatted(config.getRunnersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (checkPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 4) {
                                        if (type == ClickType.DROP) {
                                            config.setRunnersColor(config.getRunnersColorDefault());
                                            config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                            openGameSettingsGui(player);
                                        } else {
                                            SimpleGui runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                            runnersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.RESET);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.GRAY);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.DARK_GRAY);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.BLACK);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.RED);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.GOLD);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.YELLOW);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                                                    .setCallback(teamColorGui::open)
                                            );

                                            runnersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.GREEN);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.DARK_GREEN);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.DARK_AQUA);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.BLUE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.DARK_BLUE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.DARK_PURPLE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        config.setRunnersColor(Formatting.LIGHT_PURPLE);
                                                        config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.open();
                                        }
                                    }
                                }
                            })
                    );

                    teamColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                            .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                            .setCallback(() -> openGameSettingsGui(player))
                    );

                    teamColorGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
        }
    }

    public static void openGlobalPreferencesGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui globalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        globalPreferencesGui.setTitle(Text.translatable("menu_category.global_preferences"));

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

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var gameTitlesBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setGameTitles(config.isGameTitlesDefault());
                            } else {
                                config.setGameTitles(!gameTitlesBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhunt_sounds";
        item = Items.FIRE_CHARGE;
        bool = config.isManhuntSounds();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var manhuntSoundsBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setManhuntSounds(config.isManhuntSoundsDefault());
                            } else {
                                config.setManhuntSounds(!manhuntSoundsBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        bool = config.isNightVision();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var nightVisionBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setNightVision(config.isNightVisionDefault());
                            } else {
                                config.setNightVision(!nightVisionBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        integer = config.getFriendlyFire();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 1) {
            loreList.add(Text.translatable("item_lore.triple", Text.translatable("item_lore." + name + ".always").formatted(Formatting.GREEN), Text.translatable("item_lore." + name + ".per_player"), Text.translatable("item_lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer == 2) {
            loreList.add(Text.translatable("item_lore.triple", Text.translatable("item_lore." + name + ".always"), Text.translatable("item_lore." + name + ".per_player").formatted(Formatting.YELLOW), Text.translatable("item_lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.triple", Text.translatable("item_lore." + name + ".always"), Text.translatable("item_lore." + name + ".per_player"), Text.translatable("item_lore." + name + ".never").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        int friendlyFireInt = integer;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
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
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        bool = config.isBedExplosions();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setBedExplosions(config.isBedExplosionsDefault());
                            } else {
                                config.setBedExplosions(!bedExplosionsBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        bool = config.isLavaPvpInNether();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setLavaPvpInNether(config.isLavaPvpInNetherDefault());
                            } else {
                                config.setLavaPvpInNether(!lavaPvpInNetherBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;
        bool = config.isRunnerPreferences();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        var runnerPreferencesBool = bool;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("menu_category." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (type == ClickType.DROP) {
                                config.setRunnerPreferences(config.isRunnerPreferencesDefault());
                            } else {
                                config.setRunnerPreferences(!runnerPreferencesBool);
                            }
                            config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                })
        );

        globalPreferencesGui.open();
    }

    public static void openTitleTextsGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui titleTextsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        titleTextsGui.setTitle(Text.translatable("menu_category.title_texts"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item = Items.PAPER;

        loreList = new ArrayList<>();
        name = "game_start";

        loreList.add(Text.literal("\"" + config.getGameStartTitle() + "\"").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("titletext." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                            if (type == ClickType.DROP) {
                                config.setGameStartTitle(config.getGameStartTitleDefault());
                            } else {
                                AnvilInputGui gameStartTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    config.setGameStartTitle(input);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
                                    }
                                };
                                gameStartTitleGui.setTitle(Text.translatable("text.enter_value"));
                                gameStartTitleGui.setDefaultInputValue("");
                                gameStartTitleGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "game_start.subtitle";

        loreList.add(Text.literal("\"" + config.getGameStartSubtitle() + "\"").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("titletext." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                            if (type == ClickType.DROP) {
                                config.setGameStartSubtitle(config.getGameStartSubtitleDefault());
                            } else {
                                AnvilInputGui gameStartSubtitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    config.setGameStartSubtitle(input);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
                                    }
                                };
                                gameStartSubtitleGui.setTitle(Text.translatable("text.enter_value"));
                                gameStartSubtitleGui.setDefaultInputValue("");
                                gameStartSubtitleGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "game_paused";

        loreList.add(Text.literal("\"" + config.getGamePausedTitle() + "\"").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("titletext." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (checkPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 4) {
                            if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                            if (type == ClickType.DROP) {
                                config.setGamePausedTitle(config.getGamePausedTitleDefault());
                                config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                openTitleTextsGui(player);
                            } else {
                                AnvilInputGui gamePausedTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    config.setGamePausedTitle(input);
                                                    config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
                                    }
                                };
                                gamePausedTitleGui.setTitle(Text.translatable("text.enter_value"));
                                gamePausedTitleGui.setDefaultInputValue("");
                                gamePausedTitleGui.open();
                            }
                        } else {
                            player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        titleTextsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back")
                        .formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                    openSettingsGui(player);
                })
        );

        titleTextsGui.open();
    }

    public static void openModIntegrationsGui(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

        SimpleGui modIntegrationsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        modIntegrationsGui.setTitle(Text.translatable("menu_category.mod_integrations"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "chunky";
        item = Items.NETHER_STAR;
        boolvalue = config.isChunky();

        loreList.add(Text.translatable("item_lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on").formatted(Formatting.GREEN), Text.translatable("item_lore.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("item_lore.double", Text.translatable("item_lore.on"), Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var preloadChunksBool = boolvalue;
        modIntegrationsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("integration." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> openChunkyIntegrationGui(player, type, preloadChunksBool))
        );

        modIntegrationsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.go_back")
                        .formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                    openSettingsGui(player);
                })
        );

        modIntegrationsGui.open();
    }

    private static void openChunkyIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean boolvalue) {
        if (checkPermission(player, "manhunt.settings")) {
            if (slowDownManager.get(player.getUuid()) < 4) {
                if (!clickType.shift) {
                    if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);

                    if (clickType == ClickType.DROP) {
                        config.setChunky(config.isChunkyIntegrationDefault());
                    } else {
                        config.setChunky(!boolvalue);
                    }
                    config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                    openModIntegrationsGui(player);

                    if (!boolvalue) {
                        schedulePreload(player.getServer());
                    } else {
                        if (chunkyLoaded) {
                            ChunkyAPI chunky = ChunkyProvider.get().getApi();

                            chunky.cancelTask("manhunt:overworld");
                            chunky.cancelTask("manhunt:the_nether");
                            chunky.cancelTask("manhunt:the_end");
                        }
                    }
                } else {
                    var chunkyIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));

                    List<Text> loreList;
                    String name;
                    int slot = 0;
                    Item item;
                    int intvalue;

                    loreList = new ArrayList<>();
                    name = "flat_world_preset.minecraft.overworld";
                    item = Items.GRASS_BLOCK;
                    intvalue = config.getOverworld();

                    loreList.add(Text.translatable("world.manhunt.lore").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue != 2000 && intvalue != 4000 && intvalue != 8000) {
                        loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        if (intvalue == 2000) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("2000").formatted(Formatting.RED), Text.literal("4000"), Text.literal("8000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else if (intvalue == 4000) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("2000"), Text.literal("4000").formatted(Formatting.YELLOW), Text.literal("8000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("2000"), Text.literal("4000"), Text.literal("8000").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        }
                    }
                    loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                    var overworldInt = intvalue;
                    chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                            .setName(Text.translatable(name))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (checkPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 4) {
                                        if (!type.shift) {
                                            if (type == ClickType.DROP) {
                                                config.setOverworld(config.getOverworldDefault());
                                            } else {
                                                if (overworldInt != 2000 && overworldInt != 4000) {
                                                    config.setOverworld(2000);
                                                } else {
                                                    if (overworldInt == 2000) {
                                                        config.setOverworld(4000);
                                                    } else {
                                                        config.setOverworld(8000);
                                                    }
                                                }
                                            }
                                            config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                            openChunkyIntegrationGui(player, clickType, boolvalue);
                                        } else {
                                            AnvilInputGui overworldGui = new AnvilInputGui(player, false) {
                                                @Override
                                                public void onInput(String input) {
                                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                            .setCallback(() -> {
                                                                int value = overworldInt;
                                                                try {
                                                                    value = Integer.parseInt(input);
                                                                } catch (NumberFormatException e) {
                                                                    player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                                }
                                                                config.setOverworld(value);
                                                                config.save();
                                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                                openChunkyIntegrationGui(player, clickType, boolvalue);

                                                                if (value == 0) {
                                                                    schedulePreload(player.getServer());
                                                                } else {
                                                                    if (chunkyLoaded) {
                                                                        ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                        chunky.cancelTask("manhunt:overworld");
                                                                    }
                                                                }
                                                            })
                                                    );
                                                }
                                            };
                                            overworldGui.setTitle(Text.translatable("text.enter_value"));
                                            overworldGui.setDefaultInputValue("");
                                            overworldGui.open();
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                                    }
                                } else {
                                    player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                                }
                            })
                    );
                    slot++;

                    loreList = new ArrayList<>();
                    name = "the_nether";
                    item = Items.NETHERRACK;
                    intvalue = config.getNether();

                    loreList.add(Text.translatable("world.manhunt.lore").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                        loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        if (intvalue == 250) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal("1000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else if (intvalue == 500) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250"), Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250"), Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        }
                    }
                    loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                    var netherInt = intvalue;
                    chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                            .setName(Text.translatable("world.manhunt." + name))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (checkPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 4) {
                                        if (!type.shift) {
                                            if (type == ClickType.DROP) {
                                                config.setNether(config.getNetherDefault());
                                            } else {
                                                if (netherInt != 250 && netherInt != 500) {
                                                    config.setNether(250);
                                                } else {
                                                    if (netherInt == 250) {
                                                        config.setNether(500);
                                                    } else {
                                                        config.setNether(1000);
                                                    }
                                                }
                                            }
                                            config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                            openChunkyIntegrationGui(player, clickType, boolvalue);
                                        } else {
                                            AnvilInputGui netherGui = new AnvilInputGui(player, false) {
                                                @Override
                                                public void onInput(String input) {
                                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                            .setCallback(() -> {
                                                                int value = netherInt;
                                                                try {
                                                                    value = Integer.parseInt(input);
                                                                } catch (NumberFormatException e) {
                                                                    player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                                }
                                                                config.setNether(value);
                                                                config.save();
                                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                                openChunkyIntegrationGui(player, clickType, boolvalue);

                                                                if (value == 0) {
                                                                    schedulePreload(player.getServer());
                                                                } else {
                                                                    if (chunkyLoaded) {
                                                                        ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                        chunky.cancelTask("manhunt:the_nether");
                                                                    }
                                                                }
                                                            })
                                                    );
                                                }
                                            };
                                            netherGui.setTitle(Text.translatable("text.enter_value"));
                                            netherGui.setDefaultInputValue("");
                                            netherGui.open();
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                                    }
                                } else {
                                    player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                                }
                            })
                    );
                    slot++;

                    loreList = new ArrayList<>();
                    name = "the_end";
                    item = Items.END_STONE;
                    intvalue = config.getEnd();

                    loreList.add(Text.translatable("world.manhunt.lore").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("item_lore.single", Text.translatable("item_lore.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                        loreList.add(Text.translatable("item_lore.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        if (intvalue == 250) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal("1000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else if (intvalue == 500) {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250"), Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        } else {
                            loreList.add(Text.translatable("item_lore.triple", Text.literal("250"), Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
                        }
                    }
                    loreList.add(Text.translatable("item_lore.click_drop").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    loreList.add(Text.translatable("item_lore.click_shift").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                    var endInt = intvalue;
                    chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                            .setName(Text.translatable("world.manhunt." + name))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (checkPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 4) {
                                        if (!type.shift) {
                                            if (type == ClickType.DROP) {
                                                config.setEnd(config.getEndDefault());
                                            } else {
                                                if (endInt != 250 && endInt != 500) {
                                                    config.setOverworld(250);
                                                } else {
                                                    if (endInt == 250) {
                                                        config.setEnd(500);
                                                    } else {
                                                        config.setEnd(1000);
                                                    }
                                                }
                                            }
                                            config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                            openChunkyIntegrationGui(player, clickType, boolvalue);
                                        } else {
                                            AnvilInputGui endGui = new AnvilInputGui(player, false) {
                                                @Override
                                                public void onInput(String input) {
                                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                            .setCallback(() -> {
                                                                int value = endInt;
                                                                try {
                                                                    value = Integer.parseInt(input);
                                                                } catch (NumberFormatException e) {
                                                                    player.sendMessage(Text.translatable("chat.invalid_input").formatted(Formatting.RED));
                                                                }
                                                                config.setEnd(value);
                                                                config.save();
                                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5f, 1f, player.getWorld().random.nextLong()));
                                                                openChunkyIntegrationGui(player, clickType, boolvalue);

                                                                if (value == 0) {
                                                                    schedulePreload(player.getServer());
                                                                } else {
                                                                    if (chunkyLoaded) {
                                                                        ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                        chunky.cancelTask("manhunt:the_end");
                                                                    }
                                                                }
                                                            })
                                                    );
                                                }
                                            };
                                            endGui.setTitle(Text.translatable("text.enter_value"));
                                            endGui.setDefaultInputValue("");
                                            endGui.open();
                                        }
                                    } else {
                                        player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                                    }
                                } else {
                                    player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
                                }
                            })
                    );

                    chunkyIntegrationGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                            .setName(Text.translatable("text.go_back").formatted(Formatting.WHITE))
                            .setCallback(() -> openModIntegrationsGui(player))
                    );

                    chunkyIntegrationGui.open();
                }
            } else {
                player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
            }
        } else {
            player.sendMessage(Text.translatable("chat.no_permission").formatted(Formatting.RED));
        }
    }
}
