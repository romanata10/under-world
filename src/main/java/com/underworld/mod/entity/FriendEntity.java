package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class FriendEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_STARE = RawAnimation.begin().thenLoop("misc.stare");
    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("move.walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int relocateCooldown = 200;

    public FriendEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        String displayName = UnderWorldConfigHolder.getFriendDisplayName();
        this.setCustomName(Component.literal(displayName));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 36.0F, 1.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.setPersistenceRequired();
        this.setCustomName(Component.literal(UnderWorldConfigHolder.getFriendDisplayName()));
        this.setCustomNameVisible(true);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % 40 == 0) {
                String expected = UnderWorldConfigHolder.getFriendDisplayName();
                if (!this.hasCustomName() || !this.getCustomName().getString().equals(expected)) {
                    this.setCustomName(Component.literal(expected));
                    this.setCustomNameVisible(true);
                }
            }

            Player nearest = serverLevel.getNearestPlayer(this, 96.0D);
            if (nearest != null) {
                double distSq = this.distanceToSqr(nearest);
                if (distSq < 18.0D * 18.0D) {
                    this.getNavigation().stop();
                    this.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
                } else if (distSq > 48.0D * 48.0D) {
                    if (this.relocateCooldown > 0) {
                        this.relocateCooldown--;
                    } else if (!isPlayerLookingAtMe(nearest)) {
                        tryRelocateNear(serverLevel, nearest);
                        this.relocateCooldown = 300;
                    }
                }
            }
        }
    }

    private boolean isPlayerLookingAtMe(Player player) {
        var look = player.getLookAngle().normalize();
        var toMe = this.position().subtract(player.getEyePosition()).normalize();
        return look.dot(toMe) > 0.65D && player.hasLineOfSight(this);
    }

    private void tryRelocateNear(ServerLevel level, Player player) {
        double behindYaw = Math.toRadians(player.getYRot() + 180.0F + (this.random.nextFloat() - 0.5F) * 80.0F);
        double dist = 16.0D + this.random.nextDouble() * 8.0D;
        int tx = (int) Math.floor(player.getX() - Math.sin(behindYaw) * dist);
        int tz = (int) Math.floor(player.getZ() + Math.cos(behindYaw) * dist);
        int ty = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
        if (Math.abs(ty - player.getY()) <= 24.0D && level.noCollision(this, this.getType().getAABB(tx + 0.5D, ty, tz + 0.5D))) {
            this.teleportTo(tx + 0.5D, ty, tz + 0.5D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Movement", 4, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(ANIM_WALK);
            }
            Player nearest = this.level().getNearestPlayer(this, 28.0D);
            if (nearest != null) {
                return state.setAndContinue(ANIM_STARE);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
