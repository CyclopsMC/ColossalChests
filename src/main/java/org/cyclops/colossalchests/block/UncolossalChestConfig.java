package org.cyclops.colossalchests.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.blockentity.ItemStackTileEntityUncolossalChestRender;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

import java.util.function.Consumer;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfig extends BlockConfig {

    public UncolossalChestConfig() {
        super(
                ColossalChests._instance,
            "uncolossal_chest",
                eConfig -> new UncolossalChest(Block.Properties.of()
                        .strength(5.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.WOOD)),
                (eConfig, block) -> {
                    Item.Properties itemProperties = new Item.Properties();
                    return new BlockItem(block, itemProperties) {
                        @Override
                        @OnlyIn(Dist.CLIENT)
                        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                            consumer.accept(new ItemStackTileEntityUncolossalChestRender.ClientItemExtensions());
                        }
                    };
                }
        );
    }

}
