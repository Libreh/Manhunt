package manhunt.config.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import lombok.Cleanup;
import manhunt.Manhunt;

import java.io.*;

// Thanks to https://github.com/sakurawald/fuji-fabric.

public class ResourceConfigHandler extends ConfigHandler<JsonElement> {

    final String resourcePath;

    public ResourceConfigHandler(File file, String resourcePath) {
        super(file);
        this.file = file;
        this.resourcePath = resourcePath;
    }

    public ResourceConfigHandler(String resourcePath) {
        this(Manhunt.CONFIG_PATH.resolve(resourcePath).toFile(), resourcePath);
    }

    public void loadFromDisk() {
        // Does the file exist?
        try {
            if (!file.exists()) {
                saveToDisk();
            } else {
                // read older json from disk
                @Cleanup Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
                JsonElement olderJsonElement = JsonParser.parseReader(reader);

                // merge older json with newer json
                if (!this.merged) {
                    this.merged = true;
                    JsonElement newerJsonElement = ResourceConfigHandler.getJsonElement(this.resourcePath);
                    mergeJson(olderJsonElement, newerJsonElement);
                }

                // read merged json
                model = olderJsonElement;
                this.saveToDisk();
            }

        } catch (IOException e) {
            Manhunt.LOGGER.error("Load config failed: " + e.getMessage());
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveToDisk() {
        try {
            // Should we generate a default config instance ?
            if (!file.exists()) {
                this.file.getParentFile().mkdirs();
                this.model = ResourceConfigHandler.getJsonElement(this.resourcePath);
            }

            // Save.
            JsonWriter jsonWriter = gson.newJsonWriter(new BufferedWriter(new FileWriter(this.file)));
            gson.toJson(this.model, jsonWriter);
            jsonWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
