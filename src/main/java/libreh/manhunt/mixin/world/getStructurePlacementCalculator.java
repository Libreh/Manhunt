package libreh.manhunt.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import libreh.manhunt.ManhuntMod;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public class getStructurePlacementCalculator {
    @ModifyExpressionValue(method = "locateStructure(Lnet/minecraft/server/world/ServerWorld;" + "Lnet/minecraft" +
            "/registry/entry/RegistryEntryList;Lnet/minecraft/util/math/BlockPos;IZ)" + "Lcom/mojang/datafixers/util" +
            "/Pair;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world" + "/ServerChunkManager;" +
            "getStructurePlacementCalculator()" + "Lnet/minecraft/world/gen/chunk/placement" +
            "/StructurePlacementCalculator;"))
    private StructurePlacementCalculator replaceCalculator(StructurePlacementCalculator original) {
        return getCalculator(original);
    }

    @Unique
    private StructurePlacementCalculator getCalculator(StructurePlacementCalculator original) {
        if (ManhuntMod.structurePlacementCalculator != null) {
            return ManhuntMod.structurePlacementCalculator;
        } else {
            return original;
        }
    }
}
