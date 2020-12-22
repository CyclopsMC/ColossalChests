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
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(new TranslationTextComponent(material.getUnlocalizedName()).mergeStyle(TextFormatting.BLUE));
        super.addInformation(itemStack, world, list, flag);

    }
}
