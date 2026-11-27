package com.underworld.mod.entity;

import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class WindTurbineStalkerEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_AWAKENED =
            SynchedEntityData.defineId(WindTurbineStalkerEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_DORMANT = RawAnimation.begin().thenLoop("misc.dormant");
    private static final RawAnimation ANIM_AWAKENED = RawAnimation.begin().thenLoop("misc.awakened");
    private static final RawAnimation ANIM_STALK = RawAnimation.begin().thenLoop("move.stalk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int drownCooldown = 0;

    public WindTurbineStalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 55;
        this.setMaxUpStep(2.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 88.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.98D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_AWAKENED, false);
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean isAwakened() {
        return this.entityData.get(DATA_AWAKENED);
    }

    public void setAwakened(boolean awakened) {
        this.entityData.set(DATA_AWAKENED, awakened);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Awakened", this.isAwakened());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAwakened(tag.getBoolean("Awakened"));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean res = super.hurt(source, amount);
        if (res && !this.level().isClientSide && !this.isAwakened()) {
            awaken((ServerLevel) this.level());
        }
        return res;
    }

    private void awaken(ServerLevel level) {
        setAwakened(true);
        level.playSound(null, this.blockPosition(), ModSounds.TURBINE_WHOOSH.get(), SoundSource.HOSTILE, 2.2F, 0.72F);
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), 63.2D, this.getZ(), 48, 2.2D, 0.6D, 2.2D, 0.18D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.drownCooldown > 0) this.drownCooldown--;

        ServerPlayer nearest = findNearestSurvivalPlayer(level, 88.0D);
        if (!this.isAwakened()) {
            // Stand vertically in ocean at waterline y=60 looking like a normal offshore wind turbine
            this.setNoGravity(true);
            double targetY = this.isInWater() ? 60.0D : this.getY();
            this.setDeltaMovement(0.0D, (targetY - this.getY()) * 0.2D, 0.0D);
            if (nearest != null) {
                double dx = nearest.getX() - this.getX();
                double dz = nearest.getZ() - this.getZ();
                float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                this.setYRot(yaw);
                this.yHeadRot = yaw;
                if (this.distanceToSqr(nearest) <= 20.0D * 20.0D) {
                    awaken(level);
                }
            }
            return;
        }

        if (nearest == null) {
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            return;
        }

        this.setTarget(nearest);
        // Stride rapidly on giant spindly legs right along wave surface y=59..61 toward victim!
        this.setNoGravity(true);
        Vec3 toTarget = new Vec3(nearest.getX() - this.getX(), 0.0D, nearest.getZ() - this.getZ());
        double horizDist = toTarget.horizontalDistance();
        if (horizDist > 0.2D) {
            Vec3 dir = toTarget.scale(1.0D / horizDist);
            float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(yaw);
            this.yRotO = yaw;
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;

            double bobY = 59.5D;
            double dy = (bobY - this.getY()) * 0.22D;
            this.setDeltaMovement(dir.x * 0.34D, dy, dir.z * 0.34D);
        }

        if (this.tickCount % 14 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.TURBINE_STEP.get(), SoundSource.HOSTILE, 2.0F, 0.85F + this.random.nextFloat() * 0.15F);
            level.sendParticles(ParticleTypes.SPLASH, this.getX(), 63.0D, this.getZ(), 18, 1.5D, 0.2D, 1.5D, 0.12D);
        }
        if (this.tickCount % 44 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.TURBINE_WHOOSH.get(), SoundSource.HOSTILE, 1.6F, 0.85F);
        }

        // Catch & capsize boat, then drag player underwater to drown!
        double flatDistSq = (nearest.getX() - this.getX()) * (nearest.getX() - this.getX())
                + (nearest.getZ() - this.getZ()) * (nearest.getZ() - this.getZ());
        if (flatDistSq <= 4.6D * 4.6D) {
            if (nearest.getVehicle() instanceof Boat boat && boat.isAlive()) {
                level.playSound(null, boat.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 2.0F, 0.7F);
                level.sendParticles(ParticleTypes.SPLASH, boat.getX(), boat.getY() + 0.5D, boat.getZ(), 32, 0.8D, 0.4D, 0.8D, 0.15D);
                nearest.stopRiding();
                boat.discard();
            }
            if (this.drownCooldown <= 0) {
                this.drownCooldown = 14;
                nearest.setDeltaMovement(0.0D, -0.58D, 0.0D);
                nearest.hurtMarked = true;
                nearest.setAirSupply(Math.max(0, nearest.getAirSupply() - 55));
                nearest.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                nearest.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
                nearest.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
                nearest.hurt(this.damageSources().drown(), 7.0F);
                level.sendParticles(ParticleTypes.BUBBLE, nearest.getX(), nearest.getY() + 0.8D, nearest.getZ(), 18, 0.6D, 0.5D, 0.6D, 0.1D);
            }
        }
    }

    @Nullable
    private ServerPlayer findNearestSurvivalPlayer(ServerLevel level, double radius) {
        double best = radius * radius;
        ServerPlayer found = null;
        for (ServerPlayer p : level.players()) {
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < best) {
                best = d;
                found = p;
            }
        }
        return found;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, state -> {
            if (!this.isAwakened()) {
                return state.setAndContinue(ANIM_DORMANT);
            }
            if (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
                return state.setAndContinue(ANIM_STALK);
            }
            return state.setAndContinue(ANIM_AWAKENED);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
