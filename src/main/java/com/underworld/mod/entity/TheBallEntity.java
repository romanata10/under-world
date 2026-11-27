package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModEffects;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TheBallEntity extends PathfinderMob {
    private int lifeTicks = 0;

    public TheBallEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public float getMorphScaleX(float partialTick) {
        float t = this.tickCount + partialTick;
        float sizePulse = 1.0F + 0.42F * Mth.sin(t * 0.12F);
        float widen = 1.0F + 0.50F * Mth.sin(t * 0.19F + 1.4F);
        return Mth.clamp(sizePulse * widen, 0.42F, 2.25F);
    }

    public float getMorphScaleY(float partialTick) {
        float t = this.tickCount + partialTick;
        float sizePulse = 1.0F + 0.42F * Mth.sin(t * 0.12F);
        float squish = 1.0F - 0.30F * Mth.sin(t * 0.19F + 1.4F);
        return Mth.clamp(sizePulse * squish, 0.40F, 1.85F);
    }

    public float getMorphScaleZ(float partialTick) {
        float t = this.tickCount + partialTick;
        float sizePulse = 1.0F + 0.42F * Mth.sin(t * 0.12F);
        float widen = 1.0F + 0.50F * Mth.sin(t * 0.19F + 1.4F);
        return Mth.clamp(sizePulse * widen, 0.42F, 2.25F);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof ServerPlayer player) {
            triggerTouchEffect((ServerLevel) this.level(), player);
        }
        return false;
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide && player instanceof ServerPlayer sp && this.isAlive()) {
            triggerTouchEffect((ServerLevel) this.level(), sp);
        }
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();
        this.lifeTicks++;

        if (this.level().isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.level();

        // Despawn cleanly if nobody is nearby after 4 minutes
        if (this.lifeTicks > 4800) {
            this.discard();
            return;
        }

        // Hover mysteriously ~1.7 blocks above ground with slow bobbing & tiny drift
        double targetHoverY = findHoverTargetY(serverLevel);
        double bobOffset = Math.sin(this.lifeTicks * 0.08D) * 0.28D;
        double desiredY = targetHoverY + bobOffset;
        double vy = Mth.clamp((desiredY - this.getY()) * 0.12D, -0.18D, 0.18D);
        double vx = Math.cos(this.lifeTicks * 0.03D) * 0.018D;
        double vz = Math.sin(this.lifeTicks * 0.03D) * 0.018D;
        this.setDeltaMovement(vx, vy, vz);

        if (this.lifeTicks % 45 == 0) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_BALL_HUM.get(), SoundSource.AMBIENT, 1.1F, 0.85F);
        }
        if (this.lifeTicks % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.7D, this.getZ(),
                    2, 0.35D, 0.35D, 0.35D, 0.01D);
        }

        // Check proximity touch so morphing ball triggers even before hitbox edge
        float sx = getMorphScaleX(0.0F);
        double touchRadius = 1.15D * Math.max(1.0F, sx * 0.75F);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.isSpectator() || !player.isAlive()) continue;
            Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
            Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
            if (center.distanceTo(playerCenter) <= touchRadius + 0.55D
                    || this.getBoundingBox().inflate(0.35D).intersects(player.getBoundingBox())) {
                triggerTouchEffect(serverLevel, player);
                return;
            }
        }
    }

    private double findHoverTargetY(ServerLevel level) {
        BlockPos start = this.blockPosition();
        for (int dy = 0; dy <= 10; dy++) {
            BlockPos check = start.below(dy);
            if (check.getY() <= level.getMinBuildHeight()) break;
            if (!level.getBlockState(check).isAir()) {
                return check.getY() + 1.85D;
            }
        }
        return this.getY();
    }

    private void triggerTouchEffect(ServerLevel level, ServerPlayer player) {
        if (!this.isAlive()) return;
        int duration = UnderWorldConfigHolder.getTheBallEffectDurationTicks();
        player.addEffect(new MobEffectInstance(ModEffects.GEOMETRIC_NIGHTMARE.get(), duration, 0, false, true, true));

        level.playSound(null, this.blockPosition(), ModSounds.GEOMETRIC_NIGHTMARE.get(), SoundSource.HOSTILE, 1.8F, 0.95F);
        level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.7D, this.getZ(), 28, 0.45D, 0.45D, 0.45D, 0.10D);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.7D, this.getZ(), 18, 0.50D, 0.50D, 0.50D, 0.08D);
        this.discard();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }
}
