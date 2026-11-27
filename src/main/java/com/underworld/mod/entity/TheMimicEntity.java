package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModBlocks;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TheMimicEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_TRANSFORMED =
            SynchedEntityData.defineId(TheMimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_STOLEN_UUID =
            SynchedEntityData.defineId(TheMimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_STOLEN_NAME =
            SynchedEntityData.defineId(TheMimicEntity.class, EntityDataSerializers.STRING);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_CHASE = RawAnimation.begin().thenLoop("move.chase");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int chaseTicksRemaining = 0;
    private int rearmCooldown = 0;
    private int disguisedWanderTicks = 2400;
    private int attackCooldown = 0;
    @Nullable
    private UUID lastVictimUuid = null;

    public TheMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 35;
        this.setMaxUpStep(1.5F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRANSFORMED, false);
        this.entityData.define(DATA_STOLEN_UUID, Optional.empty());
        this.entityData.define(DATA_STOLEN_NAME, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.72D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 48.0F));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        setTransformed(false);
        this.disguisedWanderTicks = 2400;
        return data;
    }

    public boolean isTransformed() {
        return this.entityData.get(DATA_TRANSFORMED);
    }

    public void setTransformed(boolean transformed) {
        this.entityData.set(DATA_TRANSFORMED, transformed);
        if (transformed) {
            this.setCustomName(null);
            this.setCustomNameVisible(false);
        }
    }

    @Nullable
    public UUID getStolenPlayerUuid() {
        return this.entityData.get(DATA_STOLEN_UUID).orElse(null);
    }

    public String getStolenPlayerName() {
        return this.entityData.get(DATA_STOLEN_NAME);
    }

    public boolean hasStolenSkin() {
        return getStolenPlayerUuid() != null;
    }

    public void setStolenPlayer(@Nullable UUID uuid, String name) {
        this.entityData.set(DATA_STOLEN_UUID, Optional.ofNullable(uuid));
        this.entityData.set(DATA_STOLEN_NAME, name != null ? name : "");
        if (uuid != null && name != null && !name.isEmpty()) {
            this.setCustomName(Component.literal(name));
            this.setCustomNameVisible(true);
        } else {
            this.setCustomName(null);
            this.setCustomNameVisible(false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Transformed", isTransformed());
        UUID stolen = getStolenPlayerUuid();
        if (stolen != null) {
            tag.putUUID("StolenUuid", stolen);
        }
        tag.putString("StolenName", getStolenPlayerName());
        tag.putInt("ChaseTicks", this.chaseTicksRemaining);
        tag.putInt("RearmCooldown", this.rearmCooldown);
        tag.putInt("DisguisedWanderTicks", this.disguisedWanderTicks);
        if (this.lastVictimUuid != null) {
            tag.putUUID("LastVictimUuid", this.lastVictimUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTransformed(tag.getBoolean("Transformed"));
        UUID stolen = tag.hasUUID("StolenUuid") ? tag.getUUID("StolenUuid") : null;
        String stolenName = tag.getString("StolenName");
        setStolenPlayer(stolen, stolenName);
        this.chaseTicksRemaining = tag.getInt("ChaseTicks");
        this.rearmCooldown = tag.getInt("RearmCooldown");
        if (tag.contains("DisguisedWanderTicks")) {
            this.disguisedWanderTicks = tag.getInt("DisguisedWanderTicks");
        }
        this.lastVictimUuid = tag.hasUUID("LastVictimUuid") ? tag.getUUID("LastVictimUuid") : null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && this.level() instanceof ServerLevel sl && !isTransformed()) {
            triggerBurstReveal(sl, source.getEntity() instanceof ServerPlayer sp ? sp : null);
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.rearmCooldown > 0) this.rearmCooldown--;
        if (this.attackCooldown > 0) this.attackCooldown--;

        if (!isTransformed()) {
            tickDisguised(serverLevel);
        } else {
            tickTransformedChase(serverLevel);
        }
    }

    private void tickDisguised(ServerLevel level) {
        this.disguisedWanderTicks--;
        if (this.disguisedWanderTicks <= 0) {
            vanishIntoBlackPixels(level);
            return;
        }

        ServerPlayer triggerPlayer = findNearestTriggerPlayer(level, UnderWorldConfigHolder.getTheMimicTriggerRange());
        if (triggerPlayer != null && this.rearmCooldown <= 0) {
            triggerBurstReveal(level, triggerPlayer);
            return;
        }

        // If carrying a victim's skin (multiplayer horror), actively seek other players while wandering!
        if (hasStolenSkin()) {
            ServerPlayer roamTarget = findNextMultiplayerTarget(level, 56.0D);
            if (roamTarget != null) {
                if (this.tickCount % 12 == 0) {
                    this.getNavigation().moveTo(roamTarget, 0.88D);
                }
                faceTarget(roamTarget);
            }
        } else {
            ServerPlayer watched = findNearestSurvivalPlayer(level, 40.0D);
            if (watched != null) {
                faceTarget(watched);
            }
        }
    }

    private void tickTransformedChase(ServerLevel level) {
        this.chaseTicksRemaining--;
        if (this.chaseTicksRemaining <= 0) {
            vanishIntoBlackPixels(level);
            return;
        }

        ServerPlayer target = findNearestSurvivalPlayer(level, 64.0D);
        if (target == null) {
            if (this.chaseTicksRemaining > 80) {
                this.chaseTicksRemaining = 80;
            }
            return;
        }

        this.setTarget(target);
        faceTarget(target);
        this.getNavigation().moveTo(target, 1.38D);

        if (this.tickCount % 22 == 0) {
            level.playSound(null, this.blockPosition(), ModSounds.MIMIC_CHASE.get(), SoundSource.HOSTILE, 1.7F,
                    0.85F + level.getRandom().nextFloat() * 0.30F);
        }

        // Light trail of broken black glitch particles while chasing
        if (this.tickCount % 3 == 0) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.GLITCH_BLOCK.get().defaultBlockState()),
                    this.getX(), this.getY() + 1.4D, this.getZ(),
                    3, 0.35D, 0.7D, 0.35D, 0.02D
            );
        }

        double distSq = this.distanceToSqr(target);
        if (distSq <= 2.35D * 2.35D && this.attackCooldown <= 0) {
            onCatchPlayer(level, target);
        }
    }

    private void triggerBurstReveal(ServerLevel level, @Nullable ServerPlayer target) {
        setTransformed(true);
        this.chaseTicksRemaining = UnderWorldConfigHolder.getTheMimicChaseDurationTicks();
        this.attackCooldown = 10;
        if (target != null) {
            this.setTarget(target);
        }

        spawnBlackPixelBurst(level);
        level.playSound(null, this.blockPosition(), ModSounds.MIMIC_BURST.get(), SoundSource.HOSTILE, 2.2F, 0.92F);
    }

    private void onCatchPlayer(ServerLevel level, ServerPlayer victim) {
        this.attackCooldown = 25;
        victim.hurt(this.damageSources().mobAttack(this), 12.0F);
        level.playSound(null, victim.blockPosition(), ModSounds.MIMIC_BURST.get(), SoundSource.HOSTILE, 1.9F, 0.72F);

        // Steal victim's skin & name and revert to wandering player-mimic seeking more victims!
        this.lastVictimUuid = victim.getUUID();
        setStolenPlayer(victim.getUUID(), victim.getGameProfile().getName());
        setTransformed(false);
        this.setTarget(null);
        this.getNavigation().stop();
        this.rearmCooldown = 140; // 7s grace before re-triggering
        this.disguisedWanderTicks = 2400; // 2 mins wandering to hunt others
        spawnBlackPixelBurst(level);
    }

    private void spawnBlackPixelBurst(ServerLevel level) {
        BlockState glitchState = ModBlocks.GLITCH_BLOCK.get().defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, glitchState),
                this.getX(), this.getY() + 1.2D, this.getZ(), 42, 0.55D, 0.95D, 0.55D, 0.14D);
        level.sendParticles(new DustParticleOptions(new Vector3f(0.02F, 0.02F, 0.02F), 2.4F),
                this.getX(), this.getY() + 1.2D, this.getZ(), 36, 0.50D, 0.90D, 0.50D, 0.05D);
        level.sendParticles(ParticleTypes.SQUID_INK,
                this.getX(), this.getY() + 1.1D, this.getZ(), 24, 0.45D, 0.8D, 0.45D, 0.10D);
    }

    private void vanishIntoBlackPixels(ServerLevel level) {
        spawnBlackPixelBurst(level);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.2D, this.getZ(),
                18, 0.45D, 0.85D, 0.45D, 0.04D);
        this.discard();
    }

    private void faceTarget(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Nullable
    private ServerPlayer findNearestTriggerPlayer(ServerLevel level, double radius) {
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

    @Nullable
    private ServerPlayer findNearestSurvivalPlayer(ServerLevel level, double radius) {
        return findNearestTriggerPlayer(level, radius);
    }

    @Nullable
    private ServerPlayer findNextMultiplayerTarget(ServerLevel level, double radius) {
        double best = radius * radius;
        List<ServerPlayer> others = new ArrayList<>();
        ServerPlayer fallback = null;
        for (ServerPlayer p : level.players()) {
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            if (p.distanceToSqr(this) <= best) {
                if (this.lastVictimUuid != null && p.getUUID().equals(this.lastVictimUuid)) {
                    fallback = p;
                } else {
                    others.add(p);
                }
            }
        }
        if (!others.isEmpty()) {
            ServerPlayer closest = null;
            double minSq = Double.MAX_VALUE;
            for (ServerPlayer p : others) {
                double d = p.distanceToSqr(this);
                if (d < minSq) {
                    minSq = d;
                    closest = p;
                }
            }
            return closest;
        }
        return fallback;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (this.isTransformed() && (state.isMoving() || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D)) {
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
