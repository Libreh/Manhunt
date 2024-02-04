package manhunt.config.handler;

import assets.manhunt.Placeholder;
import com.google.gson.*;
import lombok.Cleanup;
import lombok.Getter;
import manhunt.Manhunt;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

// Thanks to https://github.com/sakurawald/fuji-fabric.

public abstract class ConfigHandler<T> {

    @Getter
    protected static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    protected File file;
    protected T model;

    protected boolean merged = false;

    public ConfigHandler(File file) {
        this.file = file;
    }

    public static JsonElement getJsonElement(String resourcePath) {
        try {
            InputStream inputStream = Placeholder.class.getResourceAsStream(resourcePath);
            assert inputStream != null;
            @Cleanup Reader reader = new BufferedReader(new InputStreamReader(inputStream));
            return JsonParser.parseReader(reader);
        } catch (Exception e) {
            Manhunt.LOGGER.error(e.getMessage());
        }

        return null;
    }

    public abstract void loadFromDisk();

    public abstract void saveToDisk();


    public T model() {
        return this.model;
    }

    public JsonElement toJsonElement() {
        return gson.toJsonTree(this.model);
    }


    @SuppressWarnings("unused")
    public void backupFromDisk() {
        if (!file.exists()) return;
        String originalFileName = file.getName();
        String backupFileName = originalFileName + ".bak";
        String backupFilePath = file.getParent() + File.separator + backupFileName;
        File backupFile = new File(backupFilePath);
        try {
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Manhunt.LOGGER.error("Backup file failed: " + e.getMessage());
        }
    }

    public void mergeJson(JsonElement oldJson, JsonElement newJson) {
        if (!oldJson.isJsonObject() || !newJson.isJsonObject()) {
            throw new IllegalArgumentException("Both elements must be JSON objects.");
        }
        mergeFields(oldJson.getAsJsonObject(), newJson.getAsJsonObject());
    }

    private void mergeFields(JsonObject oldJson, JsonObject newJson) {
        Set<Map.Entry<String, JsonElement>> entrySet = newJson.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (oldJson.has(key) && oldJson.get(key).isJsonObject() && value.isJsonObject()) {
                mergeFields(oldJson.getAsJsonObject(key), value.getAsJsonObject());
            } else {
                // note: for JsonArray, we will not directly set array elements, but we will add new properties for every array element (language default empty-value). e.g. For List<ExamplePojo>, we will never change the size of this list, but we will add missing properties for every ExamplePojo with the language default empty-value.
                if (!oldJson.has(key)) {
                    oldJson.add(key, value);
                    Manhunt.LOGGER.warn("Add missing json property: file = {}, key = {}, value = {}", this.file.getName(), key, value);
                }
            }
        }
    }
}
