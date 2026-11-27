package com.underworld.mod.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;

public final class StructureBuildUtil {
    private StructureBuildUtil() {}

    public static void setIfInside(ServerLevelAccessor level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    public static void fillBox(ServerLevelAccessor level, BoundingBox box,
                               int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    setIfInside(level, box, new BlockPos(x, y, z), state);
                }
            }
        }
    }

    public static void fillColumnDown(ServerLevelAccessor level, BoundingBox box, BlockPos startPos, int maxDepth, BlockState fill) {
        for (int d = 0; d < maxDepth; d++) {
            BlockPos p = startPos.below(d);
            if (!box.isInside(p)) {
                continue;
            }
            if (p.getY() <= level.getMinBuildHeight() + 1) {
                break;
            }
            BlockState existing = level.getBlockState(p);
            if (!existing.isAir() && existing.getFluidState().isEmpty() && d > 0) {
                break;
            }
            level.setBlock(p, fill, 2);
        }
    }

    public static void fillFoundationSolid(ServerLevelAccessor level, BoundingBox box, BlockPos startPos, int maxDepth, BlockState fill) {
        for (int d = 0; d < maxDepth; d++) {
            BlockPos p = startPos.below(d);
            if (!box.isInside(p)) {
                continue;
            }
            if (p.getY() <= level.getMinBuildHeight() + 1) {
                break;
            }
            BlockState existing = level.getBlockState(p);
            if (d > 0 && existing.isSolidRender(level, p)) {
                break;
            }
            level.setBlock(p, fill, 2);
        }
    }

    public static void placeChest(ServerLevelAccessor level, BoundingBox box, BlockPos pos, RandomSource random, ResourceLocation lootTable) {
        if (box.isInside(pos)) {
            level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(lootTable, random.nextLong());
            }
        }
    }

    public static void placeChestWithItems(ServerLevelAccessor level, BoundingBox box, BlockPos pos, Direction facing,
                                           RandomSource random, @Nullable ResourceLocation lootTable, ItemStack... items) {
        if (!box.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            if (lootTable != null) {
                chest.setLootTable(lootTable, random.nextLong());
            }
            int slot = 0;
            for (ItemStack item : items) {
                if (!item.isEmpty() && slot < chest.getContainerSize()) {
                    chest.setItem(slot++, item.copy());
                }
            }
            chest.setChanged();
        }
    }

    public static ItemStack createLoreBook(String bookId, String rawTitle, String author, int pageCount) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(WrittenBookItem.TAG_TITLE, rawTitle);
        tag.putString(WrittenBookItem.TAG_AUTHOR, author);
        tag.putBoolean(WrittenBookItem.TAG_RESOLVED, true);
        tag.putInt(WrittenBookItem.TAG_GENERATION, 0);

        ListTag pages = new ListTag();
        for (int i = 1; i <= pageCount; i++) {
            Component comp = Component.translatable("book.under_world." + bookId + ".page_" + i);
            pages.add(StringTag.valueOf(Component.Serializer.toJson(comp)));
        }
        tag.put(WrittenBookItem.TAG_PAGES, pages);
        stack.setHoverName(Component.translatable("book.under_world." + bookId + ".title"));
        return stack;
    }

    public static void placeLecternWithBook(ServerLevelAccessor level, BoundingBox box, BlockPos pos,
                                            Direction facing, ItemStack book) {
        if (!box.isInside(pos)) {
            return;
        }
        BlockState state = Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, facing)
                .setValue(LecternBlock.HAS_BOOK, true);
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
            lectern.setBook(book.copy());
            lectern.setChanged();
        }
    }

    public static void placeWallSign(ServerLevelAccessor level, BoundingBox box, BlockPos pos, Direction facing,
                                     Block wallSignBlock, DyeColor color, boolean glowing, String... lines) {
        if (!box.isInside(pos)) {
            return;
        }
        BlockState state = wallSignBlock.defaultBlockState().setValue(WallSignBlock.FACING, facing);
        level.setBlock(pos, state, 2);
        applySignText(level, pos, color, glowing, lines);
    }

    public static void placeStandingSign(ServerLevelAccessor level, BoundingBox box, BlockPos pos, int rotation16,
                                         Block standingSignBlock, DyeColor color, boolean glowing, String... lines) {
        if (!box.isInside(pos)) {
            return;
        }
        BlockState state = standingSignBlock.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation16 & 15);
        level.setBlock(pos, state, 2);
        applySignText(level, pos, color, glowing, lines);
    }

    public static void placeWallOrStandingSign(ServerLevelAccessor level, BoundingBox box, BlockPos pos,
                                               Direction facing, DyeColor color, boolean glowing, String... lines) {
        placeStandingSign(level, box, pos, facingToRotation16(facing), Blocks.OAK_SIGN, color, glowing, lines);
    }

    public static void placeWallOrStandingSign(ServerLevelAccessor level, BlockPos pos,
                                               Direction facing, boolean glowing, String... lines) {
        placeStandingSign(level, new BoundingBox(pos), pos, facingToRotation16(facing), Blocks.OAK_SIGN,
                glowing ? DyeColor.RED : DyeColor.BLACK, glowing, lines);
    }

    private static int facingToRotation16(Direction facing) {
        return switch (facing) {
            case NORTH -> 8;
            case EAST -> 12;
            case WEST -> 4;
            default -> 0;
        };
    }

    private static void applySignText(ServerLevelAccessor level, BlockPos pos, DyeColor color, boolean glowing, String... lines) {
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            Component[] msgs = new Component[]{
                    Component.empty(), Component.empty(), Component.empty(), Component.empty()
            };
            for (int i = 0; i < Math.min(4, lines.length); i++) {
                String s = lines[i];
                if (s != null && !s.isEmpty()) {
                    msgs[i] = s.startsWith("sign.") ? Component.translatable(s) : Component.literal(s);
                }
            }
            SignText text = new SignText(msgs, msgs, color, glowing);
            sign.setText(text, true);
            sign.setWaxed(true);
            sign.setChanged();
        }
    }
}
