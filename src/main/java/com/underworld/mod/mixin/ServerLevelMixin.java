package com.underworld.mod.mixin;

import com.underworld.mod.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Shadow
    @Final
    private ServerLevelData serverLevelData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void underworld$markFunWorldLevelData(CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (ModDimensions.isFunWorld(self) && this.serverLevelData instanceof FunWorldLevelDataDuck duck) {
            duck.underworld$setFunWorld(true);
        }
    }
}
