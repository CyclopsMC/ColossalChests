package org.cyclops.colossalchests.block;

import net.minecraft.client.model.ModelChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.tileentity.RenderTileEntityColossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

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
    public static int maxSize = 11;

    /**
     * Make a new instance.
     */
    public ColossalChestConfig() {
        super(
                ColossalChests._instance,
        	true,
            "colossalChest",
            null,
            ColossalChest.class
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ModelChest model = new ModelChest();
        //ResourceLocation texture = new ResourceLocation(Reference.MOD_ID, Reference.TEXTURE_PATH_MODELS + "colossalChest.png"); TODO ?
        ResourceLocation texture = new ResourceLocation("textures/entity/chest/normal.png");
        ColossalChests._instance.getProxy().registerRenderer(TileColossalChest.class, new RenderTileEntityColossalChest(model, texture));
    }
    
}
