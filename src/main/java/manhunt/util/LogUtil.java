package manhunt.util;

import lombok.experimental.UtilityClass;
import manhunt.Manhunt;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

// Thanks to https://github.com/sakurawald/fuji-fabric.

@UtilityClass
public class LogUtil {
    public static Logger createLogger(String name) {
        Logger logger = LogManager.getLogger(name);
        try {
            String level = System.getProperty("%s.level".formatted(Manhunt.MOD_ID));
            Configurator.setLevel(logger, Level.getLevel(level));
        } catch (Exception e) {
            return logger;
        }
        return logger;
    }
}