package org.cyclops.colossalchests.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.stream.Collectors;

/**
 * Config for the {@link BlockEntityColossalChest}.
 * @author rubensworks
 *
 */
public class BlockEntityColossalChestConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityColossalChest, M> {

    public BlockEntityColossalChestConfig(M mod) {
        super(
                mod,
                "colossal_chest",
                (eConfig) -> new BlockEntityType<>(((BlockEntityColossalChestConfig) eConfig).getBlockEntitySupplier(),
                        ChestMaterial.VALUES.stream()
                                .map(ChestMaterial::getBlockCore)
                                .collect(Collectors.toSet()), null)
        );
    }

    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityColossalChest> getBlockEntitySupplier() {
        return BlockEntityColossalChest::new;
    }

    protected BlockEntityRendererProvider<BlockEntityColossalChest> getBlockEntityRendererProvider() {
        return RenderTileEntityColossalChest::new;
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            getMod().getProxy().registerRenderer(getInstance(), getBlockEntityRendererProvider());
        }
    }

}
