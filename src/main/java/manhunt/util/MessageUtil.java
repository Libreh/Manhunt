package manhunt.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import manhunt.Manhunt;
import manhunt.config.handler.ResourceConfigHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Thanks to https://github.com/sakurawald/fuji-fabric

public class MessageUtil {
    private static final FabricServerAudiences adventure = FabricServerAudiences.of(Manhunt.SERVER);
    private static final Map<String, String> player2lang = new HashMap<>();
    private static final Map<String, JsonObject> lang2json = new HashMap<>();
    private static final String DEFAULT_LANG = "en_us";
    private static final MiniMessage miniMessageParser = MiniMessage.builder().build();

    public static void copyLanguageFiles() {
        new ResourceConfigHandler("lang/en_us.json").loadFromDisk();
    }

    public static void loadLanguageIfAbsent(String lang) {
        if (lang2json.containsKey(lang)) return;

        InputStream is;
        try {
            is = FileUtils.openInputStream(Manhunt.CONFIG_PATH.resolve("lang").resolve(lang + ".json").toFile());
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
            lang2json.put(lang, jsonObject);
            Manhunt.LOGGER.info("Language {} loaded.", lang);
        } catch (IOException e) {
            Manhunt.LOGGER.debug("One of your player is using a language '{}' that is missing -> fallback to default language for this player", lang);
        }

        if (!lang2json.containsKey(DEFAULT_LANG)) loadLanguageIfAbsent(DEFAULT_LANG);
    }


    public static String ofString(Audience audience, String key, Object... args) {

        /* get player */
        ServerPlayerEntity player;
        if (audience instanceof ServerPlayerEntity) player = (ServerPlayerEntity) audience;
        else if (audience instanceof ServerCommandSource source && source.getPlayer() != null)
            player = source.getPlayer();
        else player = null;

        /* get lang */
        String lang;
        if (player != null) {
            lang = player2lang.getOrDefault(player.getGameProfile().getName(), DEFAULT_LANG);
        } else {
            lang = DEFAULT_LANG;
        }

        loadLanguageIfAbsent(lang);

        /* get json */
        JsonObject json;
        json = lang2json.get(!lang2json.containsKey(lang) ? DEFAULT_LANG : lang);
        if (!json.has(key)) {
            Manhunt.LOGGER.warn("Language {} miss key '{}' -> fallback to default language for this key", lang, key);
            json = lang2json.get(DEFAULT_LANG);
        }

        /* get value */
        String value;
        value = json.get(key).getAsString();
        return formatString(value, args);
    }

    public static String formatString(String string, Object... args) {
        if (args.length > 0) {
            return String.format(string, args);
        }
        return string;
    }

    public static Component ofComponent(Audience audience, String key, Object... args) {
        //note: if call ofString() directly with args, then we pass args to ofString(),
        // or else we pass args to ofComponent() to avoid args being formatted twice
        return ofComponent(ofString(audience, key), args);
    }

    public static Component ofComponent(String str, Object... args) {
        return miniMessageParser.deserialize(formatString(str, args));
    }

    public static Text ofVomponent(String str, Object... args) {
        return toVomponent(ofComponent(str, args));
    }

    public static Text ofVomponent(Audience audience, String key, Object... args) {
        return toVomponent(ofComponent(audience, key, args));
    }

    public static Text toVomponent(Component component) {
        return adventure.toNative(component);
    }

    public static List<Text> ofVomponents(Audience audience, String key, Object... args) {
        String lines = ofString(audience, key, args);

        List<Text> ret = new ArrayList<>();
        for (String line : lines.split("\n")) {
            ret.add(ofVomponent(line));
        }
        return ret;
    }

    public static void sendMessage(Audience audience, String key, Object... args) {
        audience.sendMessage(ofComponent(audience, key, args));
    }

    public static void sendActionBar(Audience audience, String key, Object... args) {
        audience.sendActionBar(ofComponent(audience, key, args));
    }

    public static void sendBroadcast(String key, Object... args) {
        // fix: log broadcast for console
        Manhunt.LOGGER.info(PlainTextComponentSerializer.plainText().serialize(ofComponent(null, key, args)));

        for (ServerPlayerEntity player : Manhunt.SERVER.getPlayerManager().getPlayerList()) {
            sendMessage(player, key, args);
        }
    }

    public static void showTitle(Audience audience, String title, String subtitle) {
        audience.showTitle(Title.title(ofComponent(audience, title), ofComponent(audience, subtitle)));
    }

}