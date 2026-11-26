package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class TheSkyEntity extends PathfinderMob implements GeoEntity {
    private static final int STATE_STANDING = 0;
    private static final int STATE_STOMPING = 1;
    private static final int STATE_VANISHING = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheSkyEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation ANIM_STOMP = RawAnimation.begin().thenPlay("move.stomp");
    private static final RawAnimation ANIM_VANISH = RawAnimation.begin().thenPlayAndHold("misc.vanish");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int stateTicks = 0;
    private int lifetimeTicks = 0;
    private boolean roared = false;
    private boolean stompHitApplied = false;

    public TheSkyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_STANDING);
    }

    public int getSkyState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setSkyState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkyState", this.getSkyState());
        tag.putInt("StateTicks", this.stateTicks);
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
        tag.putBoolean("Roared", this.roared);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSkyState(tag.getInt("SkyState"));
        this.stateTicks = tag.getInt("StateTicks");
        this.lifetimeTicks = tag.getInt("LifetimeTicks");
        this.roared = tag.getBoolean("Roared");
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(32.0D, 24.0D, 32.0D);
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
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            if (this.getSkyState() == STATE_VANISHING) {
                for (int i = 0; i < 6; i++) {
                    this.level().addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 20.0D,
                            this.getY() + this.random.nextDouble() * 80.0D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 20.0D,
                            0.0D, 0.08D, 0.0D
                    );
                }
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (!this.roared) {
            this.roared = true;
            serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_SKY_ROAR.get(), SoundSource.AMBIENT, 8.0F, 0.85F);
        }

        int state = this.getSkyState();
        if (state == STATE_STANDING) {
            this.lifetimeTicks++;
            Player nearest = serverLevel.getNearestPlayer(this, 160.0D);
            if (nearest != null) {
                double dx = nearest.getX() - this.getX();
                double dz = nearest.getZ() - this.getZ();
                float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                this.setYRot(targetYaw);
                this.yHeadRot = targetYaw;
                this.yBodyRot = targetYaw;

                double vanishRange = UnderWorldConfigHolder.get() != null
                        ? UnderWorldConfigHolder.get().theSky.theSkyVanishRange.get()
                        : 24.0D;
                double horizDistSq = dx * dx + dz * dz;
                if (!nearest.isCreative() && !nearest.isSpectator() && horizDistSq <= vanishRange * vanishRange) {
                    double stompChance = UnderWorldConfigHolder.get() != null
                            ? UnderWorldConfigHolder.get().theSky.theSkyStompChance.get()
                            : 0.25D;
                    if (this.random.nextDouble() < stompChance) {
                        this.setSkyState(STATE_STOMPING);
                        this.stateTicks = 24;
                        this.stompHitApplied = false;
                        serverLevel.playSound(null, this.blockPosition(), ModSounds.THE_SKY_STOMP.get(), SoundSource.HOSTILE, 6.0F, 0.8F);
                    } else {
                        beginVanish(serverLevel);
                    }
                    return;
                }
            }
            if (this.lifetimeTicks > 3600) {
                beginVanish(serverLevel);
            }
            return;
        }

        if (state == STATE_STOMPING) {
            this.stateTicks--;
            if (!this.stompHitApplied && this.stateTicks <= 12) {
                this.stompHitApplied = true;
                applyStompImpact(serverLevel);
            }
            if (this.stateTicks <= 0) {
                beginVanish(serverLevel);
            }
            return;
        }

        if (state == STATE_VANISHING) {
            this.stateTicks--;
            if (this.stateTicks <= 0) {
                this.discard();
            }
        }
    }

    private void applyStompImpact(ServerLevel level) {
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.0D, this.getZ(), 5, 6.0D, 0.5D, 6.0D, 0.1D);
        List<ServerPlayer> victims = level.getPlayers(p ->
                !p.isCreative() && !p.isSpectator() && p.distanceToSqr(this.getX(), p.getY(), this.getZ()) <= 26.0D * 26.0D);
        for (ServerPlayer player : victims) {
            player.hurt(this.damageSources().mobAttack(this), 14.0F);
            Vec3 away = player.position().subtract(this.position());
            Vec3 horiz = new Vec3(away.x, 0.0D, away.z).normalize().scale(1.4D);
            player.push(horiz.x, 0.75D, horiz.z);
            player.hurtMarked = true;
        }
    }

    private void beginVanish(ServerLevel level) {
        this.setSkyState(STATE_VANISHING);
        this.stateTicks = 25;
        level.playSound(null, this.blockPosition(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 4.0F, 0.75F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 4, state -> {
            int skyState = this.getSkyState();
            if (skyState == STATE_STOMPING) {
                return state.setAndContinue(ANIM_STOMP);
            }
            if (skyState == STATE_VANISHING) {
                return state.setAndContinue(ANIM_VANISH);
            }
            return state.setAndContinue(ANIM_IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
