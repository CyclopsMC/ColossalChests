package org.cyclops.colossalchests.block;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static ChestWallConfig _instance;

    /**
     * Make a new instance.
     */
    public ChestWallConfig() {
        super(
                ColossalChests._instance,
        	true,
            "chest_wall",
            null,
            ChestWall.class
        );
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockMaterial.class;
    }
    
    @Override
    public boolean isMultipartEnabled() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onInit(Step step) {
        super.onInit(step);
        if(step == Step.INIT) {
            ColossalChestConfig.onInit(step, this);
        }
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        return super.getModelName(itemStack) + ColossalChestConfig.getModelNameSuffix(itemStack);
    }
    
}
