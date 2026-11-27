package com.underworld.mod.registry;

import com.underworld.mod.UnderWorldMod;
import com.underworld.mod.world.structure.BrokenShaftPiece;
import com.underworld.mod.world.structure.BrokenShaftStructure;
import com.underworld.mod.world.structure.ClockStatuePiece;
import com.underworld.mod.world.structure.ClockStatueStructure;
import com.underworld.mod.world.structure.ElongatedTreePiece;
import com.underworld.mod.world.structure.ElongatedTreeStructure;
import com.underworld.mod.world.structure.FountainPiece;
import com.underworld.mod.world.structure.FountainStructure;
import com.underworld.mod.world.structure.GlitchClusterPiece;
import com.underworld.mod.world.structure.GlitchClusterStructure;
import com.underworld.mod.world.structure.LeafMazePiece;
import com.underworld.mod.world.structure.LeafMazeStructure;
import com.underworld.mod.world.structure.OldVillageHousePiece;
import com.underworld.mod.world.structure.OldVillageHouseStructure;
import com.underworld.mod.world.structure.VoidShrinePiece;
import com.underworld.mod.world.structure.VoidShrineStructure;
import com.underworld.mod.world.structure.WheatFarmPiece;
import com.underworld.mod.world.structure.WheatFarmStructure;
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

    public static final RegistryObject<StructureType<FountainStructure>> FOUNTAIN =
            STRUCTURE_TYPES.register("fountain", () -> () -> FountainStructure.CODEC);

    public static final RegistryObject<StructureType<GlitchClusterStructure>> GLITCH_CLUSTER =
            STRUCTURE_TYPES.register("glitch_cluster", () -> () -> GlitchClusterStructure.CODEC);

    public static final RegistryObject<StructureType<ElongatedTreeStructure>> ELONGATED_TREE =
            STRUCTURE_TYPES.register("elongated_tree", () -> () -> ElongatedTreeStructure.CODEC);

    public static final RegistryObject<StructureType<WheatFarmStructure>> WHEAT_FARM =
            STRUCTURE_TYPES.register("wheat_farm", () -> () -> WheatFarmStructure.CODEC);

    public static final RegistryObject<StructureType<OldVillageHouseStructure>> OLD_VILLAGE_HOUSE =
            STRUCTURE_TYPES.register("old_village_house", () -> () -> OldVillageHouseStructure.CODEC);

    public static final RegistryObject<StructureType<ClockStatueStructure>> CLOCK_STATUE =
            STRUCTURE_TYPES.register("clock_statue", () -> () -> ClockStatueStructure.CODEC);

    public static final RegistryObject<StructureType<LeafMazeStructure>> LEAF_MAZE =
            STRUCTURE_TYPES.register("leaf_maze", () -> () -> LeafMazeStructure.CODEC);

    public static final RegistryObject<StructurePieceType> VOID_SHRINE_PIECE =
            STRUCTURE_PIECES.register("void_shrine", () -> (StructurePieceType.ContextlessType) VoidShrinePiece::new);

    public static final RegistryObject<StructurePieceType> BROKEN_SHAFT_PIECE =
            STRUCTURE_PIECES.register("broken_shaft", () -> (StructurePieceType.ContextlessType) BrokenShaftPiece::new);

    public static final RegistryObject<StructurePieceType> FOUNTAIN_PIECE =
            STRUCTURE_PIECES.register("fountain", () -> (StructurePieceType.ContextlessType) FountainPiece::new);

    public static final RegistryObject<StructurePieceType> GLITCH_CLUSTER_PIECE =
            STRUCTURE_PIECES.register("glitch_cluster", () -> (StructurePieceType.ContextlessType) GlitchClusterPiece::new);

    public static final RegistryObject<StructurePieceType> ELONGATED_TREE_PIECE =
            STRUCTURE_PIECES.register("elongated_tree", () -> (StructurePieceType.ContextlessType) ElongatedTreePiece::new);

    public static final RegistryObject<StructurePieceType> WHEAT_FARM_PIECE =
            STRUCTURE_PIECES.register("wheat_farm", () -> (StructurePieceType.ContextlessType) WheatFarmPiece::new);

    public static final RegistryObject<StructurePieceType> OLD_VILLAGE_HOUSE_PIECE =
            STRUCTURE_PIECES.register("old_village_house", () -> (StructurePieceType.ContextlessType) OldVillageHousePiece::new);

    public static final RegistryObject<StructurePieceType> CLOCK_STATUE_PIECE =
            STRUCTURE_PIECES.register("clock_statue", () -> (StructurePieceType.ContextlessType) ClockStatuePiece::new);

    public static final RegistryObject<StructurePieceType> LEAF_MAZE_PIECE =
            STRUCTURE_PIECES.register("leaf_maze", () -> (StructurePieceType.ContextlessType) LeafMazePiece::new);

    private ModStructures() {}
}
