package org.cyclops.colossalchests2.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.block.BlockChestCore;
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
                eConfig -> new BlockEntityType<>(supplier, Set.copyOf(BlockChestCore.getInstances()), null) {
                    // Also accept cores of added materials, which may be registered later.
                    @Override
                    public boolean isValid(BlockState state) {
                        return state.getBlock() instanceof BlockChestCore;
                    }
                }
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
        }
    }

}
