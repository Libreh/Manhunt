package manhunt.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DeleteWorld {

    public static void invoke() {
        var gameDir = FabricLoader.getInstance().getGameDir();

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/data"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/DIM1"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/DIM-1"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/dimensions"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/entities"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/level.dat"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/level.dat_old"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/poi"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/region"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> dirStream = Files.walk(gameDir.resolve("world/stats"))) {
            dirStream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {
        }
    }
}
