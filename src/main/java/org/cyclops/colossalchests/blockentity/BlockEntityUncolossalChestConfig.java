package org.cyclops.colossalchests.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityUncolossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;

/**
 * Config for the {@link BlockEntityUncolossalChest}.
 * @author rubensworks
 *
 */
public class BlockEntityUncolossalChestConfig extends BlockEntityConfig<BlockEntityUncolossalChest> {

    public BlockEntityUncolossalChestConfig() {
        super(
                ColossalChests._instance,
                "uncolossal_chest",
                (eConfig) -> new BlockEntityType<>(BlockEntityUncolossalChest::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ColossalChests._instance.getProxy().registerRenderer(getInstance(), RenderTileEntityUncolossalChest::new);
    }

}
