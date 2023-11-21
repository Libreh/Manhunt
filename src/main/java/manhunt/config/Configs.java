package manhunt.config;

import manhunt.config.handler.ConfigHandler;
import manhunt.config.handler.ObjectConfigHandler;
import manhunt.config.model.ConfigModel;

public class Configs {

    public static final ConfigHandler<ConfigModel> configHandler = new ObjectConfigHandler<>("config.json", ConfigModel.class);
}
