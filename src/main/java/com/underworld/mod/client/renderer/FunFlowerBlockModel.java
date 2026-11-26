package com.underworld.mod.client.renderer;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.block.FunFlowerBlock;
import com.underworld.mod.block.FunFlowerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

import java.util.EnumMap;
import java.util.Map;

public class FunFlowerBlockModel extends DefaultedBlockGeoModel<FunFlowerBlockEntity> {
    private static final ResourceLocation TALL_MODEL = UnderWorldMod.id("geo/block/fun_flower.geo.json");
    private static final ResourceLocation FLAT_MODEL = UnderWorldMod.id("geo/block/fun_flat_flower.geo.json");
    private static final ResourceLocation TALL_ANIM = UnderWorldMod.id("animations/block/fun_flower.animation.json");
    private static final ResourceLocation FLAT_ANIM = UnderWorldMod.id("animations/block/fun_flat_flower.animation.json");

    private static final Map<FunFlowerBlock.FlowerVariant, ResourceLocation> DAY_TEXTURES =
            new EnumMap<>(FunFlowerBlock.FlowerVariant.class);
    private static final Map<FunFlowerBlock.FlowerVariant, ResourceLocation> NIGHT_TEXTURES =
            new EnumMap<>(FunFlowerBlock.FlowerVariant.class);

    static {
        for (FunFlowerBlock.FlowerVariant variant : FunFlowerBlock.FlowerVariant.values()) {
            DAY_TEXTURES.put(variant, UnderWorldMod.id("textures/block/flower/" + variant.getAssetName() + ".png"));
            NIGHT_TEXTURES.put(variant, UnderWorldMod.id("textures/block/flower/" + variant.getAssetName() + "_night.png"));
        }
    }

    public FunFlowerBlockModel() {
        super(UnderWorldMod.id("fun_flower"));
    }

    @Override
    public ResourceLocation getModelResource(FunFlowerBlockEntity animatable) {
        return animatable.getVariant().isFlat() ? FLAT_MODEL : TALL_MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(FunFlowerBlockEntity animatable) {
        return animatable.getVariant().isFlat() ? FLAT_ANIM : TALL_ANIM;
    }

    @Override
    public ResourceLocation getTextureResource(FunFlowerBlockEntity animatable) {
        FunFlowerBlock.FlowerVariant variant = animatable.getVariant();
        return animatable.isNightActive() ? NIGHT_TEXTURES.get(variant) : DAY_TEXTURES.get(variant);
    }

    @Override
    public void setCustomAnimations(FunFlowerBlockEntity animatable, long instanceId,
                                    AnimationState<FunFlowerBlockEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head == null) {
            return;
        }

        if (!animatable.isNightActive()) {
            head.setRotX(0.0F);
            head.setRotY(0.0F);
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        BlockPos pos = animatable.getBlockPos();
        boolean flat = animatable.getVariant().isFlat();
        double flowerX = pos.getX() + 0.5D;
        double flowerY = pos.getY() + (flat ? 0.25D : 1.45D);
        double flowerZ = pos.getZ() + 0.5D;

        double dx = player.getX() - flowerX;
        double dy = player.getEyeY() - flowerY;
        double dz = player.getZ() - flowerZ;
        double horizDist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (-Mth.atan2(dx, -dz));
        float pitch = (float) Mth.clamp(Mth.atan2(dy, Math.max(0.1D, horizDist)), -0.6D, 0.85D);

        head.setRotY(yaw);
        head.setRotX(flat ? (0.45F + pitch * 0.45F) : pitch);
    }
}
