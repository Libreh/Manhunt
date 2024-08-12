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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.*;

public class ManhuntSettings {
    public static final HashMap<UUID, Boolean> customTitles = new HashMap<>();
    public static final HashMap<UUID, Boolean> customSounds = new HashMap<>();
    public static final HashMap<UUID, Boolean> customParticles = new HashMap<>();
    public static final HashMap<UUID, Boolean> automaticCompass = new HashMap<>();
    public static final HashMap<UUID, Boolean> nightVision = new HashMap<>();
    public static final HashMap<UUID, Boolean> friendlyFire = new HashMap<>();
    public static final HashMap<UUID, Boolean> bedExplosions = new HashMap<>();
    public static final HashMap<UUID, Boolean> lavaPvpInNether = new HashMap<>();
    public static final HashMap<UUID, Integer> slowDownManager = new HashMap<>();
    public static List<ServerPlayerEntity> playerList = new ArrayList<>();

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

        preferencesGui.setSlot(11, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openPersonalPreferencesGui(player))
        );

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));

        preferencesGui.setSlot(15, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openRunnerPreferencesGui(player))
        );

        preferencesGui.open();
    }

    public static void openPersonalPreferencesGui(ServerPlayerEntity player) {
        SimpleGui personalPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        personalPreferencesGui.setTitle(Text.translatable("category.manhunt.personal_preferences"));

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "custom_titles";
        item = Items.OAK_SIGN;
        boolvalue = customTitles.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean customTitlesBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            customTitles.put(player.getUuid(), ManhuntConfig.config.isCustomTitlesDefault());
                        } else {
                            customTitles.put(player.getUuid(), !customTitlesBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.GOAT_HORN;
        boolvalue = customSounds.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean customSoundsBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .hideDefaultTooltip()
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            customSounds.put(player.getUuid(), ManhuntConfig.config.isCustomSoundsDefault());
                        } else {
                            customSounds.put(player.getUuid(), !customSoundsBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "custom_particles";
        item = Items.BLAZE_POWDER;
        boolvalue = customParticles.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean customParticlesBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            customParticles.put(player.getUuid(), ManhuntConfig.config.isCustomParticlesDefault());
                        } else {
                            customParticles.put(player.getUuid(), !customParticlesBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "automatic_compass";
        item = Items.COMPASS;
        boolvalue = automaticCompass.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean automaticCompassBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .hideDefaultTooltip()
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            automaticCompass.put(player.getUuid(), ManhuntConfig.config.isAutomaticCompassDefault());
                        } else {
                            automaticCompass.put(player.getUuid(), !automaticCompassBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GOLDEN_CARROT;
        boolvalue = nightVision.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean nightVisionBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            nightVision.put(player.getUuid(), false);
                        } else {
                            nightVision.put(player.getUuid(), !nightVisionBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        boolvalue = friendlyFire.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        boolean friendlyFireBool = boolvalue;
        personalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            friendlyFire.put(player.getUuid(), true);
                        } else {
                            friendlyFire.put(player.getUuid(), !friendlyFireBool);
                        }
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPersonalPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        personalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openPreferencesGui(player);
                })
        );

        personalPreferencesGui.open();
    }

    public static void openRunnerPreferencesGui(ServerPlayerEntity player) {
        SimpleGui runnerPreferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        runnerPreferencesGui.setTitle(Text.translatable("category.manhunt.runner_preferences"));

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean value;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        value = bedExplosions.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = value;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            bedExplosions.put(player.getUuid(), ManhuntConfig.config.isBedExplosionsDefault());
                        } else {
                            bedExplosions.put(player.getUuid(), !bedExplosionsBool);
                        }
                        ManhuntConfig.config.save();
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        value = lavaPvpInNether.get(player.getUuid());

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (value) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = value;
        runnerPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (type == ClickType.DROP) {
                            lavaPvpInNether.put(player.getUuid(), ManhuntConfig.config.isLavaPvpInNetherDefault());
                        } else {
                            lavaPvpInNether.put(player.getUuid(), !lavaPvpInNetherBool);
                        }
                        ManhuntConfig.config.save();
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openRunnerPreferencesGui(player);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );

        runnerPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openPreferencesGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
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
        name = "manhunt_settings";
        item = Items.COMPARATOR;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(10, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name).styled(style -> style.withColor(Formatting.WHITE).withItalic(false)))
                .setLore(loreList)
                .setSkullOwner(player.getGameProfile(), player.getServer())
                .setCallback((index, type, action) -> openManhuntSettingsGui(player))
        );

        loreList = new ArrayList<>();
        name = "global_preferences";
        item = Items.WRITABLE_BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(12, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openGlobalPreferencesGui(player))
        );

        loreList = new ArrayList<>();
        name = "title_texts";
        item = Items.BOOK;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(14, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTitleTextsGui(player))
        );

        loreList = new ArrayList<>();
        name = "mod_integrations";
        item = Items.PISTON;

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        settingsGui.setSlot(16, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openModIntegrationsGui(player))
        );

        settingsGui.open();
    }

    public static void openManhuntSettingsGui(ServerPlayerEntity player) {
        SimpleGui manhuntSettingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

        manhuntSettingsGui.setTitle(Text.translatable("item.manhunt.settings"));

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;

        loreList = new ArrayList<>();
        name = "set_motd";
        item = Items.REPEATING_COMMAND_BLOCK;
        boolvalue = ManhuntConfig.config.isSetMotd();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var setMotdBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setSetMotd(ManhuntConfig.config.isSetMotdDefault());
                            } else {
                                ManhuntConfig.config.setSetMotd(!setMotdBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "role_preset";
        item = Items.FLETCHING_TABLE;
        intvalue = ManhuntConfig.config.getRolePreset();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.free_select").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.role_preset.speedrun_showdown")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.runner_cycle"), Text.translatable("lore.manhunt.role_preset.hunter_infection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset.no_selection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.free_select"), Text.translatable("lore.manhunt.role_preset.speedrun_showdown").formatted(Formatting.YELLOW)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.runner_cycle"), Text.translatable("lore.manhunt.role_preset.hunter_infection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset.no_selection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 3) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.free_select"), Text.translatable("lore.manhunt.role_preset.speedrun_showdown")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.runner_cycle").formatted(Formatting.GOLD), Text.translatable("lore.manhunt.role_preset.hunter_infection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset.no_selection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 4) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.free_select"), Text.translatable("lore.manhunt.role_preset.speedrun_showdown")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.runner_cycle"), Text.translatable("lore.manhunt.role_preset.hunter_infection").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset.no_selection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.free_select"), Text.translatable("lore.manhunt.role_preset.speedrun_showdown")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.role_preset.runner_cycle"), Text.translatable("lore.manhunt.role_preset.hunter_infection")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.role_preset.no_selection").formatted(Formatting.DARK_RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var rolePresetInt = intvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setRolePreset(ManhuntConfig.config.getRolePresetDefault());
                            } else {
                                MinecraftServer server = player.getServer();
                                if (rolePresetInt == 1) {
                                    ManhuntConfig.config.setRolePreset(2);
                                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                        );
                                    }

                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.set_role",
                                                    Text.literal("Everyone").formatted(ManhuntConfig.config.getRunnersColor()),
                                                    Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.config.getRunnersColor())),
                                            false
                                    );
                                } else if (rolePresetInt == 2) {
                                    ManhuntConfig.config.setRolePreset(3);
                                    if (playerList == null || playerList.isEmpty()) {
                                        playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                                    }

                                    playerList.removeIf(Objects::isNull);

                                    ServerPlayerEntity runner = playerList.getFirst();

                                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                                        );
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(
                                            runner.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                    );
                                    playerList.remove(runner);
                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                            Text.literal(runner.getNameForScoreboard()).formatted(ManhuntConfig.config.getRunnersColor()),
                                            Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.config.getRunnersColor())), false
                                    );
                                } else if (rolePresetInt == 3) {
                                    ManhuntConfig.config.setRolePreset(4);
                                    List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
                                    Collections.shuffle(players);
                                    ServerPlayerEntity hunter = players.getFirst();

                                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                        serverPlayer.getScoreboard().addScoreHolderToTeam(
                                                serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners")
                                        );
                                    }

                                    player.getScoreboard().addScoreHolderToTeam(
                                            hunter.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                                    );
                                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                                                    Text.literal(hunter.getNameForScoreboard()).formatted(ManhuntConfig.config.getHuntersColor()),
                                                    Text.translatable("role.manhunt.hunter").formatted(ManhuntConfig.config.getHuntersColor())),
                                            false
                                    );
                                } else if (rolePresetInt == 4) {
                                    ManhuntConfig.config.setRolePreset(5);
                                } else {
                                    ManhuntConfig.config.setRolePreset(1);
                                }
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "team_color";
        item = Items.WHITE_BANNER;
        boolvalue = ManhuntConfig.config.isTeamColor();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var teamColorBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorBool))
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_head_start";
        item = Items.GOLDEN_BOOTS;
        intvalue = ManhuntConfig.config.getRunnerHeadStart();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 10 && intvalue != 20 && intvalue != 30) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(ManhuntConfig.config.getRunnerHeadStart())).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 10) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10").formatted(Formatting.RED), Text.literal("20"), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 20) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"), Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("10"), Text.literal("20"), Text.literal("30").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int runnerHeadstartInt = intvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (slowDownManager.get(player.getUuid()) < 12)
                                    slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.config.setRunnerHeadStart(ManhuntConfig.config.getRunnerHeadStartDefault());
                                } else {
                                    if (runnerHeadstartInt != 10 && runnerHeadstartInt != 20) {
                                        ManhuntConfig.config.setRunnerHeadStart(10);
                                    } else {
                                        if (runnerHeadstartInt == 10) {
                                            ManhuntConfig.config.setRunnerHeadStart(20);
                                        } else {
                                            ManhuntConfig.config.setRunnerHeadStart(30);
                                        }
                                    }
                                }
                                ManhuntConfig.config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openManhuntSettingsGui(player);
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
                                                        player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.config.setRunnerHeadStart(value);
                                                    ManhuntConfig.config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openManhuntSettingsGui(player);
                                                })
                                        );
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
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_glow";
        item = Items.GLOWSTONE;
        boolvalue = ManhuntConfig.config.isRunnersGlow();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersGlowBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setRunnersGlow(ManhuntConfig.config.isRunnersGlowDefault());
                            } else {
                                ManhuntConfig.config.setRunnersGlow(!runnersGlowBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "hunt_on_death";
        item = Items.SKELETON_SKULL;
        boolvalue = ManhuntConfig.config.isHuntOnDeath();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersHuntOnDeathBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setHuntOnDeath(ManhuntConfig.config.isHuntOnDeathDefault());
                            } else {
                                ManhuntConfig.config.setHuntOnDeath(!runnersHuntOnDeathBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runners_can_pause";
        item = Items.BLUE_ICE;
        boolvalue = ManhuntConfig.config.isRunnersCanPause();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnersCanPauseBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setRunnersCanPause(ManhuntConfig.config.isRunnersCanPauseDefault());
                            } else {
                                ManhuntConfig.config.setRunnersCanPause(!runnersCanPauseBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "leave_pause_time";
        item = Items.PRISMARINE;
        intvalue = ManhuntConfig.config.getLeavePauseTime();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 1 && intvalue != 2 && intvalue != 5) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 1) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1").formatted(Formatting.GREEN), Text.literal("2"), Text.literal("5")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 2) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1"), Text.literal("2").formatted(Formatting.YELLOW), Text.literal("5")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1"), Text.literal("2"), Text.literal("5").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int pauseTimeOnLeaveInt = intvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 12)
                            slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (!type.shift) {
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.config.setLeavePauseTime(ManhuntConfig.config.getLeavePauseTimeDefault());
                                } else {
                                    if (pauseTimeOnLeaveInt != 1 && pauseTimeOnLeaveInt != 2) {
                                        ManhuntConfig.config.setLeavePauseTime(1);
                                    } else {
                                        if (pauseTimeOnLeaveInt == 1) {
                                            ManhuntConfig.config.setLeavePauseTime(2);
                                        } else {
                                            ManhuntConfig.config.setLeavePauseTime(5);
                                        }
                                    }
                                }
                                ManhuntConfig.config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openManhuntSettingsGui(player);
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
                                                        player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                    }
                                                    ManhuntConfig.config.setLeavePauseTime(value);
                                                    ManhuntConfig.config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openManhuntSettingsGui(player);
                                                })
                                        );
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
                })
        );
        slot++;

        manhuntSettingsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "time_limit";
        item = Items.CLOCK;
        intvalue = ManhuntConfig.config.getTimeLimit();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 0) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue != 30 && intvalue != 60 && intvalue != 90) {
            loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(ManhuntConfig.config.getTimeLimit())).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (intvalue == 30) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30").formatted(Formatting.RED), Text.literal("60"), Text.literal("90")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else if (intvalue == 60) {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30"), Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("30"), Text.literal("60"), Text.literal("90").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        int timeLimitInt = intvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 6) {
                        if (!type.shift) {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 12)
                                    slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                                if (type == ClickType.DROP) {
                                    ManhuntConfig.config.setTimeLimit(ManhuntConfig.config.getTimeLimitDefault());
                                } else {
                                    if (timeLimitInt != 30 && timeLimitInt != 60) {
                                        ManhuntConfig.config.setTimeLimit(30);
                                    } else {
                                        if (timeLimitInt == 30) {
                                            ManhuntConfig.config.setTimeLimit(60);
                                        } else {
                                            ManhuntConfig.config.setTimeLimit(90);
                                        }
                                    }
                                }
                                ManhuntConfig.config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openManhuntSettingsGui(player);
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
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
                                                    player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                }
                                                ManhuntConfig.config.setTimeLimit(value);
                                                ManhuntConfig.config.save();
                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                openManhuntSettingsGui(player);
                                            })
                                    );
                                }
                            };
                            timeLimitGui.setTitle(Text.translatable("text.manhunt.enter_value"));
                            timeLimitGui.setDefaultInputValue("");
                            timeLimitGui.open();
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spectate_on_win";
        item = Items.SPYGLASS;
        boolvalue = ManhuntConfig.config.isSpectateOnWin();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var spectateWinBool = boolvalue;
        manhuntSettingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setSpectateOnWin(ManhuntConfig.config.isSpectateOnWinDefault());
                            } else {
                                ManhuntConfig.config.setSpectateOnWin(!spectateWinBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openManhuntSettingsGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        manhuntSettingsGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        manhuntSettingsGui.open();
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Boolean boolvalue) {
        if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
            if (slowDownManager.get(player.getUuid()) < 6) {
                if (!clickType.shift) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.config.setTeamColor(ManhuntConfig.config.isTeamColorDefault());
                    } else {
                        ManhuntConfig.config.setTeamColor(!boolvalue);

                        if (ManhuntConfig.config.isTeamColor()) {
                            player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                            player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                        } else {
                            player.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
                            player.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
                        }
                    }

                    ManhuntConfig.config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openManhuntSettingsGui(player);
                } else {
                    var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                    teamColorGui.setTitle(Text.translatable("setting.manhunt.team_color"));

                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

                    List<Text> loreList = new ArrayList<>();

                    loreList.add(Text.literal(ManhuntConfig.config.getHuntersColor().name()).styled(style -> style.withColor(ManhuntConfig.config.getHuntersColor()).withItalic(false)));
                    loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                            .setName(Text.translatable("setting.manhunt.hunters_color").formatted(ManhuntConfig.config.getHuntersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 6) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setHuntersColor(ManhuntConfig.config.getHuntersColorDefault());

                                            ManhuntConfig.config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                            openManhuntSettingsGui(player);
                                        } else {
                                            SimpleGui huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                            huntersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.RESET);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.GRAY);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.DARK_GRAY);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.BLACK);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.RED);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.GOLD);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.YELLOW);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                                                    .setCallback(teamColorGui::open)
                                            );

                                            huntersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.GREEN);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.DARK_GREEN);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.DARK_AQUA);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.BLUE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.DARK_BLUE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.DARK_PURPLE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            huntersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setHuntersColor(Formatting.LIGHT_PURPLE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("hunters").setColor(ManhuntConfig.config.getHuntersColor());
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

                    loreList.add(Text.literal(ManhuntConfig.config.getRunnersColor().name()).styled(style -> style.withColor(ManhuntConfig.config.getRunnersColor()).withItalic(false)));
                    loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                    teamColorGui.setSlot(5, new GuiElementBuilder(Items.CLOCK)
                            .setName(Text.translatable("setting.manhunt.runners_color").formatted(ManhuntConfig.config.getRunnersColor()))
                            .setLore(loreList)
                            .setCallback((index, type, action) -> {
                                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                    if (slowDownManager.get(player.getUuid()) < 6) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setRunnersColor(ManhuntConfig.config.getRunnersColorDefault());
                                            ManhuntConfig.config.save();
                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                            openManhuntSettingsGui(player);
                                        } else {
                                            SimpleGui runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                                            runnersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.white"))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.RESET);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.GRAY);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.DARK_GRAY);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.BLACK);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.RED);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.GOLD);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.YELLOW);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                                    .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                                                    .setCallback(teamColorGui::open)
                                            );

                                            runnersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.GREEN);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.DARK_GREEN);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.DARK_AQUA);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.BLUE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.DARK_BLUE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.DARK_PURPLE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
                                                        openTeamColorGui(player, clickType, boolvalue);
                                                    })
                                            );

                                            runnersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                                    .setCallback(() -> {
                                                        ManhuntConfig.config.setRunnersColor(Formatting.LIGHT_PURPLE);
                                                        ManhuntConfig.config.save();
                                                        player.getScoreboard().getTeam("runners").setColor(ManhuntConfig.config.getRunnersColor());
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
                            .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                            .setCallback(() -> openManhuntSettingsGui(player))
                    );

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

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;
        int intvalue;

        loreList = new ArrayList<>();
        name = "custom_titles";
        item = Items.OAK_SIGN;
        boolvalue = ManhuntConfig.config.isCustomTitles();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var gameTitlesBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setCustomTitles(ManhuntConfig.config.isCustomTitlesDefault());
                            } else {
                                ManhuntConfig.config.setCustomTitles(!gameTitlesBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "custom_sounds";
        item = Items.GOAT_HORN;
        boolvalue = ManhuntConfig.config.isCustomSounds();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var manhuntSoundsBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .hideDefaultTooltip()
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setCustomSounds(ManhuntConfig.config.isCustomSoundsDefault());
                            } else {
                                ManhuntConfig.config.setCustomSounds(!manhuntSoundsBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "custom_particles";
        item = Items.BLAZE_POWDER;
        boolvalue = ManhuntConfig.config.isCustomParticles();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var customParticlesBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setCustomParticles(ManhuntConfig.config.isCustomParticlesDefault());
                            } else {
                                ManhuntConfig.config.setCustomParticles(!customParticlesBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "automatic_compass";
        item = Items.COMPASS;
        boolvalue = ManhuntConfig.config.isAutomaticCompass();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var automaticCompassBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setAutomaticCompass(ManhuntConfig.config.isAutomaticCompassDefault());
                            } else {
                                ManhuntConfig.config.setAutomaticCompass(!automaticCompassBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "night_vision";
        item = Items.GLOWSTONE;
        boolvalue = ManhuntConfig.config.isNightVision();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var nightVisionBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setNightVision(ManhuntConfig.config.isNightVisionDefault());
                            } else {
                                ManhuntConfig.config.setNightVision(!nightVisionBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendly_fire";
        item = Items.EMERALD;
        intvalue = ManhuntConfig.config.getFriendlyFire();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (intvalue == 1) {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always").formatted(Formatting.GREEN), Text.translatable("lore.manhunt." + name + ".per_player"), Text.translatable("lore.manhunt." + name + ".never")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else if (intvalue == 2) {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always"), Text.translatable("lore.manhunt." + name + ".per_player").formatted(Formatting.YELLOW), Text.translatable("lore.manhunt." + name + ".never")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("lore.manhunt." + name + ".always"), Text.translatable("lore.manhunt." + name + ".per_player"), Text.translatable("lore.manhunt." + name + ".never").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        int friendlyFireInt = intvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setFriendlyFire(ManhuntConfig.config.getFriendlyFireDefault());
                            } else {
                                if (friendlyFireInt == 1) {
                                    ManhuntConfig.config.setFriendlyFire(2);
                                } else if (friendlyFireInt == 2) {
                                    ManhuntConfig.config.setFriendlyFire(3);
                                } else {
                                    ManhuntConfig.config.setFriendlyFire(1);
                                }
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runner_preferences";
        item = Items.WRITABLE_BOOK;
        boolvalue = ManhuntConfig.config.isRunnerPreferences();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var runnerPreferencesBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("category.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setRunnerPreferences(ManhuntConfig.config.isRunnerPreferencesDefault());
                            } else {
                                ManhuntConfig.config.setRunnerPreferences(!runnerPreferencesBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "bed_explosions";
        item = Items.RED_BED;
        boolvalue = ManhuntConfig.config.isBedExplosions();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var bedExplosionsBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setBedExplosions(ManhuntConfig.config.isBedExplosionsDefault());
                            } else {
                                ManhuntConfig.config.setBedExplosions(!bedExplosionsBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );
        slot++;

        globalPreferencesGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                .setCallback(() -> {
                    openSettingsGui(player);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lava_pvp_in_nether";
        item = Items.LAVA_BUCKET;
        boolvalue = ManhuntConfig.config.isLavaPvpInNether();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".second").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt." + name + ".third").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        var lavaPvpInNetherBool = boolvalue;
        globalPreferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("setting.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setLavaPvpInNether(ManhuntConfig.config.isLavaPvpInNetherDefault());
                            } else {
                                ManhuntConfig.config.setLavaPvpInNether(!lavaPvpInNetherBool);
                            }
                            ManhuntConfig.config.save();
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openGlobalPreferencesGui(player);
                        } else {
                            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                        }
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                    }
                })
        );

        globalPreferencesGui.setSlot(17, new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));

        globalPreferencesGui.open();
    }

    public static void openTitleTextsGui(ServerPlayerEntity player) {
        SimpleGui titleTextsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        titleTextsGui.setTitle(Text.translatable("category.manhunt.title_texts"));

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item = Items.PAPER;

        loreList = new ArrayList<>();
        name = "start_title";

        loreList.add(Text.literal("\"" + ManhuntConfig.config.getStartTitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("title_text.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setStartTitle(ManhuntConfig.config.getStartTitleDefault());
                            } else {
                                AnvilInputGui gameStartTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    ManhuntConfig.config.setStartTitle(input);
                                                    ManhuntConfig.config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
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
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "start_subtitle";

        loreList.add(Text.literal("\"" + ManhuntConfig.config.getStartSubtitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("title_text.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setStartSubtitle(ManhuntConfig.config.getStartSubtitleDefault());
                            } else {
                                AnvilInputGui gameStartSubtitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    ManhuntConfig.config.setStartSubtitle(input);
                                                    ManhuntConfig.config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
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
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "paused_title";

        loreList.add(Text.literal("\"" + ManhuntConfig.config.getPausedTitle() + "\"").styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

        titleTextsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("title_text.manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> {
                    if (slowDownManager.get(player.getUuid()) < 12)
                        slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                    if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                        if (slowDownManager.get(player.getUuid()) < 6) {
                            if (type == ClickType.DROP) {
                                ManhuntConfig.config.setPausedTitle(ManhuntConfig.config.getPausedTitleDefault());
                                ManhuntConfig.config.save();
                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                openTitleTextsGui(player);
                            } else {
                                AnvilInputGui gamePausedTitleGui = new AnvilInputGui(player, false) {
                                    @Override
                                    public void onInput(String input) {
                                        this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                                .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                                .setCallback(() -> {
                                                    ManhuntConfig.config.setPausedTitle(input);
                                                    ManhuntConfig.config.save();
                                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                    openTitleTextsGui(player);
                                                })
                                        );
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
                })
        );

        titleTextsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back")
                        .formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openSettingsGui(player);
                })
        );

        titleTextsGui.open();
    }

    public static void openModIntegrationsGui(ServerPlayerEntity player) {
        SimpleGui modIntegrationsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        modIntegrationsGui.setTitle(Text.translatable("category.manhunt.mod_integrations"));

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean boolvalue;

        loreList = new ArrayList<>();
        name = "vanilla";
        item = Items.GRASS_BLOCK;
        boolvalue = ManhuntConfig.config.isVanilla();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var vanillaBool = boolvalue;
        modIntegrationsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("integration.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> openVanillaIntegrationGui(player, type, vanillaBool))
        );
        slot++;

        loreList = new ArrayList<>();
        name = "chunky";
        item = Items.NETHER_STAR;
        boolvalue = ManhuntConfig.config.isChunky();

        loreList.add(Text.translatable("lore.manhunt." + name).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        if (boolvalue) {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
        loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

        var chunkyBool = boolvalue;
        modIntegrationsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("integration.manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback((index, type, action) -> openChunkyIntegrationGui(player, type, chunkyBool))
        );

        modIntegrationsGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                .setName(Text.translatable("text.manhunt.go_back")
                        .formatted(Formatting.WHITE))
                .setCallback(() -> {
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openSettingsGui(player);
                })
        );

        modIntegrationsGui.open();
    }

    private static void openVanillaIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (slowDownManager.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.config.setVanilla(ManhuntConfig.config.isVanillaDefault());
                    } else {
                        ManhuntConfig.config.setVanilla(!bool);
                    }
                    ManhuntConfig.config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openModIntegrationsGui(player);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var vanillaIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                vanillaIntegrationGui.setTitle(Text.translatable("integration.manhunt.vanilla"));

                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

                List<Text> loreList;
                String name;
                int slot = 0;
                Item item;
                boolean boolvalue;
                int intvalue;

                loreList = new ArrayList<>();
                name = "options.difficulty";
                item = Items.CREEPER_HEAD;
                Difficulty difficulty = ManhuntConfig.config.getDifficulty();

                if (difficulty == Difficulty.EASY) {
                    loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (difficulty == Difficulty.NORMAL) {
                    loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW), Text.translatable("options.difficulty.hard")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    loreList.add(Text.translatable("lore.manhunt.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable(name).formatted(Formatting.WHITE))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.config.setDifficulty(ManhuntConfig.config.getDifficultyDefault());
                                    } else {
                                        if (difficulty == Difficulty.EASY) {
                                            ManhuntConfig.config.setDifficulty(Difficulty.NORMAL);
                                        } else if (difficulty == Difficulty.NORMAL) {
                                            ManhuntConfig.config.setDifficulty(Difficulty.HARD);
                                        } else {
                                            ManhuntConfig.config.setDifficulty(Difficulty.EASY);
                                        }
                                    }
                                    ManhuntConfig.config.save();
                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                    openVanillaIntegrationGui(player, clickType, bool);
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        })
                );
                slot++;

                loreList = new ArrayList<>();
                name = "world_border";
                item = Items.PRISMARINE_WALL;
                intvalue = ManhuntConfig.config.getWorldBorder();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 5632 && intvalue != 11776 && intvalue != 59999968) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 5632) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"), Text.literal("Maximum")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 11776) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1st ring"), Text.literal("2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("1st ring"), Text.literal("2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                int worldBorderInt = intvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("setting.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (slowDownManager.get(player.getUuid()) < 12)
                                slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setWorldBorder(ManhuntConfig.config.getWorldBorderDefault());
                                        } else {
                                            if (worldBorderInt != 5632 && worldBorderInt != 11776) {
                                                ManhuntConfig.config.setWorldBorder(5632);
                                            } else {
                                                if (worldBorderInt == 5632) {
                                                    ManhuntConfig.config.setWorldBorder(11776);
                                                } else {
                                                    ManhuntConfig.config.setWorldBorder(59999968);
                                                }
                                            }
                                        }
                                        ManhuntConfig.config.save();
                                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                        openVanillaIntegrationGui(player, clickType, bool);
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
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.config.setWorldBorder(value);
                                                            ManhuntConfig.config.save();
                                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                            openVanillaIntegrationGui(player, clickType, bool);
                                                        })
                                                );
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
                        })
                );
                slot++;

                loreList = new ArrayList<>();
                name = "spawn_radius";
                item = Items.BEDROCK;
                intvalue = ManhuntConfig.config.getSpawnRadius();

                if (intvalue != 0 && intvalue != 5 && intvalue != 10) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 0) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("0").formatted(Formatting.RED), Text.literal("5"), Text.literal("10")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 5) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("0"), Text.literal("5").formatted(Formatting.YELLOW), Text.literal("10")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("0"), Text.literal("5"), Text.literal("10").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                int spawnRadiusInt = intvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("setting.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setSpawnRadius(ManhuntConfig.config.getSpawnRadiusDefault());
                                        } else {
                                            if (spawnRadiusInt != 0 && spawnRadiusInt != 5) {
                                                ManhuntConfig.config.setSpawnRadius(0);
                                            } else {
                                                if (spawnRadiusInt == 0) {
                                                    ManhuntConfig.config.setSpawnRadius(5);
                                                } else {
                                                    ManhuntConfig.config.setSpawnRadius(10);
                                                }
                                            }
                                        }
                                        ManhuntConfig.config.save();
                                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                        openVanillaIntegrationGui(player, clickType, bool);
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
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                            }

                                                            ManhuntConfig.config.setSpawnRadius(value);
                                                            ManhuntConfig.config.save();
                                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                            openVanillaIntegrationGui(player, clickType, bool);
                                                        })
                                                );
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
                        })
                );
                slot++;

                loreList = new ArrayList<>();
                name = "spectators_generate_chunks";
                item = Items.SNOW_BLOCK;
                boolvalue = ManhuntConfig.config.isSpectatorsGenerateChunks();

                if (boolvalue) {
                    loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on").formatted(Formatting.GREEN), Text.translatable("lore.manhunt.off")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    loreList.add(Text.translatable("lore.manhunt.double", Text.translatable("lore.manhunt.on"), Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));

                var spectatorsGenerateChunksBool = boolvalue;
                vanillaIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("setting.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (slowDownManager.get(player.getUuid()) < 12)
                                slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (type == ClickType.DROP) {
                                        ManhuntConfig.config.setSpectatorsGenerateChunks(ManhuntConfig.config.isSpectatorsGenerateChunksDefault());
                                    } else {
                                        ManhuntConfig.config.setSpectatorsGenerateChunks(!spectatorsGenerateChunksBool);
                                    }
                                    ManhuntConfig.config.save();
                                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                    openVanillaIntegrationGui(player, clickType, bool);
                                } else {
                                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                                }
                            } else {
                                player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                            }
                        })
                );

                vanillaIntegrationGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                        .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                        .setCallback(() -> openModIntegrationsGui(player))
                );

                vanillaIntegrationGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }

    private static void openChunkyIntegrationGui(ServerPlayerEntity player, ClickType clickType, Boolean bool) {
        if (slowDownManager.get(player.getUuid()) < 6) {
            if (!clickType.shift) {
                if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                    if (clickType == ClickType.DROP) {
                        ManhuntConfig.config.setChunky(ManhuntConfig.config.isChunkyDefault());
                    } else {
                        ManhuntConfig.config.setChunky(!bool);
                    }
                    ManhuntConfig.config.save();
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openModIntegrationsGui(player);

                    if (!bool) {
                        ManhuntMod.schedulePreload(player.getServer());
                    } else {
                        if (ManhuntGame.chunkyLoaded) {
                            ChunkyAPI chunky = ChunkyProvider.get().getApi();

                            chunky.cancelTask("manhunt:overworld");
                            chunky.cancelTask("manhunt:the_nether");
                            chunky.cancelTask("manhunt:the_end");
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.no_permission").formatted(Formatting.RED));
                }
            } else {
                var chunkyIntegrationGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                chunkyIntegrationGui.setTitle(Text.translatable("integration.manhunt.chunky"));

                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));

                List<Text> loreList;
                String name;
                int slot = 0;
                Item item;
                int intvalue;

                loreList = new ArrayList<>();
                name = "flat_world_preset.minecraft.overworld";
                item = Items.GRASS_BLOCK;
                intvalue = ManhuntConfig.config.getOverworld();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 2000 && intvalue != 4000 && intvalue != 8000) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 2000) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("2000").formatted(Formatting.RED), Text.literal("4000"), Text.literal("8000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 4000) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("2000"), Text.literal("4000").formatted(Formatting.YELLOW), Text.literal("8000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("2000"), Text.literal("4000"), Text.literal("8000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var overworldInt = intvalue;
                chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable(name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setOverworld(ManhuntConfig.config.getOverworldDefault());
                                        } else {
                                            if (overworldInt != 2000 && overworldInt != 4000) {
                                                ManhuntConfig.config.setOverworld(2000);
                                            } else {
                                                if (overworldInt == 2000) {
                                                    ManhuntConfig.config.setOverworld(4000);
                                                } else {
                                                    ManhuntConfig.config.setOverworld(8000);
                                                }
                                            }
                                        }
                                        ManhuntConfig.config.save();
                                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                        openChunkyIntegrationGui(player, clickType, bool);
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
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.config.setOverworld(value);
                                                            ManhuntConfig.config.save();
                                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                            openChunkyIntegrationGui(player, clickType, bool);

                                                            if (value == 0) {
                                                                ManhuntMod.schedulePreload(player.getServer());
                                                            } else {
                                                                if (ManhuntGame.chunkyLoaded) {
                                                                    ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                    chunky.cancelTask("manhunt:overworld");
                                                                }
                                                            }
                                                        })
                                                );
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
                        })
                );
                slot++;

                loreList = new ArrayList<>();
                name = "the_nether";
                item = Items.NETHERRACK;
                intvalue = ManhuntConfig.config.getTheNether();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 250) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 500) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"), Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"), Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var netherInt = intvalue;
                chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("world.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setTheNether(ManhuntConfig.config.getTheNetherDefault());
                                        } else {
                                            if (netherInt != 250 && netherInt != 500) {
                                                ManhuntConfig.config.setTheNether(250);
                                            } else {
                                                if (netherInt == 250) {
                                                    ManhuntConfig.config.setTheNether(500);
                                                } else {
                                                    ManhuntConfig.config.setTheNether(1000);
                                                }
                                            }
                                        }
                                        ManhuntConfig.config.save();
                                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                        openChunkyIntegrationGui(player, clickType, bool);
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
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.config.setTheNether(value);
                                                            ManhuntConfig.config.save();
                                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                            openChunkyIntegrationGui(player, clickType, bool);

                                                            if (value == 0) {
                                                                ManhuntMod.schedulePreload(player.getServer());
                                                            } else {
                                                                if (ManhuntGame.chunkyLoaded) {
                                                                    ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                    chunky.cancelTask("manhunt:the_nether");
                                                                }
                                                            }
                                                        })
                                                );
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
                        })
                );
                slot++;

                loreList = new ArrayList<>();
                name = "the_end";
                item = Items.END_STONE;
                intvalue = ManhuntConfig.config.getTheEnd();

                if (intvalue == 0) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.translatable("lore.manhunt.off").formatted(Formatting.RED)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else if (intvalue != 250 && intvalue != 500 && intvalue != 1000) {
                    loreList.add(Text.translatable("lore.manhunt.single", Text.literal(String.valueOf(intvalue)).formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                } else {
                    if (intvalue == 250) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250").formatted(Formatting.RED), Text.literal("500"), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else if (intvalue == 500) {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"), Text.literal("500").formatted(Formatting.YELLOW), Text.literal("1000")).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    } else {
                        loreList.add(Text.translatable("lore.manhunt.triple", Text.literal("250"), Text.literal("500"), Text.literal("1000").formatted(Formatting.GREEN)).styled(style -> style.withColor(Formatting.GRAY).withItalic(false)));
                    }
                }
                loreList.add(Text.translatable("lore.manhunt.click_drop").styled(style -> style.withColor(Formatting.AQUA).withItalic(false)));
                loreList.add(Text.translatable("lore.manhunt.click_shift").styled(style -> style.withColor(Formatting.LIGHT_PURPLE).withItalic(false)));

                var endInt = intvalue;
                chunkyIntegrationGui.setSlot(slot, new GuiElementBuilder(item)
                        .setName(Text.translatable("world.manhunt." + name))
                        .setLore(loreList)
                        .setCallback((index, type, action) -> {
                            if (ManhuntMod.checkLeaderPermission(player, "manhunt.settings")) {
                                if (slowDownManager.get(player.getUuid()) < 6) {
                                    if (!type.shift) {
                                        if (type == ClickType.DROP) {
                                            ManhuntConfig.config.setTheEnd(ManhuntConfig.config.getTheEndDefault());
                                        } else {
                                            if (endInt != 250 && endInt != 500) {
                                                ManhuntConfig.config.setOverworld(250);
                                            } else {
                                                if (endInt == 250) {
                                                    ManhuntConfig.config.setTheEnd(500);
                                                } else {
                                                    ManhuntConfig.config.setTheEnd(1000);
                                                }
                                            }
                                        }
                                        ManhuntConfig.config.save();
                                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                        openChunkyIntegrationGui(player, clickType, bool);
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
                                                                player.sendMessage(Text.translatable("chat.manhunt.invalid_input").formatted(Formatting.RED));
                                                            }
                                                            ManhuntConfig.config.setTheEnd(value);
                                                            ManhuntConfig.config.save();
                                                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                            openChunkyIntegrationGui(player, clickType, bool);

                                                            if (value == 0) {
                                                                ManhuntMod.schedulePreload(player.getServer());
                                                            } else {
                                                                if (ManhuntGame.chunkyLoaded) {
                                                                    ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                                                    chunky.cancelTask("manhunt:the_end");
                                                                }
                                                            }
                                                        })
                                                );
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
                        })
                );

                chunkyIntegrationGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                        .setName(Text.translatable("text.manhunt.go_back").formatted(Formatting.WHITE))
                        .setCallback(() -> openModIntegrationsGui(player))
                );

                chunkyIntegrationGui.open();
            }
        } else {
            player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
        }
    }
}
