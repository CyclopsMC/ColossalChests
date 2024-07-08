package org.cyclops.colossalchests.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
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
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable(material.getUnlocalizedName()).withStyle(ChatFormatting.BLUE));
        super.appendHoverText(itemStack, context, list, flag);

    }
}
