package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.network.NetworkHandler;
import com.underworld.mod.registry.ModSounds;
import com.underworld.mod.world.UnderWorldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class TheTruthEntity extends Monster implements GeoEntity {
    public static final int STATE_EMERGE = 0;
    public static final int STATE_CHASE = 1;
    public static final int STATE_VANISH = 2;

    private static final int EMERGE_DURATION_TICKS = 26;
    private static final int VANISH_DURATION_TICKS = 20;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheTruthEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_EMERGE_PROGRESS =
            SynchedEntityData.defineId(TheTruthEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_TICKS =
            SynchedEntityData.defineId(TheTruthEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TOTAL_TICKS =
            SynchedEntityData.defineId(TheTruthEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIM_EMERGE = RawAnimation.begin().thenLoop("misc.emerge");
    private static final RawAnimation ANIM_CHASE = RawAnimation.begin().thenLoop("move.chase");
    private static final RawAnimation ANIM_VANISH = RawAnimation.begin().thenPlayAndHold("misc.vanish");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int emergeTicks = EMERGE_DURATION_TICKS;
    private int vanishTicks = VANISH_DURATION_TICKS;
    private int noTargetTicks = 0;
    private boolean initialized = false;
    private boolean killHandled = false;
    @Nullable
    private UUID chasedPlayerUuid = null;

    public TheTruthEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 999.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.255D)
                .add(Attributes.ATTACK_DAMAGE, 100.0D)
                .add(Attributes.FOLLOW_RANGE, 160.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_EMERGE);
        this.entityData.define(DATA_EMERGE_PROGRESS, 0.0F);
        this.entityData.define(DATA_REMAINING_TICKS, 600);
        this.entityData.define(DATA_TOTAL_TICKS, 600);
    }

    @Override
    protected void registerGoals() {
        // Movement, rotation, block clearing, and catch mechanics are driven deterministically in tick().
    }

    public int getTruthState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setTruthState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public float getEmergeProgress() {
        return this.entityData.get(DATA_EMERGE_PROGRESS);
    }

    public void setEmergeProgress(float progress) {
        this.entityData.set(DATA_EMERGE_PROGRESS, Mth.clamp(progress, 0.0F, 1.0F));
    }

    public int getRemainingTicks() {
        return this.entityData.get(DATA_REMAINING_TICKS);
    }

    public void setRemainingTicks(int ticks) {
        this.entityData.set(DATA_REMAINING_TICKS, Math.max(0, ticks));
    }

    public int getTotalTicks() {
        return this.entityData.get(DATA_TOTAL_TICKS);
    }

    public void setTotalTicks(int ticks) {
        this.entityData.set(DATA_TOTAL_TICKS, Math.max(20, ticks));
    }

    public void setChasedPlayer(@Nullable ServerPlayer player) {
        this.chasedPlayerUuid = player != null ? player.getUUID() : null;
        this.setTarget(player);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        initChaseSettings();
        return data;
    }

    private void initChaseSettings() {
        int duration = UnderWorldConfigHolder.get() != null
                ? UnderWorldConfigHolder.get().theTruth.theTruthChaseDurationTicks.get()
                : 600;
        this.setTotalTicks(duration);
        this.setRemainingTicks(duration);
        this.setTruthState(STATE_EMERGE);
        this.setEmergeProgress(0.0F);
        this.emergeTicks = EMERGE_DURATION_TICKS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TruthState", this.getTruthState());
        tag.putFloat("EmergeProgress", this.getEmergeProgress());
        tag.putInt("RemainingTicks", this.getRemainingTicks());
        tag.putInt("TotalTicks", this.getTotalTicks());
        tag.putInt("EmergeTicks", this.emergeTicks);
        tag.putInt("VanishTicks", this.vanishTicks);
        tag.putBoolean("TruthInitialized", this.initialized);
        if (this.chasedPlayerUuid != null) {
            tag.putUUID("ChasedPlayer", this.chasedPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TruthState")) this.setTruthState(tag.getInt("TruthState"));
        if (tag.contains("EmergeProgress")) this.setEmergeProgress(tag.getFloat("EmergeProgress"));
        if (tag.contains("RemainingTicks")) this.setRemainingTicks(tag.getInt("RemainingTicks"));
        if (tag.contains("TotalTicks")) this.setTotalTicks(tag.getInt("TotalTicks"));
        if (tag.contains("EmergeTicks")) this.emergeTicks = tag.getInt("EmergeTicks");
        if (tag.contains("VanishTicks")) this.vanishTicks = tag.getInt("VanishTicks");
        if (tag.contains("TruthInitialized")) this.initialized = tag.getBoolean("TruthInitialized");
        if (tag.hasUUID("ChasedPlayer")) {
            this.chasedPlayerUuid = tag.getUUID("ChasedPlayer");
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(24.0D, 18.0D, 24.0D);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 384.0D * 384.0D;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            List<? extends TheTruthEntity> others = serverLevel.getEntities(
                    com.underworld.mod.registry.ModEntities.THE_TRUTH.get(),
                    e -> e != this && e.isAlive() && e.getTruthState() != STATE_VANISH
            );
            if (others.isEmpty()) {
                NetworkHandler.syncTruthHudToDimension(serverLevel, false, 0, this.getTotalTicks());
            }
        }
        super.remove(reason);
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (!this.initialized) {
            this.initialized = true;
            if (this.getTotalTicks() <= 0) {
                initChaseSettings();
            }
            serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_TRUTH_EMERGE.get(), SoundSource.HOSTILE, 6.0F, 0.85F);
            serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_TRUTH_ROAR.get(), SoundSource.HOSTILE, 6.0F, 0.9F);
            NetworkHandler.syncTruthHudToDimension(serverLevel, true, this.getRemainingTicks(), this.getTotalTicks());
        }

        int state = this.getTruthState();
        if (state == STATE_EMERGE) {
            this.emergeTicks--;
            float progress = 1.0F - ((float) Math.max(0, this.emergeTicks) / (float) EMERGE_DURATION_TICKS);
            this.setEmergeProgress(progress);

            ServerPlayer target = resolveTarget(serverLevel);
            if (target != null) {
                faceTarget(target);
            }

            if (this.emergeTicks % 3 == 0) {
                spawnServerEruptionParticles(serverLevel);
            }

            if (this.emergeTicks <= 0) {
                this.setEmergeProgress(1.0F);
                this.setTruthState(STATE_CHASE);
                serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_TRUTH_ROAR.get(), SoundSource.HOSTILE, 6.0F, 0.85F);
                NetworkHandler.syncTruthHudToDimension(serverLevel, true, this.getRemainingTicks(), this.getTotalTicks());
            }
            return;
        }

        if (state == STATE_CHASE) {
            int remaining = this.getRemainingTicks() - 1;
            this.setRemainingTicks(remaining);

            if (remaining % 10 == 0) {
                NetworkHandler.syncTruthHudToDimension(serverLevel, true, Math.max(0, remaining), this.getTotalTicks());
            }

            if (remaining <= 0) {
                beginVanish(serverLevel);
                return;
            }

            if (this.tickCount % 95 == 0) {
                serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_TRUTH_ROAR.get(), SoundSource.HOSTILE, 6.0F, 0.82F + this.random.nextFloat() * 0.16F);
            }
            if (this.tickCount % 28 == 0) {
                serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_TRUTH_AMBIENT.get(), SoundSource.HOSTILE, 4.0F, 0.95F);
            }

            ServerPlayer target = resolveTarget(serverLevel);
            if (target == null) {
                if (serverLevel.players().isEmpty()) {
                    this.noTargetTicks++;
                    if (this.noTargetTicks > 40) {
                        beginVanish(serverLevel);
                    }
                } else {
                    this.noTargetTicks = 0;
                    ServerPlayer observer = serverLevel.getPlayers(p -> p.isAlive()).stream().findFirst().orElse(null);
                    if (observer != null) {
                        faceTarget(observer);
                    }
                }
                return;
            }
            this.noTargetTicks = 0;

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double horizDist = Math.sqrt(dx * dx + dz * dz);
            faceTarget(target);

            // Catch & Kill check
            if (!this.killHandled && horizDist <= 7.5D
                    && target.getY() >= this.getY() - 3.5D
                    && target.getY() <= this.getY() + 50.0D) {
                catchAndKillPlayer(serverLevel, target);
                return;
            }

            // Relentless kinematic pursuit toward target
            double dirX = horizDist > 0.001D ? dx / horizDist : 0.0D;
            double dirZ = horizDist > 0.001D ? dz / horizDist : 1.0D;
            double speed = UnderWorldConfigHolder.get() != null
                    ? UnderWorldConfigHolder.get().theTruth.theTruthChaseSpeed.get()
                    : 0.255D;

            double nextX = this.getX() + dirX * speed;
            double nextZ = this.getZ() + dirZ * speed;
            double nextY = computeChaseGroundY(serverLevel, nextX, nextZ, target.getY());

            this.setPos(nextX, nextY, nextZ);

            // Carve a 30-block wide x 50-block tall path through blocks ahead
            boolean breakEnabled = UnderWorldConfigHolder.get() == null
                    || UnderWorldConfigHolder.get().theTruth.theTruthBreakBlocks.get();
            if (breakEnabled && this.tickCount % 2 == 0 && ForgeEventFactory.getMobGriefingEvent(serverLevel, this)) {
                breakPathBlocks(serverLevel, dirX, dirZ);
            }
            return;
        }

        if (state == STATE_VANISH) {
            this.vanishTicks--;
            if (this.vanishTicks <= 0) {
                this.discard();
            }
        }
    }

    @Nullable
    private ServerPlayer resolveTarget(ServerLevel level) {
        if (this.chasedPlayerUuid != null) {
            ServerPlayer locked = level.getServer().getPlayerList().getPlayer(this.chasedPlayerUuid);
            if (locked != null && locked.level() == level && locked.isAlive()
                    && !locked.isCreative() && !locked.isSpectator()) {
                return locked;
            }
        }
        ServerPlayer nearest = null;
        double bestDistSq = 180.0D * 180.0D;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
                continue;
            }
            double dSq = player.distanceToSqr(this);
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                nearest = player;
            }
        }
        if (nearest != null) {
            this.chasedPlayerUuid = nearest.getUUID();
            this.setTarget(nearest);
        }
        return nearest;
    }

    private void faceTarget(ServerPlayer target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(targetYaw);
        this.yHeadRot = targetYaw;
        this.yBodyRot = targetYaw;
    }

    private double computeChaseGroundY(ServerLevel level, double nx, double nz, double targetY) {
        double currentY = this.getY();
        if (targetY < currentY - 3.5D) {
            return Math.max(level.getMinBuildHeight() + 2, currentY - 0.35D);
        }

        int bx = Mth.floor(nx);
        int bz = Mth.floor(nz);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        double desiredY;
        if (Math.abs(surfaceY - targetY) <= 18.0D) {
            desiredY = surfaceY;
        } else {
            desiredY = targetY;
        }
        double dy = Mth.clamp(desiredY - currentY, -0.45D, 0.45D);
        return Mth.clamp(currentY + dy, level.getMinBuildHeight() + 2, level.getMaxBuildHeight() - 52);
    }

    private void breakPathBlocks(ServerLevel level, double dirX, double dirZ) {
        double perpX = -dirZ;
        double perpZ = dirX;
        int baseY = Mth.floor(this.getY());
        int maxY = Math.min(level.getMaxBuildHeight() - 2, baseY + 49);
        int particleBudget = 12;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        // Forward swath (30 blocks wide: lat -14..+14, fwd 0..5, height 50 blocks: baseY..baseY+49)
        for (int lat = -14; lat <= 14; lat += 1) {
            for (int fwd = 0; fwd <= 5; fwd += 2) {
                int wx = Mth.floor(this.getX() + dirX * fwd + perpX * lat);
                int wz = Mth.floor(this.getZ() + dirZ * fwd + perpZ * lat);
                if (!level.isLoaded(mutable.set(wx, baseY, wz))) {
                    continue;
                }
                for (int wy = baseY; wy <= maxY; wy++) {
                    mutable.set(wx, wy, wz);
                    BlockState state = level.getBlockState(mutable);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        continue;
                    }
                    float hardness = state.getDestroySpeed(level, mutable);
                    if (hardness < 0.0F) {
                        continue;
                    }
                    if (particleBudget > 0 && wy <= baseY + 4 && this.random.nextFloat() < 0.25F) {
                        level.destroyBlock(mutable, false, this);
                        particleBudget--;
                    } else {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private void catchAndKillPlayer(ServerLevel level, ServerPlayer victim) {
        this.killHandled = true;
        NetworkHandler.syncTruthHudToDimension(level, false, 0, this.getTotalTicks());
        level.playSound(null, victim.blockPosition(), ModSounds.THE_TRUTH_KILL.get(), SoundSource.HOSTILE, 5.0F, 0.95F);

        boolean pillarEnabled = UnderWorldConfigHolder.get() == null
                || UnderWorldConfigHolder.get().theTruth.theTruthSpawnPillarOnRejoin.get();
        if (pillarEnabled) {
            UnderWorldSavedData.get(level).markTruthPillarPending(victim.getUUID());
        }

        boolean kickEnabled = UnderWorldConfigHolder.get() == null
                || UnderWorldConfigHolder.get().theTruth.theTruthKickOnKill.get();

        victim.hurt(this.damageSources().mobAttack(this), 99999.0F);
        if (kickEnabled) {
            UnderWorldEventDirector.schedulePlayerKick(
                    victim,
                    5,
                    Component.translatable("disconnect.under_world.the_truth")
            );
        }
        this.discard();
    }

    private void beginVanish(ServerLevel level) {
        this.setTruthState(STATE_VANISH);
        this.vanishTicks = VANISH_DURATION_TICKS;
        this.setTarget(null);
        NetworkHandler.syncTruthHudToDimension(level, false, 0, this.getTotalTicks());
        level.playSound(null, this.blockPosition(), ModSounds.FRIEND_VANISH.get(), SoundSource.HOSTILE, 4.0F, 0.7F);
    }

    private void spawnServerEruptionParticles(ServerLevel level) {
        BlockPos ground = BlockPos.containing(this.getX(), this.getY() - 1.0D, this.getZ());
        BlockState state = level.getBlockState(ground);
        if (state.isAir()) {
            state = Blocks.DIRT.defaultBlockState();
        }
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                this.getX(), this.getY() + 0.6D, this.getZ(),
                40, 10.0D, 1.0D, 10.0D, 0.25D
        );
        level.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY() + 1.5D, this.getZ(),
                24, 9.0D, 2.0D, 9.0D, 0.08D
        );
    }

    private void spawnClientParticles() {
        int state = this.getTruthState();
        if (state == STATE_EMERGE || state == STATE_CHASE) {
            for (int i = 0; i < (state == STATE_EMERGE ? 6 : 3); i++) {
                this.level().addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 24.0D,
                        this.getY() + this.random.nextDouble() * 42.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 24.0D,
                        0.0D, 0.05D, 0.0D
                );
            }
        }
        if (state == STATE_VANISH) {
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(
                        ParticleTypes.SQUID_INK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 22.0D,
                        this.getY() + this.random.nextDouble() * 44.0D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 22.0D,
                        0.0D, 0.08D, 0.0D
                );
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 3, state -> {
            int s = this.getTruthState();
            if (s == STATE_EMERGE) {
                return state.setAndContinue(ANIM_EMERGE);
            }
            if (s == STATE_VANISH) {
                return state.setAndContinue(ANIM_VANISH);
            }
            return state.setAndContinue(ANIM_CHASE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
