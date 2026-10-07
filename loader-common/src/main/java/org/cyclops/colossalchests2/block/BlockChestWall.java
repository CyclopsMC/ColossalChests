package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.block.ChestMemberBlock;
import org.cyclops.colossalchests2.multiblock.StructureView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A plain wall of a material, or the base of this mod's functional walls.
 * Addons create plain walls with {@link org.cyclops.colossalchests2.api.IColossalChestsApi#createWall},
 * and make functional walls with {@link ChestMemberBlock}.
 * @author rubensworks
 */
public class BlockChestWall extends ChestMemberBlock {

    private static final List<BlockChestWall> INSTANCES = Lists.newArrayList();

    @Nullable
    private final ChestMaterial material;

    /**
     * A plain wall of a material.
     */
    public BlockChestWall(Properties properties, ChestMaterial material) {
        this(properties, material, true);
    }

    /**
     * A wall without a material, which fits chests of any material.
     */
    public BlockChestWall(Properties properties) {
        this(properties, null, false);
    }

    protected BlockChestWall(Properties properties, @Nullable ChestMaterial material, boolean plain) {
        super(properties);
        this.material = material;
        if (plain) {
            INSTANCES.add(this);
        }
    }

    /**
     * @return The plain walls.
     */
    public static List<BlockChestWall> getInstances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (material != null) {
            tooltip.add(material.getLimitsTooltip());
        }
    }

    /**
     * @return The material of a plain wall, null for functional walls.
     */
    @Nullable
    public ChestMaterial getMaterial() {
        return material;
    }

    /**
     * @return If this is a plain wall, which has a material and no function.
     */
    public boolean isPlain() {
        return material != null;
    }

    /**
     * @return The material id for structure detection.
     */
    public Object getMemberMaterial() {
        return material == null ? StructureView.Member.ANY_MATERIAL : material.id();
    }

}
