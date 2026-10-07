package org.cyclops.colossalchests2.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.BiFunction;

/**
 * Config for the {@link BlockUncolossalChest}.
 * @author rubensworks
 */
public class BlockUncolossalChestConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public BlockUncolossalChestConfig(M mod) {
        this(mod, getDefaultItemConstructor(mod));
    }

    /**
     * @param itemConstructor For loaders that need their own item class, such as for custom item rendering.
     */
    public BlockUncolossalChestConfig(M mod, BiFunction<BlockConfigCommon<M>, Block, ? extends Item> itemConstructor) {
        super(
                mod,
                "uncolossal_chest",
                eConfig -> new BlockUncolossalChest(BlockBehaviour.Properties.of()
                        .strength(5.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.WOOD)),
                itemConstructor
        );
    }

}
