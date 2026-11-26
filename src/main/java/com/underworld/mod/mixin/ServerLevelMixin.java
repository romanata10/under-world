package com.underworld.mod.mixin;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.world.FunWorldTimeManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void underworld$markFunWorldLevelData(CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (ModDimensions.isFunWorld(self)) {
            FunWorldTimeManager.registerFunWorldData(self.getLevelData());
        }
    }
}
