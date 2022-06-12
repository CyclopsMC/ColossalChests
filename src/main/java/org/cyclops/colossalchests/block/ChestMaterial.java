package org.cyclops.colossalchests.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.Vec3i;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.multi.AllowedBlock;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.CubeSizeValidator;
import org.cyclops.cyclopscore.block.multi.ExactBlockCountValidator;
import org.cyclops.cyclopscore.block.multi.HollowCubeDetector;
import org.cyclops.cyclopscore.block.multi.MaximumSizeValidator;
import org.cyclops.cyclopscore.block.multi.MinimumSizeValidator;

import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class ChestMaterial {

    public static final List<ChestMaterial> VALUES = Lists.newArrayList();
    public static final Map<String, ChestMaterial> KEYED_VALUES = Maps.newHashMap();

    public static final ChestMaterial WOOD = new ChestMaterial("wood", 1);
    public static final ChestMaterial COPPER = new ChestMaterial("copper", 1.666);
    public static final ChestMaterial IRON = new ChestMaterial("iron", 2);
    public static final ChestMaterial SILVER = new ChestMaterial("silver", 2.666);
    public static final ChestMaterial GOLD = new ChestMaterial("gold", 3);
    public static final ChestMaterial DIAMOND = new ChestMaterial("diamond", 4);
    public static final ChestMaterial OBSIDIAN = new ChestMaterial("obsidian", 4);

    private final String name;
    private final double inventoryMultiplier;
    private final int index;

    private ColossalChest blockCore;
    private Interface blockInterface;
    private ChestWall blockWall;
    private CubeDetector chestDetector = null;
    private MenuType<ContainerColossalChest> container;

    public ChestMaterial(String name, double inventoryMultiplier) {
        this.name = name;
        this.inventoryMultiplier = inventoryMultiplier;
        this.index = ChestMaterial.VALUES.size();
        ChestMaterial.VALUES.add(this);
        ChestMaterial.KEYED_VALUES.put(getName(), this);
    }

    public static ChestMaterial valueOf(String materialString) {
        return KEYED_VALUES.get(materialString.toLowerCase());
    }

    public String getName() {
        return name;
    }

    public double getInventoryMultiplier() {
        return this.inventoryMultiplier;
    }

    public String getUnlocalizedName() {
        return "material." + Reference.MOD_ID + "." + getName();
    }

    public boolean isExplosionResistant() {
        return this == OBSIDIAN;
    }

    public int ordinal() {
        return this.index;
    }

    public ColossalChest getBlockCore() {
        return blockCore;
    }

    public void setBlockCore(ColossalChest blockCore) {
        if (this.blockCore != null) {
            throw new IllegalStateException("Tried registering multiple core blocks for " + this.getName());
        }
        this.blockCore = blockCore;
    }

    public Interface getBlockInterface() {
        return blockInterface;
    }

    public void setBlockInterface(Interface blockInterface) {
        if (this.blockInterface != null) {
            throw new IllegalStateException("Tried registering multiple core blocks for " + this.getName());
        }
        this.blockInterface = blockInterface;
    }

    public ChestWall getBlockWall() {
        return blockWall;
    }

    public void setBlockWall(ChestWall blockWall) {
        if (this.blockWall != null) {
            throw new IllegalStateException("Tried registering multiple core blocks for " + this.getName());
        }
        this.blockWall = blockWall;
    }

    public void setContainer(MenuType<ContainerColossalChest> container) {
        if (this.container != null) {
            throw new IllegalStateException("Tried registering multiple containers for " + this.getName());
        }
        this.container = container;
    }

    public MenuType<ContainerColossalChest> getContainer() {
        return container;
    }

    public CubeDetector getChestDetector() {
        if (chestDetector == null) {
            chestDetector = new HollowCubeDetector(
                    new AllowedBlock[]{
                            new AllowedBlock(getBlockWall()),
                            new AllowedBlock(getBlockCore()).addCountValidator(new ExactBlockCountValidator(1)),
                            new AllowedBlock(getBlockInterface())
                    },
                    Lists.newArrayList(getBlockCore(), getBlockWall(), getBlockInterface())
            )
                    .addSizeValidator(new MinimumSizeValidator(new Vec3i(1, 1, 1)))
                    .addSizeValidator(new CubeSizeValidator())
                    .addSizeValidator(new MaximumSizeValidator(BlockEntityColossalChest.getMaxSize()) {
                        @Override
                        public Vec3i getMaximumSize() {
                            return BlockEntityColossalChest.getMaxSize();
                        }
                    });
        }
        return chestDetector;
    }

}
