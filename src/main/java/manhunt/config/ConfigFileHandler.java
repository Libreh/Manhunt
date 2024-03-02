package manhunt.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static manhunt.ManhuntMod.MOD_ID;

// Thanks to https://github.com/malte0811/FerriteCore

public class ConfigFileHandler {
    public static final Path configDir = FabricLoader.getInstance().getConfigDir();
    public static final Path config = configDir.resolve(MOD_ID + ".properties");

    public static void finish(List<ManhuntConfig.Setting> settings) throws IOException {
        if (!Files.exists(config)) {
            try {
                Files.createDirectories(configDir);
            } catch (FileAlreadyExistsException x) {
                if (!Files.isDirectory(configDir)) {
                    throw new IOException("Config dir exists, but is not a directory?", x);
                }
            }
            Files.createFile(config);
        }
        Properties propsInFile = new Properties();
        propsInFile.load(Files.newInputStream(config));
        HashMap<String, Object> existingSettings = new HashMap<>();
        for (String key : propsInFile.stringPropertyNames()) {
            existingSettings.put(key, propsInFile.get(key));
        }
        List<String> newLines = new ArrayList<>();
        for (ManhuntConfig.Setting setting : settings) {
            final Object value = existingSettings.getOrDefault(setting.getName(), setting.getDefaultValue());
            newLines.add("# " + setting.getComment());
            newLines.add(setting.getName() + " = " + value);
        }
        Files.write(config, newLines);
    }
}