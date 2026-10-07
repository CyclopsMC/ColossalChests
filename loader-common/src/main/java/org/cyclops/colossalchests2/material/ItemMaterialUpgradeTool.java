package org.cyclops.colossalchests2.material;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.block.IChestMember;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.ChestInteractions;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.inventory.ContainerMaterialUpgradeTool;
import org.cyclops.cyclopscore.helper.IModHelpers;

import java.util.List;
import java.util.Optional;

/**
 * Changes a formed chest to the material chosen in the tool's GUI, opened by using it on anything else.
 * It swaps walls like breaking and replacing them by hand: walls of the new material are taken from the
 * player and the old walls are given back.
 * @author rubensworks
 */
public class ItemMaterialUpgradeTool extends Item {

    public ItemMaterialUpgradeTool(Properties properties) {
        super(properties);
    }

    public static Optional<ChestMaterial> getTarget(ItemStack stack) {
        ResourceLocation id = stack.get(RegistryEntries.COMPONENT_MATERIAL_TARGET.value());
        return id == null ? Optional.empty() : ChestMaterial.byId(id);
    }

    public static void setTarget(ItemStack stack, ChestMaterial material) {
        stack.set(RegistryEntries.COMPONENT_MATERIAL_TARGET.value(), material.id());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (player == null || !(state.getBlock() instanceof IChestMember || state.getBlock() instanceof BlockChestCore)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Optional<ChestMaterial> target = getTarget(context.getItemInHand());
        Optional<BlockEntityChestCore> core = ChestInteractions.findFormedCore(state, level, pos);
        MaterialChanges.Result result;
        if (target.isEmpty()) {
            result = MaterialChanges.Result.fail(Component.translatable("chest.colossalchests2.material_change.no_target"));
        } else if (core.isEmpty()) {
            result = MaterialChanges.Result.fail(Component.translatable("chest.colossalchests2.material_change.not_formed"));
        } else {
            result = MaterialChanges.change(player, core.get(), target.get());
        }
        player.displayClientMessage(result.message(), true);
        return result.success() ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            IModHelpers.get().getMinecraftHelpers().openMenu(serverPlayer,
                    new SimpleMenuProvider((id, inventory, p) -> new ContainerMaterialUpgradeTool(id, inventory, hand), stack.getHoverName()),
                    buf -> buf.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(getTarget(stack)
                .map(material -> Component.translatable("item.colossalchests2.material_upgrade_tool.target", material.getDisplayName()))
                .orElse(Component.translatable("item.colossalchests2.material_upgrade_tool.no_target"))
                .withStyle(ChatFormatting.GRAY));
    }

}
