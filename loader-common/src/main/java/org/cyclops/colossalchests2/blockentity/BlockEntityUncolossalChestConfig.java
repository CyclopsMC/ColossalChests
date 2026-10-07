package org.cyclops.colossalchests2.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.client.render.RenderUncolossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.Set;

/**
 * Config for the {@link BlockEntityUncolossalChest}.
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityUncolossalChest, M> {

    public BlockEntityUncolossalChestConfig(M mod, BlockEntityType.BlockEntitySupplier<? extends BlockEntityUncolossalChest> supplier) {
        super(
                mod,
                "uncolossal_chest",
                eConfig -> new BlockEntityType<>(supplier, Set.of(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value()), null)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            getMod().getProxy().registerRenderer(getInstance(), getRendererProvider());
        }
    }

    /**
     * Only called on the client.
     */
    protected BlockEntityRendererProvider<BlockEntityUncolossalChest> getRendererProvider() {
        return RenderUncolossalChest::new;
    }

}
