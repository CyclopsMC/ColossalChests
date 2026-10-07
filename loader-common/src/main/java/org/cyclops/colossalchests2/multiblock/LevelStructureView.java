package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.api.block.IChestMember;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.jetbrains.annotations.Nullable;

/**
 * {@link StructureView} on a level, as seen by one core.
 * @author rubensworks
 */
public class LevelStructureView implements StructureView {

    private final Level level;
    private final BlockPos core;

    public LevelStructureView(Level level, BlockPos core) {
        this.level = level;
        this.core = core;
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return level.isLoaded(pos);
    }

    @Nullable
    @Override
    public Member getMember(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockChestCore coreBlock) {
            return new Member(coreBlock.getMaterial().id(), true);
        }
        if (state.getBlock() instanceof IChestMember member) {
            boolean claimedByOther = state.getValue(IChestMember.FORMED) && ChestCoreIndex.findFormedCore(level, pos)
                    .filter(other -> !other.getBlockPos().equals(core))
                    .isPresent();
            return claimedByOther ? null : new Member(member instanceof BlockChestWall wall ? wall.getMemberMaterial() : Member.ANY_MATERIAL, false);
        }
        return null;
    }

    @Override
    public boolean isEmpty(BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }
}
