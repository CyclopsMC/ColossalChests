package org.cyclops.colossalchests2.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.material.ItemMaterialUpgradeTool;

/**
 * Picks the material of a held Material Upgrade Tool. Button ids are indexes in {@link ChestMaterial#getAll()}.
 * @author rubensworks
 */
public class ContainerMaterialUpgradeTool extends AbstractContainerMenu {

    private final Player player;
    private final InteractionHand hand;
    private final DataSlot target;

    /**
     * Client-side constructor.
     */
    public ContainerMaterialUpgradeTool(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readEnum(InteractionHand.class));
    }

    public ContainerMaterialUpgradeTool(int id, Inventory inventory, InteractionHand hand) {
        super(RegistryEntries.MENU_MATERIAL_UPGRADE_TOOL.value(), id);
        this.player = inventory.player;
        this.hand = hand;
        this.target = addDataSlot(new DataSlot() {
            private int value = -1;

            @Override
            public int get() {
                return player.level().isClientSide ? value : ItemMaterialUpgradeTool.getTarget(getTool())
                        .map(ChestMaterial.getAll()::indexOf).orElse(-1);
            }

            @Override
            public void set(int value) {
                this.value = value;
            }
        });
    }

    private ItemStack getTool() {
        return player.getItemInHand(hand);
    }

    /**
     * @return The index of the chosen material, or -1 if none is chosen.
     */
    public int getTarget() {
        return target.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack tool = getTool();
        if (id < 0 || id >= ChestMaterial.getAll().size() || !(tool.getItem() instanceof ItemMaterialUpgradeTool)) {
            return false;
        }
        ItemMaterialUpgradeTool.setTarget(tool, ChestMaterial.getAll().get(id));
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return getTool().getItem() instanceof ItemMaterialUpgradeTool;
    }
}
