package org.cyclops.colossalchests.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.item.ItemBlockMetadata;

import java.util.List;
import java.util.Locale;

/**
 * @author rubensworks
 */
public class ItemBlockMaterial extends ItemBlockMetadata {
    /**
     * Make a new instance.
     *
     * @param block The blockState instance.
     */
    public ItemBlockMaterial(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        list.add(EnumChatFormatting.BLUE + PropertyMaterial.Type.values()[itemStack.getItemDamage()].getLocalizedName());
        super.addInformation(itemStack, entityPlayer, list, par4);

    }
}
