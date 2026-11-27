package com.underworld.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.entity.TheMimicEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.UUID;

public class TheMimicRenderer extends GeoEntityRenderer<TheMimicEntity> {
    private final PlayerModel<TheMimicEntity> widePlayerModel;
    private final PlayerModel<TheMimicEntity> slimPlayerModel;

    public TheMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<TheMimicEntity>(UnderWorldMod.id("the_mimic"), true)
                .withAltTexture(UnderWorldMod.id("the_mimic/the_mimic")));
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.widePlayerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slimPlayerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.shadowRadius = 0.65F;
    }

    @Override
    public void render(TheMimicEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isTransformed()) {
            this.shadowRadius = 0.75F;
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            return;
        }

        this.shadowRadius = 0.5F;
        SkinDescriptor skin = resolveDisguiseSkin(entity);
        PlayerModel<TheMimicEntity> model = skin.slim() ? this.slimPlayerModel : this.widePlayerModel;

        model.young = false;
        model.crouching = false;
        model.attackTime = 0.0F;
        model.setAllVisible(true);

        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headRot - bodyRot;
        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float ageInTicks = entity.tickCount + partialTick;
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = Math.min(1.0F, entity.walkAnimation.speed(partialTick));

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Unnatural creepy glitch twitching of the disguised player's head
        int step = (int) (ageInTicks * 0.85F);
        int hash = step * 1103515245 + entity.getId() * 12345;
        if ((hash & 7) < 3) {
            float twitchYaw = (((hash >> 3) & 31) - 15) * 0.045F;
            float twitchRoll = (((hash >> 8) & 31) - 15) * 0.036F;
            float twitchPitch = (((hash >> 13) & 15) - 7) * 0.032F;
            model.head.yRot += twitchYaw;
            model.head.zRot += twitchRoll;
            model.head.xRot += twitchPitch;
        } else {
            model.head.zRot += Mth.sin(ageInTicks * 0.35F) * 0.14F;
        }
        model.hat.copyFrom(model.head);

        VertexConsumer vc = bufferSource.getBuffer(model.renderType(skin.location()));
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        model.renderToBuffer(poseStack, vc, packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        if (this.shouldShowName(entity)) {
            this.renderNameTag(entity, entity.getDisplayName(), poseStack, bufferSource, packedLight);
        }
    }

    private SkinDescriptor resolveDisguiseSkin(TheMimicEntity entity) {
        UUID stolen = entity.getStolenPlayerUuid();
        if (stolen != null) {
            ClientPacketListener conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                PlayerInfo info = conn.getPlayerInfo(stolen);
                if (info != null) {
                    return new SkinDescriptor(info.getSkinLocation(), "slim".equals(info.getModelName()));
                }
            }
            return new SkinDescriptor(
                    DefaultPlayerSkin.getDefaultSkin(stolen),
                    "slim".equals(DefaultPlayerSkin.getSkinModelName(stolen))
            );
        }
        return new SkinDescriptor(DefaultPlayerSkin.getDefaultSkin(), false);
    }

    private record SkinDescriptor(ResourceLocation location, boolean slim) {}
}
