package com.nightterror.entity;

import com.nightterror.NightTerrorMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NightStalkerEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> IS_ENRAGED =
            SynchedEntityData.defineId(NightStalkerEntity.class, EntityDataSerializers.BOOLEAN);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.nightterror.night_stalker"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Phase tracking
    private int phase = 1;
    private int teleportCooldown = 0;
    private int screamCooldown = 0;

    public NightStalkerEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ENRAGED, false);
    }

    public boolean isEnraged() {
        return this.entityData.get(IS_ENRAGED);
    }

    public void setEnraged(boolean enraged) {
        this.entityData.set(IS_ENRAGED, enraged);
    }

    @Override
    public void tick() {
        super.tick();

        // Only active at night — self-destruct at dawn
        if (!this.level().isClientSide) {
            long dayTime = this.level().getDayTime() % 24000;
            boolean isDay = dayTime < 13000 || dayTime > 23000;

            if (isDay) {
                // Boss burns in daylight like a vampire
                if (this.isInSunlight() && this.level().isDay()) {
                    this.setSecondsOnFire(8);
                }
            }

            // Phase 2 at 50% HP
            float healthPercent = this.getHealth() / this.getMaxHealth();
            if (healthPercent <= 0.5f && phase == 1) {
                phase = 2;
                setEnraged(true);
                bossEvent.setColor(BossEvent.BossBarColor.RED);
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.45);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(18.0);
                this.playSound(SoundEvents.WITHER_AMBIENT, 2.0f, 0.5f);
                // Apply darkness to all nearby players
                for (Player p : this.level().getEntitiesOfClass(Player.class,
                        this.getBoundingBox().inflate(30))) {
                    ((ServerPlayer)p).connection.send(
                        new net.minecraft.network.protocol.game.ClientboundGameEventPacket(
                            net.minecraft.network.protocol.game.ClientboundGameEventPacket.LIMITED_CRAFTING,
                            0));
                    p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                }
            }

            // Teleport behind target in phase 2
            if (phase == 2 && teleportCooldown <= 0 && this.getTarget() != null) {
                LivingEntity target = this.getTarget();
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 5.0) {
                    double nx = target.getX() - (dx / dist) * 1.5;
                    double nz = target.getZ() - (dz / dist) * 1.5;
                    this.teleportTo(nx, target.getY(), nz);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5f, 0.6f);
                    teleportCooldown = 80;
                }
            }
            if (teleportCooldown > 0) teleportCooldown--;

            // Periodic terror scream (applies weakness + slowness)
            if (screamCooldown <= 0 && phase >= 1) {
                for (Player p : this.level().getEntitiesOfClass(Player.class,
                        this.getBoundingBox().inflate(20))) {
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
                }
                this.playSound(SoundEvents.WITHER_SHOOT, 1.5f, 0.4f);
                screamCooldown = 200;
            }
            if (screamCooldown > 0) screamCooldown--;

            // Update boss bar progress
            bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", phase);
        tag.putBoolean("Enraged", isEnraged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        phase = tag.getInt("Phase");
        setEnraged(tag.getBoolean("Enraged"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    // GeckoLib animation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "controller", 5, state -> {
            if (state.isMoving()) {
                if (isEnraged()) {
                    state.getController().setAnimation(
                        RawAnimation.begin().then("animation.night_stalker.run", Animation.LoopType.LOOP));
                } else {
                    state.getController().setAnimation(
                        RawAnimation.begin().then("animation.night_stalker.walk", Animation.LoopType.LOOP));
                }
            } else {
                state.getController().setAnimation(
                    RawAnimation.begin().then("animation.night_stalker.idle", Animation.LoopType.LOOP));
            }
            return PlayState.CONTINUE;
        }));

        registrar.add(new AnimationController<>(this, "attack_controller", 0, state -> {
            if (this.swinging) {
                state.getController().setAnimation(
                    RawAnimation.begin().then("animation.night_stalker.attack", Animation.LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Spawn only at night
    public static boolean checkNightStalkerSpawnRules(EntityType<NightStalkerEntity> type,
            ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, java.util.Random random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && (level.getLevelData().getDayTime() % 24000 >= 13000);
    }
}
