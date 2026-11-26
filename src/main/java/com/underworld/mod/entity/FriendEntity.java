package com.underworld.mod.entity;

import com.underworld.mod.config.UnderWorldConfigHolder;
import com.underworld.mod.registry.ModDimensions;
import com.underworld.mod.registry.ModSounds;
import com.underworld.mod.world.structure.VoidShrinePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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

    private static final String[][] CRYPTIC_SIGNS = {
            {"hello?", "", "i am here", ""},
            {"look behind", "you", "", "friend"},
            {"the flowers", "watch at night", "", "dont blink"},
            {"red bricks", "endless halls", "keep running", ""},
            {"fun world", "awaits you", "", ":)"}
    };

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int relocateCooldown = 200;
    private int spottedTicks = 0;
    private int actionCooldown = 240;

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
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 40.0F, 1.0F));
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
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.tickCount % 40 == 0) {
            String expected = UnderWorldConfigHolder.getFriendDisplayName();
            if (!this.hasCustomName() || !this.getCustomName().getString().equals(expected)) {
                this.setCustomName(Component.literal(expected));
                this.setCustomNameVisible(true);
            }
        }

        Player nearest = serverLevel.getNearestPlayer(this, 96.0D);
        if (nearest == null) {
            return;
        }

        double distSq = this.distanceToSqr(nearest);
        boolean lookedAt = isPlayerLookingAtMe(nearest);

        // Close-range spot reaction: vanish in smoke OR pull victim into The Fun World!
        if (!nearest.isCreative() && !nearest.isSpectator() && distSq <= 15.0D * 15.0D && lookedAt) {
            this.spottedTicks++;
            if (this.spottedTicks >= 12 || distSq <= 6.5D * 6.5D) {
                handleCloseSpot(serverLevel, nearest);
                return;
            }
        } else {
            this.spottedTicks = Math.max(0, this.spottedTicks - 1);
        }

        // Open nearby doors & place cryptic signs quietly when player isn't looking closely
        if (this.actionCooldown > 0) {
            this.actionCooldown--;
        } else if (!lookedAt && distSq >= 10.0D * 10.0D && distSq <= 38.0D * 38.0D) {
            this.actionCooldown = 360 + this.random.nextInt(320);
            performStalkerAction(serverLevel, nearest);
        }

        if (distSq < 18.0D * 18.0D) {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
        } else if (distSq > 44.0D * 44.0D) {
            if (this.relocateCooldown > 0) {
                this.relocateCooldown--;
            } else if (!lookedAt) {
                tryRelocateNear(serverLevel, nearest);
                this.relocateCooldown = 280;
            }
        }
    }

    private void handleCloseSpot(ServerLevel level, Player nearest) {
        serverLevelVanishEffect(level);

        boolean funEnabled = UnderWorldConfigHolder.get() == null
                || UnderWorldConfigHolder.get().dimensions.funWorldEnabled.get();
        double warpChance = UnderWorldConfigHolder.get() != null
                ? UnderWorldConfigHolder.get().friend.friendFunWorldWarpChance.get()
                : 0.28D;

        if (funEnabled && nearest instanceof ServerPlayer sp
                && level.dimension().equals(Level.OVERWORLD)
                && this.random.nextDouble() < warpChance) {
            ModDimensions.sendToFunWorld(sp);
            this.discard();
            return;
        }

        // Otherwise vanish & reposition cleanly behind player
        this.spottedTicks = 0;
        tryRelocateNear(level, nearest);
    }

    private void serverLevelVanishEffect(ServerLevel level) {
        level.playSound(null, this.blockPosition(), ModSounds.FRIEND_VANISH.get(), SoundSource.HOSTILE, 1.2F, 0.9F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(), 18, 0.35D, 0.7D, 0.35D, 0.03D);
    }

    private void performStalkerAction(ServerLevel level, Player player) {
        boolean doorEnabled = UnderWorldConfigHolder.get() == null
                || UnderWorldConfigHolder.get().friend.friendDoorInteractionEnabled.get();
        if (!doorEnabled) {
            return;
        }

        // 1. Look for a closed door within 10 blocks to quietly crack open
        BlockPos center = this.blockPosition();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(p);
                    if (state.getBlock() instanceof DoorBlock door
                            && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
                            && !door.isOpen(state)) {
                        door.setOpen(this, level, state, p, true);
                        level.playSound(null, p, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.85F);
                        return;
                    }
                }
            }
        }

        // 2. Occasionally place a cryptic oak sign near current stalking vantage point
        if (this.random.nextFloat() < 0.45F) {
            tryPlaceCrypticSign(level, center, player);
            return;
        }

        // 3. Rare chance on surface overworld (far from player > 26 blocks) to leave a Void Shrine!
        if (level.dimension().equals(Level.OVERWORLD)
                && this.distanceToSqr(player) > 26.0D * 26.0D
                && level.canSeeSky(center)
                && this.random.nextFloat() < 0.14F) {
            VoidShrinePiece.placeAt(level, center);
            serverLevelVanishEffect(level);
            tryRelocateNear(level, player);
        }
    }

    private void tryPlaceCrypticSign(ServerLevel level, BlockPos pos, Player player) {
        BlockPos below = pos.below();
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(below).isSolidRender(level, below)) {
            return;
        }
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        int rot = Mth.floor((Math.atan2(-dx, dz) * 180.0D / Math.PI) * 16.0D / 360.0D + 0.5D) & 15;
        BlockState signState = Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rot);
        level.setBlock(pos, signState, 3);
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            String[] lines = CRYPTIC_SIGNS[this.random.nextInt(CRYPTIC_SIGNS.length)];
            Component[] front = new Component[]{
                    Component.literal(lines[0]),
                    Component.literal(lines[1]),
                    Component.literal(lines[2]),
                    Component.literal(lines[3])
            };
            Component[] empty = new Component[]{Component.empty(), Component.empty(), Component.empty(), Component.empty()};
            sign.setText(new SignText(front, empty, DyeColor.BLACK, false), true);
            sign.setChanged();
        }
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
    }

    private boolean isPlayerLookingAtMe(Player player) {
        var look = player.getLookAngle().normalize();
        var toMe = this.position().subtract(player.getEyePosition()).normalize();
        return look.dot(toMe) > 0.65D && player.hasLineOfSight(this);
    }

    private void tryRelocateNear(ServerLevel level, Player player) {
        double behindYaw = Math.toRadians(player.getYRot() + 180.0F + (this.random.nextFloat() - 0.5F) * 90.0F);
        double dist = 18.0D + this.random.nextDouble() * 10.0D;
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
