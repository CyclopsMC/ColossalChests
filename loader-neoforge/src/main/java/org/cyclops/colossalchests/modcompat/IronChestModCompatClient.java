package org.cyclops.colossalchests.modcompat;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChest;

/**
 * @author rubensworks
 */
public class IronChestModCompatClient {

    public static void overrideTextures() {
        String prefix = "model/";
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.COPPER,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "copper_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.IRON,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "iron_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.SILVER,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "silver_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.GOLD,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "gold_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.DIAMOND,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "diamond_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.OBSIDIAN,
                ResourceLocation.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "obsidian_chest"));
    }

}
