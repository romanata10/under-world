package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class EyesOfFunEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_FROZEN =
            SynchedEntityData.defineId(EyesOfFunEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_FROZEN = RawAnimation.begin().thenLoop("misc.frozen");
    private static final RawAnimation ANIM_STALK = RawAnimation.begin().thenLoop("move.stalk");
    private static final RawAnimation ANIM_STRIKE = RawAnimation.begin().thenPlay("attack.strike");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean playedSpotSound = false;
    private int despawnCheckTicks = 0;

    public EyesOfFunEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 65.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FROZEN, true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true) {
            @Override
            public boolean canUse() {
                return !EyesOfFunEntity.this.isFrozen() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !EyesOfFunEntity.this.isFrozen() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public boolean isFrozen() {
        return this.entityData.get(DATA_FROZEN);
    }

    public void setFrozen(boolean frozen) {
        this.entityData.set(DATA_FROZEN, frozen);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Frozen", this.isFrozen());
        tag.putBoolean("PlayedSpotSound", this.playedSpotSound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFrozen(tag.getBoolean("Frozen"));
        this.playedSpotSound = tag.getBoolean("PlayedSpotSound");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (!this.isFrozen() && this.random.nextInt(4) == 0) {
                this.level().addParticle(
                        ParticleTypes.CRIMSON_SPORE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.3D + this.random.nextDouble() * 1.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        // Vanish gracefully at bright daytime inside The Fun World
        if (ModDimensions.isFunWorld(serverLevel)) {
            long timeOfDay = serverLevel.getDayTime() % 24000L;
            if (timeOfDay < 12500L && ++this.despawnCheckTicks > 60) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 1.2D, this.getZ(), 12, 0.4D, 0.8D, 0.4D, 0.02D);
                this.discard();
                return;
            }
        }

        Player observer = findObservingPlayer(serverLevel);
        boolean lookedAt = observer != null;
        this.setFrozen(lookedAt);

        if (lookedAt) {
            if (!this.playedSpotSound) {
                this.playedSpotSound = true;
                serverLevel.playSound(null, this.blockPosition(), ModSounds.EYES_OF_FUN_SPOT.get(), SoundSource.HOSTILE, 1.3F, 1.0F);
            }
            this.getNavigation().stop();
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, Math.min(0.0D, vel.y), 0.0D);
            this.getLookControl().setLookAt(observer, 15.0F, 15.0F);
        } else {
            LivingEntity target = this.getTarget();
            if (target == null || !target.isAlive()) {
                Player nearest = serverLevel.getNearestPlayer(this, 48.0D);
                if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                    this.setTarget(nearest);
                }
            }
            if (this.getTarget() != null && this.distanceToSqr(this.getTarget()) <= 2.3D * 2.3D && this.tickCount % 10 == 0) {
                this.doHurtTarget(this.getTarget());
            }
        }
    }

    @Nullable
    private Player findObservingPlayer(ServerLevel level) {
        for (Player player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
                continue;
            }
            if (player.distanceToSqr(this) > 48.0D * 48.0D) {
                continue;
            }
            if (isPlayerLookingAtEntity(player)) {
                return player;
            }
        }
        return null;
    }

    private boolean isPlayerLookingAtEntity(Player player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 targetCenter = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.72D, this.getZ());
        Vec3 toEntity = targetCenter.subtract(eyePos);
        double dist = toEntity.length();
        if (dist < 0.01D) {
            return true;
        }
        double dot = look.dot(toEntity.scale(1.0D / dist));
        if (dot < 0.65D) {
            return false;
        }
        HitResult hit = this.level().clip(new ClipContext(
                eyePos, targetCenter, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.isFrozen()) {
            return false;
        }
        this.triggerAnim("Actions", "strike");
        boolean instantKill = UnderWorldConfigHolder.get() == null
                || UnderWorldConfigHolder.get().dimensions.eyesOfFunInstantKill.get();
        float dmg = instantKill ? 999.0F : (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean hurt = target.hurt(this.damageSources().mobAttack(this), dmg);
        if (hurt && !this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.EYES_OF_FUN_KILL.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.0D, target.getZ(), 24, 0.5D, 0.8D, 0.5D, 0.05D);
        }
        return hurt;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 2, state -> {
            if (this.isFrozen() || !state.isMoving()) {
                return state.setAndContinue(ANIM_FROZEN);
            }
            return state.setAndContinue(ANIM_STALK);
        }));
        controllers.add(new AnimationController<>(this, "Actions", state -> PlayState.STOP)
                .triggerableAnim("strike", ANIM_STRIKE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
