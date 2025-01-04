package org.cyclops.colossalchests.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.List;
import java.util.function.BiFunction;

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

    public static <M extends IModBase> BiFunction<BlockConfigCommon<M>, Block, ? extends Item> getItemConstructor(ChestMaterial material, String descriptionSuffix) {
        return (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, eConfig.getResourceKey().location()))
                .overrideDescription("block.colossalchests." + descriptionSuffix), material);
    }
}
