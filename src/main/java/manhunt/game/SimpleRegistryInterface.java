package manhunt.game;

import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

// Thanks to https://github.com/sakurawald/fuji-fabric.

@ApiStatus.Internal
public interface SimpleRegistryInterface<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, Identifier key) {
        return ((SimpleRegistryInterface<T>) registry).manhunt$remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, T value) {
        return ((SimpleRegistryInterface<T>) registry).manhunt$remove(value);
    }

    boolean manhunt$remove(T value);

    boolean manhunt$remove(Identifier key);

    void manhunt$setFrozen(boolean value);

    boolean manhunt$isFrozen();
}