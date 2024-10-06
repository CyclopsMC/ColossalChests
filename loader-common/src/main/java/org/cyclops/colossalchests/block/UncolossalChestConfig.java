package org.cyclops.colossalchests.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public abstract class UncolossalChestConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public UncolossalChestConfig(M mod) {
        super(
                mod,
            "uncolossal_chest",
                eConfig -> new UncolossalChest(Block.Properties.of()
                        .strength(5.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.WOOD)),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }
}
