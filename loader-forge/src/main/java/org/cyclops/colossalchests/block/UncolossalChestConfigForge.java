package org.cyclops.colossalchests.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.colossalchests.client.render.blockentity.ItemStackTileEntityUncolossalChestRenderForge;
import org.cyclops.cyclopscore.init.ModBaseForge;

import java.util.function.Consumer;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigForge<M extends ModBaseForge> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigForge(M mod) {
        super(
                mod,
                "uncolossal_chest",
                eConfig -> new UncolossalChest(((UncolossalChestConfig<M>) eConfig).getProperties()),
                (eConfig, block) -> new BlockItem(block, new Item.Properties()) {
                    @Override
                    @OnlyIn(Dist.CLIENT)
                    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                        consumer.accept(new ItemStackTileEntityUncolossalChestRenderForge.ClientItemExtensions());
                    }
                }
        );
    }

}
