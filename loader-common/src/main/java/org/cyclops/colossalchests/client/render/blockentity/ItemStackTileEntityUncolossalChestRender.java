package org.cyclops.colossalchests.client.render.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * @author rubensworks
 */
public class ItemStackTileEntityUncolossalChestRender extends ItemStackBlockEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRender() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState()));
    }

    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ItemStackTileEntityUncolossalChestRender.Unbaked> MAP_CODEC = MapCodec.unit(ItemStackTileEntityUncolossalChestRender.Unbaked::new);

        @Override
        public MapCodec<ItemStackTileEntityUncolossalChestRender.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet) {
            return new ItemStackTileEntityUncolossalChestRender();
        }
    }

}
