package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.world.structure.BrokenShaftPiece;
import com.underworld.mod.world.structure.BrokenShaftStructure;
import com.underworld.mod.world.structure.VoidShrinePiece;
import com.underworld.mod.world.structure.VoidShrineStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, UnderWorldMod.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, UnderWorldMod.MOD_ID);

    public static final RegistryObject<StructureType<VoidShrineStructure>> VOID_SHRINE =
            STRUCTURE_TYPES.register("void_shrine", () -> () -> VoidShrineStructure.CODEC);

    public static final RegistryObject<StructureType<BrokenShaftStructure>> BROKEN_SHAFT =
            STRUCTURE_TYPES.register("broken_shaft", () -> () -> BrokenShaftStructure.CODEC);

    public static final RegistryObject<StructurePieceType> VOID_SHRINE_PIECE =
            STRUCTURE_PIECES.register("void_shrine", () -> (StructurePieceType.ContextlessType) VoidShrinePiece::new);

    public static final RegistryObject<StructurePieceType> BROKEN_SHAFT_PIECE =
            STRUCTURE_PIECES.register("broken_shaft", () -> (StructurePieceType.ContextlessType) BrokenShaftPiece::new);

    private ModStructures() {}
}
