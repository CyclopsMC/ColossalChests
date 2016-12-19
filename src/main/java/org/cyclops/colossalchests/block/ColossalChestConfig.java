package org.cyclops.colossalchests.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.tileentity.RenderTileEntityColossalChest;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

import java.util.Locale;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class ColossalChestConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static ColossalChestConfig _instance;

    /**
     * The maximum size a colossal chest can have.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.MACHINE, comment = "The maximum size a colossal chest can have.", isCommandable = true)
    public static int maxSize = 20;

    /**
     * If the chest should visually open when someone uses it.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "If the chest should visually open when someone uses it.", isCommandable = true)
    public static boolean chestAnimation = true;

    /**
     * Make a new instance.
     */
    public ColossalChestConfig() {
        super(
                ColossalChests._instance,
        	true,
            "colossal_chest",
            null,
            ColossalChest.class
        );
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBlockMaterial.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ModelChest model = new ModelChest();
        ColossalChests._instance.getProxy().registerRenderer(TileColossalChest.class, new RenderTileEntityColossalChest(model));
    }

    @SideOnly(Side.CLIENT)
    public static void onInit(Step step, BlockConfig blockConfig) {
        for(PropertyMaterial.Type material : PropertyMaterial.Type.values()) {
            Item item = Item.getItemFromBlock(blockConfig.getBlockInstance());
            String modId = blockConfig.getMod().getModId();
            int meta = material.ordinal();
            String itemName = blockConfig.getModelName(new ItemStack(item, 1, meta));
            ModelBakery.registerItemVariants(item, new ModelResourceLocation(modId + ":" + itemName, "inventory"));
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta,
                    new ModelResourceLocation(modId + ":" + itemName, "inventory"));
        }
    }

    public static String getModelNameSuffix(ItemStack itemStack) {
        return "_" + PropertyMaterial.Type.values()[itemStack.getItemDamage()]
                .toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onInit(Step step) {
        super.onInit(step);
        if(step == Step.INIT) {
            onInit(step, this);
        }
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        return super.getModelName(itemStack) + getModelNameSuffix(itemStack);
    }
    
}
