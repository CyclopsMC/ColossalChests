package org.cyclops.colossalchests2.client.render;

import org.cyclops.colossalchests2.block.ChestMaterial;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestTextures {

    @Test
    public void testLidAndBaseMatchWhereTheyOverlap() throws IOException {
        // The closed lid overlaps the base by one pixel with coplanar sides, so differing texels there z-fight.
        for (ChestMaterial material : ChestMaterial.VALUES) {
            if (material == ChestMaterial.WOOD) {
                continue; // Uses the vanilla texture.
            }
            String path = "/assets/colossalchests2/textures/entity/chest/" + material.id().getPath() + ".png";
            try (InputStream stream = TestChestTextures.class.getResourceAsStream(path)) {
                assertNotNull(path, stream);
                BufferedImage image = ImageIO.read(stream);
                int scale = image.getWidth() / 64;
                for (int u = 0; u < 56 * scale; u++) {
                    assertEquals(path + " at u " + u, image.getRGB(u, 19 * scale - 1), image.getRGB(u, 33 * scale));
                }
            }
        }
    }

}
