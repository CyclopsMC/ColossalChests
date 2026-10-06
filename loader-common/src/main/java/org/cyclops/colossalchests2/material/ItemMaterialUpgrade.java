package org.cyclops.colossalchests2.material;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.ChestInteractions;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.config.MaterialCost;

import java.util.List;
import java.util.Optional;

/**
 * Turns a formed chest of the previous material into this item's material. Sneaking turns it back.
 * The item is a tool, it is not used up.
 * @author rubensworks
 */
public class ItemMaterialUpgrade extends Item {

    private final ChestMaterial from;
    private final ChestMaterial to;

    public ItemMaterialUpgrade(Properties properties, ChestMaterial to) {
        super(properties);
        this.from = to.previous().orElseThrow(() -> new IllegalArgumentException("No material below " + to.id()));
        this.to = to;
    }

    public ChestMaterial getFrom() {
        return from;
    }

    public ChestMaterial getTo() {
        return to;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (player == null || !(state.getBlock() instanceof BlockChestWall || state.getBlock() instanceof BlockChestCore)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean upgrade = !player.isSecondaryUseActive();
        Optional<BlockEntityChestCore> core = ChestInteractions.findFormedCore(state, level, pos);
        Component message;
        if (core.isEmpty()) {
            message = Component.translatable("chest.colossalchests2.material_change.not_formed");
        } else if (!(core.get().getBlockState().getBlock() instanceof BlockChestCore coreBlock)
                || coreBlock.getMaterial() != (upgrade ? from : to)) {
            message = Component.translatable("chest.colossalchests2.material_change.wrong_material." + (upgrade ? "upgrade" : "downgrade"),
                    from.getDisplayName(), to.getDisplayName());
        } else {
            MaterialChanges.Result result = MaterialChanges.change(player, core.get(), upgrade);
            player.displayClientMessage(result.message(), true);
            return result.success() ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }
        player.displayClientMessage(message, true);
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.colossalchests2.material_upgrade.effect", from.getDisplayName(), to.getDisplayName())
                .withStyle(ChatFormatting.GRAY));
        for (MaterialCost cost : to.getProperties().upgradeCost()) {
            Component name = MaterialChanges.getItem(cost).map(Item::getDescription).orElse(Component.literal(cost.item().toString()));
            tooltip.add(Component.translatable("item.colossalchests2.material_upgrade.cost", cost.count(), name)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

}
