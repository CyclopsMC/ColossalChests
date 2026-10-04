package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * In-memory block grid for structure detection tests.
 * Unset positions are empty.
 * @author rubensworks
 */
public class GridView implements StructureView {

    public static final Object SOLID = new Object();

    private final Map<BlockPos, Object> blocks = Maps.newHashMap();
    private final Set<BlockPos> unloaded = Sets.newHashSet();

    public GridView wall(BlockPos pos, String material) {
        blocks.put(pos.immutable(), new Member(material, false));
        return this;
    }

    /**
     * A wall that fits any material, like a functional wall.
     */
    public GridView functionalWall(BlockPos pos) {
        blocks.put(pos.immutable(), new Member(Member.ANY_MATERIAL, false));
        return this;
    }

    public GridView core(BlockPos pos, String material) {
        blocks.put(pos.immutable(), new Member(material, true));
        return this;
    }

    public GridView solid(BlockPos pos) {
        blocks.put(pos.immutable(), SOLID);
        return this;
    }

    public GridView clear(BlockPos pos) {
        blocks.remove(pos);
        return this;
    }

    public GridView unload(BlockPos pos) {
        unloaded.add(pos.immutable());
        return this;
    }

    /**
     * Build the shell of a cube out of walls.
     */
    public GridView cube(BlockPos min, int size, String material) {
        ChestStructure structure = new ChestStructure(min, size);
        for (BlockPos pos : structure.shell()) {
            wall(pos, material);
        }
        return this;
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return !unloaded.contains(pos);
    }

    @Nullable
    @Override
    public Member getMember(BlockPos pos) {
        return blocks.get(pos) instanceof Member member ? member : null;
    }

    @Override
    public boolean isEmpty(BlockPos pos) {
        return !blocks.containsKey(pos);
    }
}
