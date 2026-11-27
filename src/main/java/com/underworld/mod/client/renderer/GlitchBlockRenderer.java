package com.underworld.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.underworld.mod.block.GlitchBlockEntity;
import com.underworld.mod.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class GlitchBlockRenderer implements BlockEntityRenderer<GlitchBlockEntity> {
    private record SpriteEntry(TextureAtlasSprite sprite, BlockState state) {}

    private static List<SpriteEntry> cachedSprites = null;

    public GlitchBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void clearCache() {
        cachedSprites = null;
    }

    private static List<SpriteEntry> getAllBlockSprites() {
        if (cachedSprites != null && !cachedSprites.isEmpty()) {
            return cachedSprites;
        }
        Minecraft mc = Minecraft.getInstance();
        BlockModelShaper shaper = mc.getBlockRenderer().getBlockModelShaper();
        TextureAtlasSprite missing = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "missingno"));

        List<SpriteEntry> list = new ArrayList<>(512);
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block == ModBlocks.GLITCH_BLOCK.get() || block instanceof LiquidBlock) {
                continue;
            }
            BlockState state = block.defaultBlockState();
            if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }
            BakedModel model = shaper.getBlockModel(state);
            if (model == null) {
                continue;
            }
            TextureAtlasSprite sprite = model.getParticleIcon();
            if (sprite != null && !sprite.equals(missing)) {
                list.add(new SpriteEntry(sprite, state));
            }
        }
        cachedSprites = list;
        return cachedSprites;
    }

    @Override
    public void render(GlitchBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        List<SpriteEntry> pool = getAllBlockSprites();
        if (pool.isEmpty()) {
            return;
        }
        Level level = entity.getLevel();
        BlockPos pos = entity.getBlockPos();
        long tick = level != null ? level.getGameTime() : (System.currentTimeMillis() / 50L);

        long step = tick / 2L;
        long posHash = pos.asLong() * 0x9E3779B97F4A7C15L;
        int baseIdx = Math.floorMod((int) ((step * 2654435761L) ^ posHash ^ (posHash >>> 32)), pool.size());
        int altIdx = Math.floorMod((int) (((step + 7L) * 1597334677L) ^ (posHash >>> 16)), pool.size());

        SpriteEntry main = pool.get(baseIdx);
        SpriteEntry alt = (step % 5L == 0L) ? pool.get(altIdx) : main;

        Minecraft mc = Minecraft.getInstance();
        BlockColors colors = mc.getBlockColors();
        int tintMain = colors.getColor(main.state(), level, pos, 0);
        int tintAlt = colors.getColor(alt.state(), level, pos, 0);

        int rM = tintMain == -1 ? 255 : (tintMain >> 16) & 0xFF;
        int gM = tintMain == -1 ? 255 : (tintMain >> 8) & 0xFF;
        int bM = tintMain == -1 ? 255 : tintMain & 0xFF;

        int rA = tintAlt == -1 ? 255 : (tintAlt >> 16) & 0xFF;
        int gA = tintAlt == -1 ? 255 : (tintAlt >> 8) & 0xFF;
        int bA = tintAlt == -1 ? 255 : tintAlt & 0xFF;

        VertexConsumer vc = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Matrix3f norm = last.normal();

        int light = Math.max(packedLight, LightTexture.pack(10, 10));

        // Down (-Y)
        quad(vc, pose, norm, main.sprite(), rM, gM, bM, light,
                0, 0, 1,  0, 0, 0,  1, 0, 0,  1, 0, 1,
                0, -1, 0);
        // Up (+Y)
        quad(vc, pose, norm, alt.sprite(), rA, gA, bA, light,
                0, 1, 0,  0, 1, 1,  1, 1, 1,  1, 1, 0,
                0, 1, 0);
        // North (-Z)
        quad(vc, pose, norm, main.sprite(), rM, gM, bM, light,
                1, 1, 0,  1, 0, 0,  0, 0, 0,  0, 1, 0,
                0, 0, -1);
        // South (+Z)
        quad(vc, pose, norm, alt.sprite(), rA, gA, bA, light,
                0, 1, 1,  0, 0, 1,  1, 0, 1,  1, 1, 1,
                0, 0, 1);
        // West (-X)
        quad(vc, pose, norm, main.sprite(), rM, gM, bM, light,
                0, 1, 0,  0, 0, 0,  0, 0, 1,  0, 1, 1,
                -1, 0, 0);
        // East (+X)
        quad(vc, pose, norm, alt.sprite(), rA, gA, bA, light,
                1, 1, 1,  1, 0, 1,  1, 0, 0,  1, 1, 0,
                1, 0, 0);
    }

    private static void quad(VertexConsumer vc, Matrix4f pose, Matrix3f norm, TextureAtlasSprite sprite,
                             int r, int g, int b, int light,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float nx, float ny, float nz) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vc.vertex(pose, x0, y0, z0).color(r, g, b, 255).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        vc.vertex(pose, x1, y1, z1).color(r, g, b, 255).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        vc.vertex(pose, x2, y2, z2).color(r, g, b, 255).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
        vc.vertex(pose, x3, y3, z3).color(r, g, b, 255).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(norm, nx, ny, nz).endVertex();
    }
}
