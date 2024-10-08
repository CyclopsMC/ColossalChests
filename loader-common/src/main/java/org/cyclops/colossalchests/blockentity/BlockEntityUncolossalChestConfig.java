package org.cyclops.colossalchests.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityUncolossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link BlockEntityUncolossalChest}.
 * @author rubensworks
 *
 */
public class BlockEntityUncolossalChestConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityUncolossalChest, M> {

    public BlockEntityUncolossalChestConfig(M mod) {
        super(
                mod,
                "uncolossal_chest",
                (eConfig) -> new BlockEntityType<>(BlockEntityUncolossalChest::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value()), null)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            getMod().getProxy().registerRenderer(getInstance(), RenderTileEntityUncolossalChest::new);
        }
    }
}
