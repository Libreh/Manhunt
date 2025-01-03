package me.libreh.manhunt.mixin.world;

import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelProperties.class)
public interface LevelPropertiesAccessor {
    @Accessor("generatorOptions")
    GeneratorOptions getGeneratorOptions();

    @Mutable @Accessor("generatorOptions")
    void setGeneratorOptions(GeneratorOptions generatorOptions);
}
