package org.cyclops.colossalchests.gametest;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;
import org.cyclops.cyclopscore.gametest.GameTestLoaderHelpers;
import org.cyclops.colossalchests.Reference;

import java.util.Collection;

/**
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
public class GameTestsLoaderForge extends GameTestsCommon {
    @GameTestGenerator
    public Collection<TestFunction> generateCommonTests() throws InstantiationException, IllegalAccessException {
        return GameTestLoaderHelpers.generateCommonTests(Reference.MOD_ID, new Class[]{
                GameTestsCommon.class
        });
    }
}
