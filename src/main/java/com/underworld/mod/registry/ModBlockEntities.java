package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.block.FunFlowerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, UnderWorldMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<FunFlowerBlockEntity>> FUN_FLOWER =
            BLOCK_ENTITIES.register("fun_flower", () -> BlockEntityType.Builder.of(
                    FunFlowerBlockEntity::new,
                    ModBlocks.FUN_YELLOW_FLOWER.get(),
                    ModBlocks.FUN_BLUE_FLOWER.get(),
                    ModBlocks.FUN_FLAT_RED_FLOWER.get(),
                    ModBlocks.FUN_FLAT_BLUE_FLOWER.get(),
                    ModBlocks.FUN_FLAT_WHITE_FLOWER.get()
            ).build(null));

    private ModBlockEntities() {}
}
