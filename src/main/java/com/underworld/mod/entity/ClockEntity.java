package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import com.underworld.mod.world.dimension.TimeChunkGenerator;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class ClockEntity extends PathfinderMob implements GeoEntity {
    public static final int MODE_STALK_VANISH = 0;
    public static final int MODE_CHASE_WARP = 1;

    private static final EntityDataAccessor<Boolean> DATA_TIME_VARIANT =
            SynchedEntityData.defineId(ClockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FROZEN =
            SynchedEntityData.defineId(ClockEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CHASING =
            SynchedEntityData.defineId(ClockEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_FROZEN = RawAnimation.begin().thenLoop("misc.frozen");
    private static final RawAnimation ANIM_STALK = RawAnimation.begin().thenLoop("move.stalk");
    private static final RawAnimation ANIM_CHASE = RawAnimation.begin().thenLoop("move.chase");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int behaviorMode = MODE_CHASE_WARP;
    private int spottedTicks = 0;
    private int ageInWorld = 0;
    private int attackCooldown = 0;
    private int leapCooldown = 0;

    public ClockEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.5F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 140.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 80.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TIME_VARIANT, false);
        this.entityData.define(DATA_FROZEN, false);
        this.entityData.define(DATA_CHASING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 64.0F));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (ModDimensions.isTime(level.getLevel())) {
            setTimeVariant(true);
            this.behaviorMode = MODE_CHASE_WARP;
        } else {
            this.behaviorMode = level.getRandom().nextFloat() < 0.55F ? MODE_CHASE_WARP : MODE_STALK_VANISH;
        }
        return data;
    }

    public void setTimeVariant(boolean variant) {
        this.entityData.set(DATA_TIME_VARIANT, variant);
    }

    public boolean isTimeVariant() {
        return this.entityData.get(DATA_TIME_VARIANT);
    }

    public void setFrozen(boolean frozen) {
        this.entityData.set(DATA_FROZEN, frozen);
    }

    public boolean isFrozen() {
        return this.entityData.get(DATA_FROZEN);
    }

    public void setChasing(boolean chasing) {
        this.entityData.set(DATA_CHASING, chasing);
    }

    public boolean isChasing() {
        return this.entityData.get(DATA_CHASING);
    }

    public void setBehaviorMode(int mode) {
        this.behaviorMode = mode;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TimeVariant", isTimeVariant());
        tag.putInt("BehaviorMode", this.behaviorMode);
        tag.putInt("AgeInWorld", this.ageInWorld);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TimeVariant")) {
            setTimeVariant(tag.getBoolean("TimeVariant"));
        }
        if (tag.contains("BehaviorMode")) {
            this.behaviorMode = tag.getInt("BehaviorMode");
        }
        this.ageInWorld = tag.getInt("AgeInWorld");
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (ModDimensions.isTime(serverLevel) && !isTimeVariant()) {
            setTimeVariant(true);
        }

        this.ageInWorld++;
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.leapCooldown > 0) this.leapCooldown--;

        ServerPlayer nearest = findNearestSurvivalPlayer(serverLevel, 80.0D);

        if (isTimeVariant()) {
            tickTimeVariant(serverLevel, nearest);
        } else {
            tickOverworldVariant(serverLevel, nearest);
        }
    }

    private void tickTimeVariant(ServerLevel level, @Nullable ServerPlayer target) {
        // Rescue if Clock slipped into endless clouds
        if (this.getY() < 12.0D) {
            BlockPos rescue = target != null
                    ? TimeChunkGenerator.findNearestSafeDiscPos(target.getBlockX(), target.getBlockZ())
                    : TimeChunkGenerator.findNearestSafeDiscPos(this.getBlockX(), this.getBlockZ());
            this.teleportTo(rescue.getX() + 0.5D, rescue.getY(), rescue.getZ() + 0.5D);
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (target == null || !target.isAlive()) {
            setFrozen(true);
            setChasing(false);
            this.getNavigation().stop();
            return;
        }

        // Synchronized ticking window: 38 ticks active ticking, 26 ticks dead silence freeze!
        int cycle = (int) (level.getGameTime() % 64L);
        boolean tickingActive = cycle < 38;

        if (!tickingActive) {
            // FROZEN PHASE: completely immobile statue while silence lasts!
            setFrozen(true);
            setChasing(false);
            this.getNavigation().stop();
            Vec3 vel = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, Math.min(0.0D, vel.y), 0.0D);
            return;
        }

        // ACTIVE TICKING PHASE: clock ticks audibly & Clock aggressively hunts!
        setFrozen(false);
        setChasing(true);
        if (cycle == 0 || cycle == 8 || cycle == 16 || cycle == 24 || cycle == 32) {
            level.playSound(null, this.blockPosition(), ModSounds.CLOCK_TICK.get(), SoundSource.HOSTILE, 2.2F,
                    cycle % 16 == 0 ? 1.0F : 0.86F);
        }

        faceTarget(target);
        double speed = UnderWorldConfigHolder.getClockTimeSpeed();
        this.getNavigation().moveTo(target, speed / 0.32D);

        double distSq = this.distanceToSqr(target);
        // Leap across clock disc gaps if pathfinder hits edge and target is ahead
        if (this.leapCooldown <= 0 && distSq > 9.0D && distSq < 36.0D * 36.0D && (this.onGround() || this.getNavigation().isDone())) {
            Vec3 toTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            double horiz = toTarget.horizontalDistance();
            if (horiz > 0.5D) {
                BlockPos frontFloor = BlockPos.containing(
                        this.getX() + (toTarget.x / horiz) * 2.2D,
                        this.getY() - 1.0D,
                        this.getZ() + (toTarget.z / horiz) * 2.2D
                );
                if (level.getBlockState(frontFloor).isAir() || Math.abs(target.getY() - this.getY()) > 1.5D) {
                    Vec3 dir = toTarget.normalize();
                    double yBoost = target.getY() > this.getY() + 1.0D ? 0.58D : 0.44D;
                    this.setDeltaMovement(dir.x * 0.58D, yBoost, dir.z * 0.58D);
                    this.leapCooldown = 14;
                }
            }
        }

        // Lethal aggressive slash when catching victim during a tick window
        if (distSq <= 2.65D * 2.65D && this.attackCooldown <= 0) {
            this.attackCooldown = 16;
            target.hurt(this.damageSources().mobAttack(this), 11.0F);
            level.playSound(null, target.blockPosition(), ModSounds.CLOCK_CHIME.get(), SoundSource.HOSTILE, 1.8F, 0.9F);
        }
    }

    private void tickOverworldVariant(ServerLevel level, @Nullable ServerPlayer target) {
        if (this.ageInWorld > 900 && (target == null || this.distanceToSqr(target) > 44.0D * 44.0D)) {
            vanishWithSmoke(level);
            return;
        }
        if (target == null) {
            setChasing(false);
            setFrozen(false);
            return;
        }

        faceTarget(target);
        double distSq = this.distanceToSqr(target);

        if (this.behaviorMode == MODE_STALK_VANISH) {
            setChasing(false);
            setFrozen(false);
            if (this.ageInWorld % 24 == 0) {
                level.playSound(null, this.blockPosition(), ModSounds.CLOCK_TICK.get(), SoundSource.HOSTILE, 0.95F, 0.92F);
            }
            // Slowly creep closer only while player looks away
            if (!isLookingAtMe(target) && distSq > 14.0D * 14.0D) {
                this.getNavigation().moveTo(target, 0.85D);
            } else {
                this.getNavigation().stop();
                this.spottedTicks++;
            }
            if (distSq < 9.5D * 9.5D || this.spottedTicks > 18) {
                level.playSound(null, this.blockPosition(), ModSounds.CLOCK_TICK.get(), SoundSource.HOSTILE, 1.4F, 0.72F);
                vanishWithSmoke(level);
            }
            return;
        }

        // MODE_CHASE_WARP: locks eyes for 15 ticks then sprints and warps caught victim to The Time!
        if (this.ageInWorld == 14) {
            level.playSound(null, this.blockPosition(), ModSounds.CLOCK_CHIME.get(), SoundSource.HOSTILE, 1.8F, 0.85F);
        }
        if (this.ageInWorld < 15) {
            setFrozen(true);
            setChasing(false);
            return;
        }

        setFrozen(false);
        setChasing(true);
        if (this.ageInWorld % 8 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.CLOCK_TICK.get(), SoundSource.HOSTILE, 1.6F,
                    (this.ageInWorld % 16 == 0) ? 1.05F : 0.88F);
        }

        this.getNavigation().moveTo(target, 1.32D);

        if (distSq <= 2.45D * 2.45D) {
            level.playSound(null, target.blockPosition(), ModSounds.CLOCK_CHIME.get(), SoundSource.HOSTILE, 2.0F, 0.78F);
            ModDimensions.sendToTime(target);
            vanishWithSmoke(level);
        }
    }

    private void faceTarget(Player target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private boolean isLookingAtMe(Player player) {
        Vec3 view = player.getViewVector(1.0F).normalize();
        Vec3 toEntity = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ()).normalize();
        return view.dot(toEntity) > 0.82D;
    }

    private void vanishWithSmoke(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.8D, this.getZ(),
                18, 0.55D, 1.1D, 0.55D, 0.04D);
        this.discard();
    }

    @Nullable
    private ServerPlayer findNearestSurvivalPlayer(ServerLevel level, double maxDist) {
        double best = maxDist * maxDist;
        ServerPlayer found = null;
        for (ServerPlayer p : level.players()) {
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) {
                continue;
            }
            double d = p.distanceToSqr(this);
            if (d < best) {
                best = d;
                found = p;
            }
        }
        return found;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (this.isFrozen()) {
                return state.setAndContinue(ANIM_FROZEN);
            }
            if (this.isChasing()) {
                return state.setAndContinue(ANIM_CHASE);
            }
            if (state.isMoving()) {
                return state.setAndContinue(ANIM_STALK);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
