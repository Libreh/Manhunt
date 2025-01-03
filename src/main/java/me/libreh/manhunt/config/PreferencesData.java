package me.libreh.manhunt.config;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.JsonDataStorage;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PreferencesData {
    public static final PlayerDataStorage<PreferencesData> STORAGE = new JsonDataStorage<>("preferences", PreferencesData.class);
    public boolean customSounds = true;
    public boolean customTitles = true;
    public boolean friendlyFire = true;
    public boolean bedExplosionsPvP = true;
    public boolean netherLavaPvP = true;
    public boolean announceSeed = true;
    public boolean announceDuration = true;

    public static PreferencesData get(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return new PreferencesData();
        }

        var data = PlayerDataApi.getCustomDataFor(serverPlayer, STORAGE);
        if (data == null) {
            data = new PreferencesData();
            PlayerDataApi.setCustomDataFor(serverPlayer, STORAGE, data);
        }

        return data;
    }
}
