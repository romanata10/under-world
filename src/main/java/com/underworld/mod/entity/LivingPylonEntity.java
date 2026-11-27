package com.underworld.mod.entity;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class LivingPylonEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_SHOCKING =
            SynchedEntityData.defineId(LivingPylonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation ANIM_SHOCK = RawAnimation.begin().thenLoop("attack.shock");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean autoDespawnAtDay = false;
    private int shockCooldown = 0;
    private int shockAnimTicks = 0;

    public LivingPylonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 45;
        this.setMaxUpStep(2.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 80.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SHOCKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 40.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public void setAutoDespawnAtDay(boolean value) {
        this.autoDespawnAtDay = value;
    }

    public boolean isShocking() {
        return this.entityData.get(DATA_SHOCKING);
    }

    public void setShocking(boolean shocking) {
        this.entityData.set(DATA_SHOCKING, shocking);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (reason == MobSpawnType.EVENT || reason == MobSpawnType.NATURAL) {
            this.autoDespawnAtDay = true;
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("AutoDespawnAtDay", this.autoDespawnAtDay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.autoDespawnAtDay = tag.getBoolean("AutoDespawnAtDay");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (this.shockCooldown > 0) this.shockCooldown--;
        if (this.shockAnimTicks > 0) {
            this.shockAnimTicks--;
            if (this.shockAnimTicks == 0) setShocking(false);
        }

        // Collapse in sparks if dawn arrives inside The Plains
        if (this.autoDespawnAtDay && ModDimensions.isPlains(level) && level.isDay()) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 3.5D, this.getZ(),
                    32, 1.2D, 2.5D, 1.2D, 0.1D);
            level.playSound(null, this.blockPosition(), ModSounds.PYLON_HUM.get(), SoundSource.HOSTILE, 1.6F, 0.65F);
            this.discard();
            return;
        }

        // Dangling black electrical thread & staring eye particles underneath belly
        if (this.tickCount % 3 == 0) {
            for (int i = 0; i < 3; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 2.2D;
                double oz = (this.random.nextDouble() - 0.5D) * 2.2D;
                double oy = 1.6D + this.random.nextDouble() * 2.6D;
                level.sendParticles(ParticleTypes.SQUID_INK, this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                        1, 0.02D, 0.12D, 0.02D, 0.0D);
                if (this.random.nextInt(4) == 0) {
                    level.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX() + ox, this.getY() + 1.4D, this.getZ() + oz,
                            1, 0.03D, 0.03D, 0.03D, 0.0D);
                }
            }
        }

        // Heavy footsteps & 60Hz transformer hum
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.002D && this.tickCount % 12 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.PYLON_STEP.get(), SoundSource.HOSTILE, 1.6F, 0.85F + this.random.nextFloat() * 0.2F);
        }
        if (this.tickCount % 48 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.PYLON_HUM.get(), SoundSource.HOSTILE, 1.3F, 0.92F);
        }

        // Electrocution attack with torn wires when catching a victim!
        ServerPlayer target = findNearestSurvivalPlayer(level, 7.2D);
        if (target != null && this.shockCooldown <= 0) {
            this.shockCooldown = 26;
            this.shockAnimTicks = 14;
            setShocking(true);
            strikeWithTornWires(level, target);
        }
    }

    private void strikeWithTornWires(ServerLevel level, ServerPlayer target) {
        level.playSound(null, target.blockPosition(), ModSounds.PYLON_ELECTROCUTE.get(), SoundSource.HOSTILE, 2.0F, 0.95F);
        Vec3 armSource = this.position().add(0.0D, 5.4D, 0.0D);
        Vec3 victimPos = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        for (int i = 1; i <= 12; i++) {
            double t = i / 12.0D;
            Vec3 arc = armSource.lerp(victimPos, t).add(
                    (this.random.nextDouble() - 0.5D) * 0.45D,
                    (this.random.nextDouble() - 0.5D) * 0.45D,
                    (this.random.nextDouble() - 0.5D) * 0.45D
            );
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, arc.x, arc.y, arc.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
            if (i % 3 == 0) {
                level.sendParticles(ParticleTypes.SQUID_INK, arc.x, arc.y, arc.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        target.hurt(this.damageSources().lightningBolt(), 26.0F);
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
            if (this.isShocking()) {
                return state.setAndContinue(ANIM_SHOCK);
            }
            if (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
                return state.setAndContinue(ANIM_WALK);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
