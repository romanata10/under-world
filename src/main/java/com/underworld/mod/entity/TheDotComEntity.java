package com.underworld.mod.entity;

import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class TheDotComEntity extends Monster implements GeoEntity {
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_CHASE = RawAnimation.begin().thenLoop("move.chase");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicksRemaining = 2400;

    public TheDotComEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 70;
        this.setMaxUpStep(1.5F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.39D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.18D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (reason == MobSpawnType.EVENT) {
            this.lifeTicksRemaining = 2400;
        } else {
            this.lifeTicksRemaining = -1;
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifeTicks", this.lifeTicksRemaining);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("LifeTicks")) {
            this.lifeTicksRemaining = tag.getInt("LifeTicks");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (this.lifeTicksRemaining > 0) {
            this.lifeTicksRemaining--;
            if (this.lifeTicksRemaining == 0) {
                level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 2.2D, this.getZ(),
                        28, 0.8D, 1.5D, 0.8D, 0.08D);
                this.discard();
                return;
            }
        }

        if (ModDimensions.isBrowser(level) && level.players().isEmpty() && this.tickCount > 100) {
            this.discard();
            return;
        }

        if (this.tickCount % 4 == 0) {
            level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 2.6D, this.getZ(),
                    2, 0.5D, 0.9D, 0.5D, 0.02D);
            if (this.random.nextInt(3) == 0) {
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 3.8D, this.getZ(),
                        1, 0.3D, 0.3D, 0.3D, 0.01D);
            }
        }
        if (this.tickCount % 55 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.DOT_COM_ROAR.get(), SoundSource.HOSTILE, 1.6F, 0.85F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
            player.serverLevel().playSound(null, player.blockPosition(), ModSounds.DOT_COM_ROAR.get(), SoundSource.HOSTILE, 1.5F, 1.25F);
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
                return state.setAndContinue(ANIM_CHASE);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
