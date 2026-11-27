package com.underworld.mod.mixin;

import com.underworld.mod.client.dimension.DimensionMusicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void underworld$overrideDimensionMusic(CallbackInfoReturnable<Music> cir) {
        if (this.player == null || this.level == null) {
            return;
        }
        Music custom = DimensionMusicHandler.resolveCustomMusic(this.level);
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }
}
