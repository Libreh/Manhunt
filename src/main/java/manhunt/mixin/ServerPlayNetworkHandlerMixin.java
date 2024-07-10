package manhunt.mixin;

import manhunt.game.GameState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static manhunt.ManhuntMod.state;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void Manhunt$handleGuiClicks(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (state == GameState.PREGAME && packet.getStack().get(DataComponentTypes.CUSTOM_DATA) != null) {
            ci.cancel();
        }
    }
}