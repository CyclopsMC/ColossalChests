package org.cyclops.colossalchests2.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.client.render.ChestOverlays;
import org.cyclops.colossalchests2.client.render.CoreMarkerOverlay;
import org.cyclops.colossalchests2.client.render.RenderChestCore;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.Set;

/**
 * Config for the {@link BlockEntityChestCore}.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityChestCore, M> {

    public BlockEntityChestCoreConfig(M mod, BlockEntityType.BlockEntitySupplier<? extends BlockEntityChestCore> supplier) {
        super(
                mod,
                "chest_core",
                eConfig -> new BlockEntityType<>(supplier, Set.copyOf(BlockChestCore.getInstances()), null)
        );
    }

    /**
     * Only called on the client.
     * @return The renderer of the giant chest.
     */
    protected BlockEntityRendererProvider<BlockEntityChestCore> getRendererProvider() {
        return RenderChestCore::new;
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            getMod().getProxy().registerRenderer(getInstance(), getRendererProvider());
            for (BlockChestCore core : BlockChestCore.getInstances()) {
                ChestOverlays.register(core, CoreMarkerOverlay.INSTANCE);
            }
        }
    }

}
