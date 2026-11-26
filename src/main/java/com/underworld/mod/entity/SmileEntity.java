package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SmileEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_SPRINTING =
            SynchedEntityData.defineId(SmileEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_ROAR = RawAnimation.begin().thenPlay("misc.roar");
    private static final RawAnimation ANIM_SPRINT = RawAnimation.begin().thenLoop("move.sprint");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int roarCooldown = 0;
    private boolean roaredOnAggro = false;
    private double lastX = 0.0D;
    private double lastZ = 0.0D;
    private int stuckTicks = 0;

    public SmileEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 35;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.43D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPRINTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.85D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public boolean isSmileSprinting() {
        return this.entityData.get(DATA_SPRINTING);
    }

    public void setSmileSprinting(boolean sprinting) {
        this.entityData.set(DATA_SPRINTING, sprinting);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sprinting", this.isSmileSprinting());
        tag.putBoolean("RoaredOnAggro", this.roaredOnAggro);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSmileSprinting(tag.getBoolean("Sprinting"));
        this.roaredOnAggro = tag.getBoolean("RoaredOnAggro");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.isSmileSprinting() && this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.9D,
                        this.getY() + 0.1D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.9D,
                        0.0D, 0.04D, 0.0D
                );
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }

        double speed = UnderWorldConfigHolder.get() != null
                ? UnderWorldConfigHolder.get().dimensions.smileSprintSpeed.get()
                : 0.43D;
        var moveAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (moveAttr != null && Math.abs(moveAttr.getBaseValue() - speed) > 0.001D) {
            moveAttr.setBaseValue(speed);
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            Player nearest = serverLevel.getNearestPlayer(this, 64.0D);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                this.setTarget(nearest);
                target = nearest;
            }
        }

        boolean sprinting = target != null && target.isAlive() && !((target instanceof Player p) && (p.isCreative() || p.isSpectator()));
        this.setSmileSprinting(sprinting);

        if (sprinting) {
            if (!this.roaredOnAggro || (this.roarCooldown <= 0 && this.distanceToSqr(target) < 28.0D * 28.0D && this.random.nextInt(140) == 0)) {
                this.roaredOnAggro = true;
                this.roarCooldown = 140;
                this.triggerAnim("Actions", "roar");
                serverLevel.playSound(null, this.blockPosition(), ModSounds.SMILE_ROAR.get(), SoundSource.HOSTILE, 2.5F, 0.92F + this.random.nextFloat() * 0.15F);
            }

            if (this.tickCount % 5 == 0 && this.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
                serverLevel.playSound(null, this.blockPosition(), ModSounds.SMILE_STEP.get(), SoundSource.HOSTILE, 1.3F, 0.9F + this.random.nextFloat() * 0.25F);
            }

            // Re-path cleanly around Maze walls if temporarily stalled
            double movedSq = (this.getX() - this.lastX) * (this.getX() - this.lastX)
                    + (this.getZ() - this.lastZ) * (this.getZ() - this.lastZ);
            if (movedSq < 0.005D) {
                this.stuckTicks++;
                if (this.stuckTicks > 20) {
                    this.stuckTicks = 0;
                    this.getNavigation().moveTo(target, 1.3D);
                }
            } else {
                this.stuckTicks = 0;
            }
        } else {
            this.roaredOnAggro = false;
        }
        this.lastX = this.getX();
        this.lastZ = this.getZ();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("Actions", "roar");
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.SMILE_ROAR.get(), SoundSource.HOSTILE, 2.0F, 1.1F);
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 2, state -> {
            if (this.isSmileSprinting() || state.isMoving()) {
                return state.setAndContinue(ANIM_SPRINT);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
        controllers.add(new AnimationController<>(this, "Actions", state -> PlayState.STOP)
                .triggerableAnim("roar", ANIM_ROAR));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
