package com.underworld.mod.mixin;

import com.mojang.authlib.GameProfile;
import com.underworld.mod.network.FriendTabHelper;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {

    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true)
    private void underWorld$getFriendSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        GameProfile profile = this.getProfile();
        if (profile != null && FriendTabHelper.FRIEND_UUID.equals(profile.getId())) {
            cir.setReturnValue(FriendTabHelper.FRIEND_TAB_SKIN);
        }
    }

    @Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
    private void underWorld$getFriendModel(CallbackInfoReturnable<String> cir) {
        GameProfile profile = this.getProfile();
        if (profile != null && FriendTabHelper.FRIEND_UUID.equals(profile.getId())) {
            cir.setReturnValue("default");
        }
    }

    @Inject(method = "isSkinLoaded", at = @At("HEAD"), cancellable = true)
    private void underWorld$isFriendSkinLoaded(CallbackInfoReturnable<Boolean> cir) {
        GameProfile profile = this.getProfile();
        if (profile != null && FriendTabHelper.FRIEND_UUID.equals(profile.getId())) {
            cir.setReturnValue(true);
        }
    }
}
