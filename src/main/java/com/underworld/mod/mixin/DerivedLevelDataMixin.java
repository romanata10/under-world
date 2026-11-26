package com.underworld.mod.mixin;

import com.underworld.mod.world.FunWorldTimeManager;
import net.minecraft.world.level.storage.DerivedLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DerivedLevelData.class)
public class DerivedLevelDataMixin {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void underworld$getFunWorldDayTime(CallbackInfoReturnable<Long> cir) {
        if (FunWorldTimeManager.isFunWorldData(this)) {
            cir.setReturnValue(FunWorldTimeManager.getServerDayTime());
        }
    }

    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void underworld$setFunWorldDayTime(long time, CallbackInfo ci) {
        if (FunWorldTimeManager.isFunWorldData(this)) {
            FunWorldTimeManager.setServerDayTime(time);
            ci.cancel();
        }
    }
}
