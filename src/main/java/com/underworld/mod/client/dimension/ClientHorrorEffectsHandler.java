package com.underworld.mod.client.dimension;

import com.underworld.mod.entity.TheMimicEntity;
import com.underworld.mod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class ClientHorrorEffectsHandler {
    private ClientHorrorEffectsHandler() {}

    @SubscribeEvent
    public static void onComputeCameraAngles(final ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        float time = (System.currentTimeMillis() % 600000L) * 0.001F;
        float rollDelta = 0.0F;
        float yawDelta = 0.0F;
        float pitchDelta = 0.0F;

        // Geometric Nightmare: pronounced warping camera roll + nauseous sway
        if (mc.player.hasEffect(ModEffects.GEOMETRIC_NIGHTMARE.get())) {
            rollDelta += (float) Math.sin(time * 2.1F) * 14.5F + (float) Math.cos(time * 5.3F) * 4.5F;
            yawDelta += (float) Math.sin(time * 1.7F) * 2.8F;
            pitchDelta += (float) Math.cos(time * 2.3F) * 2.2F;
        }

        // Transformed the_mimic chasing nearby: ominous dread tremor & screen shake
        double mimicStrength = getClosestTransformedMimicStrength(mc);
        if (mimicStrength > 0.0D) {
            float shake = (float) mimicStrength;
            yawDelta += (float) Math.sin(time * 47.0F) * 1.9F * shake;
            pitchDelta += (float) Math.cos(time * 53.0F) * 1.7F * shake;
            rollDelta += (float) Math.sin(time * 39.0F) * 2.6F * shake;
        }

        if (rollDelta != 0.0F || yawDelta != 0.0F || pitchDelta != 0.0F) {
            event.setRoll(event.getRoll() + rollDelta);
            event.setYaw(event.getYaw() + yawDelta);
            event.setPitch(event.getPitch() + pitchDelta);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(final ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        float time = (System.currentTimeMillis() % 600000L) * 0.001F;
        double fov = event.getFOV();

        if (mc.player.hasEffect(ModEffects.GEOMETRIC_NIGHTMARE.get())) {
            fov += Math.sin(time * 2.9F) * 18.0D + Math.sin(time * 7.1F) * 6.5D;
        }

        double mimicStrength = getClosestTransformedMimicStrength(mc);
        if (mimicStrength > 0.0D) {
            fov -= 6.5D * mimicStrength * (0.5D + 0.5D * Math.sin(time * 18.0F));
        }

        event.setFOV(Math.max(30.0D, Math.min(135.0D, fov)));
    }

    private static double getClosestTransformedMimicStrength(Minecraft mc) {
        if (mc.player == null || mc.level == null) return 0.0D;
        AABB box = mc.player.getBoundingBox().inflate(24.0D);
        List<TheMimicEntity> mimics = mc.level.getEntitiesOfClass(TheMimicEntity.class, box, TheMimicEntity::isTransformed);
        double bestDistSq = 24.0D * 24.0D;
        boolean found = false;
        for (TheMimicEntity mimic : mimics) {
            double dSq = mimic.distanceToSqr(mc.player);
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                found = true;
            }
        }
        if (!found) return 0.0D;
        double dist = Math.sqrt(bestDistSq);
        return Math.max(0.0D, Math.min(1.0D, 1.0D - (dist / 24.0D)));
    }
}
