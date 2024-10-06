package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author rubensworks
 */
public class ColossalChestConfig<M extends IModBase> extends BlockConfigCommon<M> {

    @ConfigurablePropertyCommon(namedId="colossal_chest", category = "machine", comment = "The maximum size a colossal chest can have.", isCommandable = true, configLocation = ModConfigLocation.SERVER)
    public static int maxSize = 20;

    @ConfigurablePropertyCommon(namedId="colossal_chest", category = "general", comment = "If the chest should visually open when someone uses it.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static boolean chestAnimation = true;

    public ColossalChestConfig(M mod, String namedId, Function<BlockConfigCommon<M>, ? extends Block> blockConstructor, @Nullable BiFunction<BlockConfigCommon<M>, Block, ? extends Item> itemConstructor) {
        super(mod, namedId, blockConstructor, itemConstructor);
    }

    public Block.Properties getProperties() {
        return Block.Properties.of()
                .strength(5.0F)
                .sound(SoundType.WOOD)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false);
    }
}
