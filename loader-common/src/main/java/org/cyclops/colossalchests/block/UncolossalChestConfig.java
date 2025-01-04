package org.cyclops.colossalchests.block;

import net.minecraft.resources.ResourceKey;
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
                (eConfig, properties) -> new UncolossalChest(((UncolossalChestConfig<M>) eConfig).getProperties()),
                getDefaultItemConstructor(mod)
        );
    }

    public Block.Properties getProperties() {
        return Block.Properties.of()
                .setId((ResourceKey<Block>) getResourceKey())
                .overrideDescription("block.colossalchests.interface")
                .strength(5.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD);
    }
}
