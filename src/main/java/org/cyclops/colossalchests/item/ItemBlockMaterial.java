package org.cyclops.colossalchests.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.cyclopscore.item.ItemBlockMetadata;

import java.util.List;

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
    public void addInformation(ItemStack itemStack, World world, List<String> list, ITooltipFlag flag) {
        int materialIndex = itemStack.getItemDamage();
        if (materialIndex > PropertyMaterial.Type.values().length) {
            materialIndex = (materialIndex - 1) / 2;
        }
        list.add(TextFormatting.BLUE + PropertyMaterial.Type.values()[materialIndex].getLocalizedName());
        super.addInformation(itemStack, world, list, flag);

    }
}
