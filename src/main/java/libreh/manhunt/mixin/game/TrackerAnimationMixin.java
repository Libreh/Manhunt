package libreh.manhunt.mixin.game;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class TrackerAnimationMixin {
    @Shadow
    public abstract ComponentMap getComponents();

    @Inject(method = "setBobbingAnimationTime", at = @At("HEAD"), cancellable = true)
    private void excludeTracker(int bobbingAnimationTime, CallbackInfo ci) {
        var customData = this.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && customData.copyNbt().getBoolean("Tracker")) {
            ci.cancel();
        }
    }
}
