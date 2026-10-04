package org.cyclops.colossalchests2.upgrade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An item that installs a core upgrade when placed in a chest's upgrade slots.
 * @author rubensworks
 */
public class ItemChestUpgrade extends Item {

    private final ChestUpgrade upgrade;

    public ItemChestUpgrade(Properties properties, ChestUpgrade upgrade) {
        super(properties);
        this.upgrade = upgrade;
    }

    public ChestUpgrade getUpgrade() {
        return upgrade;
    }

    @Nullable
    public static ChestUpgrade getUpgrade(ItemStack stack) {
        return stack.getItem() instanceof ItemChestUpgrade item ? item.getUpgrade() : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".info", upgrade.getInfoArguments()).withStyle(ChatFormatting.GRAY));
    }

}
