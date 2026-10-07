package org.cyclops.colossalchests2.block;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.colossalchests2.client.render.ItemRendererUncolossalChest;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.Consumer;

/**
 * Forge config for the {@link BlockUncolossalChest}, whose item renders like the block.
 * @author rubensworks
 */
public class BlockUncolossalChestConfigForge<M extends IModBase> extends BlockUncolossalChestConfig<M> {

    public BlockUncolossalChestConfigForge(M mod) {
        super(mod, (eConfig, block) -> new BlockItem(block, new Item.Properties()) {
            @Override
            public void initializeClient(Consumer<IClientItemExtensions> consumer) {
                consumer.accept(new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (renderer == null) {
                            renderer = new ItemRendererUncolossalChest();
                        }
                        return renderer;
                    }
                });
            }
        });
    }
}
