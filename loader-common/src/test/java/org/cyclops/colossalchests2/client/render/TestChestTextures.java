package org.cyclops.colossalchests2.client.render;

import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestTextures extends BootstrapTest {

    @Test
    public void testLidAndBaseMatchWhereTheyOverlap() throws IOException {
        // The closed lid overlaps the base by one pixel with coplanar sides, so differing texels there z-fight.
        // Textures are upside down: that pixel is the first row of the lid's sides and the last row of the base's.
        for (ChestMaterial material : ChestMaterial.BUILT_IN) {
            if (material == ChestMaterial.WOOD) {
                continue; // Uses the vanilla texture.
            }
            String path = "/assets/colossalchests2/textures/entity/chest/" + material.id().getPath() + ".png";
            try (InputStream stream = TestChestTextures.class.getResourceAsStream(path)) {
                assertNotNull(path, stream);
                BufferedImage image = ImageIO.read(stream);
                int scale = image.getWidth() / 64;
                for (int u = 0; u < 56 * scale; u++) {
                    assertEquals(path + " at u " + u, image.getRGB(u, 14 * scale), image.getRGB(u, 43 * scale - 1));
                }
            }
        }
    }

}
