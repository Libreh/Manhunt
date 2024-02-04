package manhunt.util;

import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RemoveFromRegistryUtil<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, Identifier key) {
        return ((RemoveFromRegistryUtil<T>) registry).remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, T value) {
        return ((RemoveFromRegistryUtil<T>) registry).remove(value);
    }

    boolean remove(T value);

    boolean remove(Identifier key);

    void setFrozen(boolean value);

    boolean isFrozen();
}
