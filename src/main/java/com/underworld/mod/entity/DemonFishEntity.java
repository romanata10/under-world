package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class DemonFishEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_TONGUING =
            SynchedEntityData.defineId(DemonFishEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BITING =
            SynchedEntityData.defineId(DemonFishEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_SWIM = RawAnimation.begin().thenLoop("move.swim");
    private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenLoop("attack.bite");
    private static final RawAnimation ANIM_TONGUE = RawAnimation.begin().thenLoop("attack.tongue");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int huntTicksRemaining = 1200;
    private int targetDryTicks = 0;
    private int attackCooldown = 0;
    private int biteAnimTicks = 0;
    private int tongueGrabTicks = 0;
    private int blockBreakCooldown = 0;

    public DemonFishEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 72.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TONGUING, false);
        this.entityData.define(DATA_BITING, false);
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

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.huntTicksRemaining = UnderWorldConfigHolder.getDemonFishChaseDurationTicks();
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HuntTicks", this.huntTicksRemaining);
        tag.putInt("DryTicks", this.targetDryTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HuntTicks")) {
            this.huntTicksRemaining = tag.getInt("HuntTicks");
        }
        this.targetDryTicks = tag.getInt("DryTicks");
    }

    public boolean isTonguing() {
        return this.entityData.get(DATA_TONGUING);
    }

    public void setTonguing(boolean tonguing) {
        this.entityData.set(DATA_TONGUING, tonguing);
    }

    public boolean isBiting() {
        return this.entityData.get(DATA_BITING);
    }

    public void setBiting(boolean biting) {
        this.entityData.set(DATA_BITING, biting);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel level)) {
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.blockBreakCooldown > 0) this.blockBreakCooldown--;
        if (this.biteAnimTicks > 0) {
            this.biteAnimTicks--;
            if (this.biteAnimTicks == 0) setBiting(false);
        }

        this.huntTicksRemaining--;
        if (this.huntTicksRemaining <= 0) {
            vanishInWater(level);
            return;
        }

        ServerPlayer target = findNearestSurvivalPlayer(level, 72.0D);
        if (target == null) {
            setTonguing(false);
            this.targetDryTicks++;
            if (this.targetDryTicks > 60) {
                vanishInWater(level);
            }
            return;
        }

        this.setTarget(target);

        boolean playerInBoat = target.getVehicle() instanceof Boat;
        boolean playerInWater = target.isInWaterOrBubble() || level.getFluidState(target.blockPosition()).is(FluidTags.WATER)
                || level.getFluidState(target.blockPosition().below()).is(FluidTags.WATER);

        // Vanish if the player successfully climbs out onto dry land
        if (!playerInWater && !playerInBoat && target.onGround()) {
            this.targetDryTicks++;
            if (this.targetDryTicks >= 35) {
                vanishInWater(level);
                return;
            }
        } else {
            this.targetDryTicks = 0;
        }

        // Handle boat tongue-grab & boat shattering
        if (playerInBoat && target.getVehicle() instanceof Boat boat && boat.isAlive()) {
            double boatDistSq = this.distanceToSqr(boat);
            if (boatDistSq <= 10.5D * 10.5D) {
                setTonguing(true);
                this.tongueGrabTicks++;
                spawnTongueTrailParticles(level, boat);

                if (this.tongueGrabTicks == 1) {
                    level.playSound(null, this.blockPosition(), ModSounds.DEMON_FISH_TONGUE.get(), SoundSource.HOSTILE, 1.8F, 0.95F);
                }

                // Yank boat toward Demon Fish
                Vec3 pull = this.position().subtract(boat.position());
                if (pull.lengthSqr() > 0.25D) {
                    Vec3 pNorm = pull.normalize();
                    boat.setDeltaMovement(pNorm.x * 0.24D, -0.10D, pNorm.z * 0.24D);
                }

                if (this.tongueGrabTicks >= 14) {
                    this.tongueGrabTicks = 0;
                    setTonguing(false);
                    level.playSound(null, boat.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 2.0F, 0.72F);
                    level.sendParticles(ParticleTypes.SPLASH, boat.getX(), boat.getY() + 0.4D, boat.getZ(), 28, 0.7D, 0.4D, 0.7D, 0.12D);
                    boat.spawnAtLocation(boat.getDropItem());
                    boat.discard();
                    level.playSound(null, this.blockPosition(), ModSounds.DEMON_FISH_AMBUSH.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
                }
            } else {
                setTonguing(false);
                this.tongueGrabTicks = 0;
            }
        } else {
            setTonguing(false);
            this.tongueGrabTicks = 0;
        }

        // Smooth aquatic swimming steering toward player
        steerThroughWaterToward(level, target);

        // Break blocks if the player tries to dig underwater/wall themselves off below surface!
        if (UnderWorldConfigHolder.isDemonFishBreakBlocksEnabled() && this.blockBreakCooldown <= 0
                && (playerInWater || isSubmergedNearWater(level, target.blockPosition()))) {
            if (breakUnderwaterObstructions(level, target)) {
                this.blockBreakCooldown = 6;
            }
        }

        // Ambient bubble/ambush cues
        if (this.tickCount % 30 == 0) {
            level.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY() + 0.6D, this.getZ(), 6, 0.4D, 0.3D, 0.4D, 0.05D);
        }
        if (this.tickCount % 65 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.DEMON_FISH_AMBUSH.get(), SoundSource.HOSTILE, 1.5F, 0.90F);
        }

        // Melee bite when catching target in water
        if (this.distanceToSqr(target) <= 2.45D * 2.45D && this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            this.biteAnimTicks = 12;
            setBiting(true);
            target.hurt(this.damageSources().mobAttack(this), 10.0F);
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
            level.playSound(null, target.blockPosition(), ModSounds.DEMON_FISH_AMBUSH.get(), SoundSource.HOSTILE, 2.0F, 1.15F);
            // Reset 1-minute hunt grace on landing a catch bite
            this.huntTicksRemaining = UnderWorldConfigHolder.getDemonFishChaseDurationTicks();
        }
    }

    private void steerThroughWaterToward(ServerLevel level, ServerPlayer target) {
        Vec3 targetAim = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
        Vec3 delta = targetAim.subtract(this.position());
        double dist = delta.length();
        if (dist < 0.05D) return;

        Vec3 dir = delta.scale(1.0D / dist);
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;

        boolean selfInWater = this.isInWaterOrBubble() || level.getFluidState(this.blockPosition()).is(FluidTags.WATER);
        if (selfInWater) {
            double speed = 0.37D;
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.65D).add(dir.scale(speed * 0.42D)));
        } else {
            this.setNoGravity(false);
            // Flop/lunge toward water/target
            Vec3 cur = this.getDeltaMovement();
            this.setDeltaMovement(dir.x * 0.24D, cur.y, dir.z * 0.24D);
        }
    }

    private boolean isSubmergedNearWater(ServerLevel level, BlockPos pos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 3; dy++) {
                    if (level.getFluidState(pos.offset(dx, dy, dz)).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean breakUnderwaterObstructions(ServerLevel level, ServerPlayer target) {
        Vec3 dir = target.position().subtract(this.position());
        if (dir.lengthSqr() < 0.01D) return false;
        Vec3 norm = dir.normalize();

        BlockPos ahead = BlockPos.containing(this.getX() + norm.x * 1.8D, this.getY() + 0.5D + norm.y * 1.4D, this.getZ() + norm.z * 1.8D);
        boolean brokeAny = false;

        // Break blocks around head/front toward dug pocket
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = ahead.offset(dx, dy, dz);
                    if (tryFloodBreak(level, p)) {
                        brokeAny = true;
                    }
                }
            }
        }
        // Also break directly around player if they sealed themselves into an underwater hole within 8 blocks
        if (this.distanceToSqr(target) <= 8.5D * 8.5D) {
            BlockPos tp = target.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= 2; dy++) {
                        if (dx == 0 && dz == 0 && dy < 2) continue;
                        BlockPos shell = tp.offset(dx, dy, dz);
                        if (!level.getBlockState(shell).isAir() && !level.getFluidState(shell).is(FluidTags.WATER)) {
                            if (tryFloodBreak(level, shell)) {
                                brokeAny = true;
                            }
                        }
                    }
                }
            }
        }
        if (brokeAny) {
            level.playSound(null, ahead, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.4F, 0.75F);
        }
        return brokeAny;
    }

    private static boolean tryFloodBreak(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        float speed = state.getDestroySpeed(level, pos);
        if (speed < 0.0F || speed > 40.0F || state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
            return false;
        }
        boolean hasAdjacentWater = hasNeighborWater(level, pos);
        level.destroyBlock(pos, false);
        if (hasAdjacentWater) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
        return true;
    }

    private static boolean hasNeighborWater(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos.above()).is(FluidTags.WATER)
                || level.getFluidState(pos.north()).is(FluidTags.WATER)
                || level.getFluidState(pos.south()).is(FluidTags.WATER)
                || level.getFluidState(pos.east()).is(FluidTags.WATER)
                || level.getFluidState(pos.west()).is(FluidTags.WATER);
    }

    private void spawnTongueTrailParticles(ServerLevel level, Boat boat) {
        Vec3 mouth = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);
        Vec3 boatPos = boat.position().add(0.0D, 0.35D, 0.0D);
        for (int i = 1; i <= 8; i++) {
            double t = i / 9.0D;
            Vec3 pt = mouth.lerp(boatPos, t);
            level.sendParticles(ParticleTypes.SQUID_INK, pt.x, pt.y, pt.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
            if (i % 2 == 0) {
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, pt.x, pt.y, pt.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
            }
        }
    }

    private void vanishInWater(ServerLevel level) {
        level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.6D, this.getZ(),
                26, 0.7D, 0.5D, 0.7D, 0.06D);
        level.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY() + 0.6D, this.getZ(),
                24, 0.7D, 0.5D, 0.7D, 0.12D);
        this.discard();
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
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (this.isTonguing()) {
                return state.setAndContinue(ANIM_TONGUE);
            }
            if (this.isBiting()) {
                return state.setAndContinue(ANIM_BITE);
            }
            if (state.isMoving() || this.getDeltaMovement().lengthSqr() > 0.002D) {
                return state.setAndContinue(ANIM_SWIM);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
