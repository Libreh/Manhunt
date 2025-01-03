package me.libreh.manhunt.mixin.game;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network" +
            "/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void handleGuiClicks(ClickSlotC2SPacket packet, CallbackInfo ci) {
        var stack = packet.getStack();
        var customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && customData.copyNbt().getBoolean("Remove") && !customData.copyNbt().getBoolean("Tracker")) {
            ci.cancel();
        }
    }
}