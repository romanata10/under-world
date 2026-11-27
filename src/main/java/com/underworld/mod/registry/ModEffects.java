package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.effect.GeometricNightmareEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, UnderWorldMod.MOD_ID);

    // Intentionally registered ONLY as a MobEffect (never as a Potion item)
    public static final RegistryObject<MobEffect> GEOMETRIC_NIGHTMARE =
            MOB_EFFECTS.register("geometric_nightmare", GeometricNightmareEffect::new);

    private ModEffects() {}
}
