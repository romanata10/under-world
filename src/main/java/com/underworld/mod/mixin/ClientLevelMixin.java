package com.underworld.mod.mixin;

import com.underworld.mod.client.ClientFunWorldTimeHandler;
import com.underworld.mod.registry.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void underworld$tickFunWorldTime(CallbackInfo ci) {
        ClientLevel self = (ClientLevel) (Object) this;
        if (!ModDimensions.isFunWorld(self)) {
            return;
        }
        self.setGameTime(self.getGameTime() + 1L);
        if (ClientFunWorldTimeHandler.hasPendingApply()) {
            ClientFunWorldTimeHandler.applyInto(self);
        } else if (self.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            int mult = Math.max(1, ClientFunWorldTimeHandler.getSpeedMultiplier());
            long next = self.getDayTime() + mult;
            ClientFunWorldTimeHandler.runApplying(() -> self.setDayTime(next));
        }
        ci.cancel();
    }

    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void underworld$guardFunWorldSetDayTime(long time, CallbackInfo ci) {
        ClientLevel self = (ClientLevel) (Object) this;
        if (ModDimensions.isFunWorld(self) && !ClientFunWorldTimeHandler.isApplying()) {
            ci.cancel();
        }
    }
}
