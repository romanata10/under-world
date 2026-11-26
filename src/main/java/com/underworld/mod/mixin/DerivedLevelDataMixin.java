package com.underworld.mod.mixin;

import com.underworld.mod.world.FunWorldTimeManager;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DerivedLevelData.class)
public class DerivedLevelDataMixin implements FunWorldLevelDataDuck {
    @Unique
    private boolean underworld$funWorld = false;

    @Override
    public boolean underworld$isFunWorld() {
        return this.underworld$funWorld;
    }

    @Override
    public void underworld$setFunWorld(boolean funWorld) {
        this.underworld$funWorld = funWorld;
    }

    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void underworld$getFunWorldDayTime(CallbackInfoReturnable<Long> cir) {
        if (this.underworld$funWorld) {
            cir.setReturnValue(FunWorldTimeManager.getServerDayTime());
        }
    }

    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void underworld$setFunWorldDayTime(long time, CallbackInfo ci) {
        if (this.underworld$funWorld) {
            FunWorldTimeManager.setServerDayTime(time);
            ci.cancel();
        }
    }
}
