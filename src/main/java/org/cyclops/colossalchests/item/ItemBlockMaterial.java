package org.cyclops.colossalchests.item;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.cyclops.colossalchests.block.ChestMaterial;

import java.util.List;

import net.minecraft.item.Item.Properties;

/**
 * @author rubensworks
 */
public class ItemBlockMaterial extends BlockItem {

    private final ChestMaterial material;

    public ItemBlockMaterial(Block block, Properties builder, ChestMaterial material) {
        super(block, builder);
        this.material = material;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(new TranslationTextComponent(material.getUnlocalizedName()).withStyle(TextFormatting.BLUE));
        super.appendHoverText(itemStack, world, list, flag);

    }
}
