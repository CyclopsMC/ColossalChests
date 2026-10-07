package org.cyclops.colossalchests2.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.colossalchests2.api.IChest;

/**
 * A block that can be part of a chest's shell in any material, such as a functional wall.
 * Its block state needs {@link #FORMED}, which the core sets. {@link ChestMemberBlock} does all of this.
 * @author rubensworks
 */
public interface IChestMember {

    /**
     * If the block is part of a formed chest. Formed members are drawn by the chest, so their formed model should be invisible.
     */
    BooleanProperty FORMED = BooleanProperty.create("formed");

    /**
     * Called on the server, at most once per tick, after the contents of the formed chest changed.
     * @param state The state of this member.
     * @param level The level.
     * @param pos The position of this member.
     * @param chest The chest.
     */
    default void onChestContentsChanged(BlockState state, Level level, BlockPos pos, IChest chest) {
    }

    /**
     * Formed members do not hide their neighbors, as the chest drawn around them is not a full block when open.
     */
    static VoxelShape getFormedOcclusionShape(BlockState state, VoxelShape unformedShape) {
        return state.getValue(FORMED) ? Shapes.empty() : unformedShape;
    }

    /**
     * Formed members let light through, so blocks next to an open lid are not drawn dark.
     */
    static int getFormedLightBlock(BlockState state, int unformedLightBlock) {
        return state.getValue(FORMED) ? 0 : unformedLightBlock;
    }

    static boolean getFormedPropagatesSkylightDown(BlockState state, boolean unformedPropagates) {
        return state.getValue(FORMED) || unformedPropagates;
    }

}
