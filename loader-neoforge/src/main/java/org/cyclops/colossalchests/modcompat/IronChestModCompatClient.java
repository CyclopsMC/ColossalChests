package org.cyclops.colossalchests.modcompat;

import net.minecraft.resources.Identifier;
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
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "copper_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.IRON,
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "iron_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.SILVER,
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "silver_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.GOLD,
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "gold_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.DIAMOND,
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "diamond_chest"));
        RenderTileEntityColossalChest.TEXTURES_CHEST.put(ChestMaterial.OBSIDIAN,
                Identifier.fromNamespaceAndPath(Reference.MOD_IRONCHEST, prefix + "obsidian_chest"));
    }

}
