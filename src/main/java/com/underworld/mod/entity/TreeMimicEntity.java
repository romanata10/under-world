package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class TreeMimicEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_AWAKENED =
            SynchedEntityData.defineId(TreeMimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_AWAKEN_TICKS =
            SynchedEntityData.defineId(TreeMimicEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ANIM_DISGUISED = RawAnimation.begin().thenLoop("misc.disguised");
    private static final RawAnimation ANIM_AWAKEN = RawAnimation.begin().thenPlayAndHold("misc.awaken");
    private static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("move.run");
    private static final RawAnimation ANIM_SLAM = RawAnimation.begin().thenPlay("attack.slam");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TreeMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_AWAKENED, false);
        this.entityData.define(DATA_AWAKEN_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, true) {
            @Override
            public boolean canUse() {
                return TreeMimicEntity.this.isAwakened() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return TreeMimicEntity.this.isAwakened() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return TreeMimicEntity.this.isAwakened() && super.canUse();
            }
        });
    }

    public boolean isAwakened() {
        return this.entityData.get(DATA_AWAKENED);
    }

    public void setAwakened(boolean awakened) {
        this.entityData.set(DATA_AWAKENED, awakened);
    }

    public int getAwakenTicks() {
        return this.entityData.get(DATA_AWAKEN_TICKS);
    }

    public void setAwakenTicks(int ticks) {
        this.entityData.set(DATA_AWAKEN_TICKS, ticks);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Awakened", this.isAwakened());
        tag.putInt("AwakenTicks", this.getAwakenTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAwakened(tag.getBoolean("Awakened"));
        this.setAwakenTicks(tag.getInt("AwakenTicks"));
    }

    @Override
    public boolean isPushable() {
        return this.isAwakened() && super.isPushable();
    }

    @Override
    public boolean isSilent() {
        return !this.isAwakened();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (!this.isAwakened()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            double triggerRange = UnderWorldConfigHolder.get() != null
                    ? UnderWorldConfigHolder.get().treeMimic.treeMimicTriggerRange.get()
                    : 8.5D;
            Player nearest = this.level().getNearestPlayer(this, triggerRange);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                awaken(nearest);
            }
            return;
        }

        int awakenTimer = this.getAwakenTicks();
        if (awakenTimer > 0) {
            this.setAwakenTicks(awakenTimer - 1);
            this.getNavigation().stop();
            return;
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D && this.tickCount % 14 == 0) {
            this.level().playSound(null, this.blockPosition(), ModSounds.TREE_STEP.get(), SoundSource.HOSTILE,
                    1.2F, 0.85F + this.random.nextFloat() * 0.25F);
        }
    }

    public void awaken(@Nullable LivingEntity target) {
        if (this.isAwakened()) {
            return;
        }
        this.setAwakened(true);
        this.setAwakenTicks(18);
        if (target != null) {
            this.setTarget(target);
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.blockPosition(), ModSounds.TREE_AWAKEN.get(), SoundSource.HOSTILE, 1.8F, 0.95F);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LOG.defaultBlockState()),
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    28, 1.0D, 0.8D, 1.0D, 0.1D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SQUID_INK,
                    this.getX(), this.getY() + 0.3D, this.getZ(),
                    20, 1.2D, 0.4D, 1.2D, 0.05D
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.isAwakened()) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity le ? le : null;
            awaken(attacker);
        }

        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            boolean usedAxe = source.getEntity() instanceof LivingEntity attacker
                    && attacker.getMainHandItem().getItem() instanceof AxeItem;
            if (usedAxe) {
                amount *= 2.0F;
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.blockPosition(), SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 1.3F, 0.8F);
                }
            } else {
                amount *= 0.2F;
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.blockPosition(), SoundEvents.WOOD_HIT, SoundSource.HOSTILE, 1.1F, 0.65F);
                }
                boolean requireAxe = UnderWorldConfigHolder.get() == null
                        || UnderWorldConfigHolder.get().treeMimic.treeMimicRequireAxe.get();
                if (requireAxe && this.getHealth() - amount <= 1.0F) {
                    amount = Math.max(0.0F, this.getHealth() - 1.0F);
                    if (source.getEntity() instanceof ServerPlayer sp) {
                        sp.displayClientMessage(
                                Component.translatable("message.under_world.tree_needs_axe").withStyle(ChatFormatting.GRAY),
                                true
                        );
                    }
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.triggerAnim("Actions", "slam");
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.blockPosition(), ModSounds.TREE_STEP.get(), SoundSource.HOSTILE, 1.5F, 0.75F);
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "State", 3, state -> {
            if (!this.isAwakened()) {
                return state.setAndContinue(ANIM_DISGUISED);
            }
            if (this.getAwakenTicks() > 0) {
                return state.setAndContinue(ANIM_AWAKEN);
            }
            return state.setAndContinue(ANIM_RUN);
        }));
        controllers.add(DefaultAnimations.triggerOnlyController(this)
                .triggerableAnim("slam", ANIM_SLAM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
