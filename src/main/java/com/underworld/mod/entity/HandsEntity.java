package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.event.UnderWorldEventDirector;
import com.underworld.mod.registry.ModSounds;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class HandsEntity extends Monster implements GeoEntity {
    private static final int STATE_EMERGE = 0;
    private static final int STATE_CHASE = 1;
    private static final int STATE_BURROW = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(HandsEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHASE_TICKS =
            SynchedEntityData.defineId(HandsEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_CHASE = RawAnimation.begin().thenLoop("move.chase");
    private static final RawAnimation ANIM_BURROW = RawAnimation.begin().thenPlayAndHold("misc.burrow");
    private static final RawAnimation ANIM_SWIPE = RawAnimation.begin().thenPlay("attack.swipe");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int emergeTicks = 12;
    private int burrowTicks = 30;
    private boolean playedIntroSound = false;

    public HandsEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_EMERGE);
        this.entityData.define(DATA_CHASE_TICKS, 1200);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        int duration = UnderWorldConfigHolder.get() != null
                ? UnderWorldConfigHolder.get().hands.handsChaseDurationTicks.get()
                : 1200;
        this.setChaseTicks(duration);
        this.setState(STATE_EMERGE);
        return data;
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public int getChaseTicks() {
        return this.entityData.get(DATA_CHASE_TICKS);
    }

    public void setChaseTicks(int ticks) {
        this.entityData.set(DATA_CHASE_TICKS, ticks);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HandsState", this.getState());
        tag.putInt("ChaseTicks", this.getChaseTicks());
        tag.putInt("EmergeTicks", this.emergeTicks);
        tag.putInt("BurrowTicks", this.burrowTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HandsState")) this.setState(tag.getInt("HandsState"));
        if (tag.contains("ChaseTicks")) this.setChaseTicks(tag.getInt("ChaseTicks"));
        if (tag.contains("EmergeTicks")) this.emergeTicks = tag.getInt("EmergeTicks");
        if (tag.contains("BurrowTicks")) this.burrowTicks = tag.getInt("BurrowTicks");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.getState() == STATE_BURROW) {
                BlockPos below = this.blockPosition().below();
                BlockState state = this.level().getBlockState(below);
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        this.getY() + 0.1D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        0.0D, 0.15D, 0.0D);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (!this.playedIntroSound) {
            this.playedIntroSound = true;
            serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_SCREAM.get(), SoundSource.HOSTILE, 2.2F, 0.95F);
            serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_GLITCH.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
        }

        int state = this.getState();
        if (state == STATE_EMERGE) {
            this.getNavigation().stop();
            if (--this.emergeTicks <= 0) {
                this.setState(STATE_CHASE);
            }
            return;
        }

        if (state == STATE_CHASE) {
            int remaining = this.getChaseTicks() - 1;
            this.setChaseTicks(remaining);

            if (this.getTarget() == null || !this.getTarget().isAlive()) {
                Player nearest = serverLevel.getNearestPlayer(this, 64.0D);
                if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                    this.setTarget(nearest);
                }
            }

            if (this.tickCount % 28 == 0) {
                serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_GLITCH.get(), SoundSource.HOSTILE,
                        1.7F, 0.85F + this.random.nextFloat() * 0.35F);
            }
            if (this.tickCount % 85 == 0) {
                serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_SCREAM.get(), SoundSource.HOSTILE,
                        2.0F, 0.9F + this.random.nextFloat() * 0.25F);
            }

            boolean breakEnabled = UnderWorldConfigHolder.get() == null || UnderWorldConfigHolder.get().hands.handsBreakBlocks.get();
            if (breakEnabled && this.tickCount % 5 == 0 && ForgeEventFactory.getMobGriefingEvent(serverLevel, this)) {
                breakSurroundingBlocks(serverLevel);
            }

            if (remaining <= 0) {
                startBurrowing(serverLevel);
            }
            return;
        }

        if (state == STATE_BURROW) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, -0.05D, 0.0D);
            if (--this.burrowTicks <= 0) {
                this.discard();
            }
        }
    }

    private void startBurrowing(ServerLevel serverLevel) {
        this.setState(STATE_BURROW);
        this.burrowTicks = 30;
        this.getNavigation().stop();
        this.setTarget(null);
        serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_BURROW.get(), SoundSource.HOSTILE, 1.8F, 1.0F);
    }

    private void breakSurroundingBlocks(ServerLevel level) {
        BlockPos origin = this.blockPosition();
        var look = this.getLookAngle();
        int behindX = (int) Math.round(-look.x);
        int behindZ = (int) Math.round(-look.z);

        for (int dy = 0; dy <= 2; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && dy == 0) continue;
                    if (this.random.nextFloat() < 0.45F) {
                        tryDestroy(level, origin.offset(dx, dy, dz));
                    }
                }
            }
            if (behindX != 0 || behindZ != 0) {
                tryDestroy(level, origin.offset(behindX, dy, behindZ));
                tryDestroy(level, origin.offset(behindX * 2, dy, behindZ * 2));
            }
        }
    }

    private void tryDestroy(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness >= 45.0F || level.getBlockEntity(pos) != null) {
            return;
        }
        level.destroyBlock(pos, true, this);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("Actions", "attack");
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_SCREAM.get(), SoundSource.HOSTILE, 2.0F, 1.05F);
            serverLevel.playSound(null, this.blockPosition(), ModSounds.HANDS_GLITCH.get(), SoundSource.HOSTILE, 1.8F, 1.15F);
        }

        DamageSource source = this.damageSources().mobAttack(this);
        boolean hit = target.hurt(source, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hit) {
            this.doEnchantDamageEffects(this, target);
            if (!this.level().isClientSide && target instanceof ServerPlayer killedPlayer && killedPlayer.isDeadOrDying()) {
                boolean kickEnabled = UnderWorldConfigHolder.get() == null || UnderWorldConfigHolder.get().hands.handsKickOnKill.get();
                if (kickEnabled) {
                    UnderWorldEventDirector.schedulePlayerKick(
                            killedPlayer,
                            6,
                            Component.translatable("disconnect.under_world.hands")
                    );
                }
                startBurrowing((ServerLevel) this.level());
            }
        }
        return hit;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 3, state -> {
            int entityState = this.getState();
            if (entityState == STATE_BURROW) {
                return state.setAndContinue(ANIM_BURROW);
            }
            if (entityState == STATE_CHASE || state.isMoving()) {
                return state.setAndContinue(ANIM_CHASE);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
        controllers.add(DefaultAnimations.triggerOnlyController(this)
                .triggerableAnim("attack", ANIM_SWIPE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
