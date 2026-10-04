package org.cyclops.colossalchests2.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A formed chest: a hollow cube of walls with one core on its shell.
 * @param min The lowest corner.
 * @param size The outer edge length.
 * @author rubensworks
 */
public record ChestStructure(BlockPos min, int size) {

    public static final Codec<ChestStructure> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("min").forGetter(ChestStructure::min),
            Codec.INT.fieldOf("size").forGetter(ChestStructure::size)
    ).apply(i, ChestStructure::new));

    public BlockPos max() {
        return min.offset(size - 1, size - 1, size - 1);
    }

    /**
     * @param pos A position.
     * @return If the position is inside the cube, shell included.
     */
    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() < min.getX() + size
                && pos.getY() >= min.getY() && pos.getY() < min.getY() + size
                && pos.getZ() >= min.getZ() && pos.getZ() < min.getZ() + size;
    }

    /**
     * @param pos A position.
     * @return If the position is on the shell of the cube.
     */
    public boolean isOnShell(BlockPos pos) {
        if (!contains(pos)) {
            return false;
        }
        int dx = pos.getX() - min.getX();
        int dy = pos.getY() - min.getY();
        int dz = pos.getZ() - min.getZ();
        int last = size - 1;
        return dx == 0 || dx == last || dy == 0 || dy == last || dz == 0 || dz == last;
    }

    /**
     * @return All positions on the shell, as fresh immutable positions.
     */
    public Iterable<BlockPos> shell() {
        return () -> new Iterator<>() {
            private final Iterator<BlockPos> all = BlockPos.betweenClosed(min, max()).iterator();
            private BlockPos next = advance();

            private BlockPos advance() {
                while (all.hasNext()) {
                    BlockPos pos = all.next();
                    if (isOnShell(pos)) {
                        return pos.immutable();
                    }
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public BlockPos next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                BlockPos current = next;
                next = advance();
                return current;
            }
        };
    }

}
