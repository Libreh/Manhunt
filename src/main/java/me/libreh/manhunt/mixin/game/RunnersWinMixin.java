package me.libreh.manhunt.mixin.game;

import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.libreh.manhunt.utils.Methods.runnersWin;

@Mixin(PhaseManager.class)
public class RunnersWinMixin {
    @Inject(method = "setPhase", at = @At("TAIL"))
    private void runnersWon(PhaseType<?> type, CallbackInfo ci) {
        if (type == PhaseType.DYING) {
            runnersWin();
        }
    }
}