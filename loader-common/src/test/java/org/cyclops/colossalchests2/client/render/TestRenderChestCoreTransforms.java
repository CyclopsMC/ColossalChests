package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.cyclops.colossalchests2.api.client.IChestOverlay;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.joml.Vector3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Checks that overlays and the chest model land where the {@link IChestOverlay} contract says.
 * @author rubensworks
 */
public class TestRenderChestCoreTransforms {

    private static final float EPSILON = 0.0001F;
    private static final float OUT = RenderChestCore.OVERLAY_OFFSET;
    private static final ChestStructure STRUCTURE = new ChestStructure(BlockPos.ZERO, 3);

    private static Vector3f transform(PoseStack poseStack, float x, float y, float z) {
        return poseStack.last().pose().transformPosition(x, y, z, new Vector3f());
    }

    private static void assertPoint(float x, float y, float z, Vector3f actual) {
        assertEquals("x of " + actual, x, actual.x, EPSILON);
        assertEquals("y of " + actual, y, actual.y, EPSILON);
        assertEquals("z of " + actual, z, actual.z, EPSILON);
    }

    private static PoseStack face(Direction face, Direction facing) {
        PoseStack poseStack = new PoseStack();
        RenderChestCore.applyFaceTransform(poseStack, face, facing);
        return poseStack;
    }

    @Test
    public void testFaceNorth() {
        // Seen from the north, left is east.
        PoseStack poseStack = face(Direction.NORTH, Direction.NORTH);
        assertPoint(1, 0, -OUT, transform(poseStack, 0, 0, 0));
        assertPoint(0, 1, -OUT, transform(poseStack, 1, 1, 0));
    }

    @Test
    public void testFaceSouth() {
        PoseStack poseStack = face(Direction.SOUTH, Direction.NORTH);
        assertPoint(0, 0, 1 + OUT, transform(poseStack, 0, 0, 0));
        assertPoint(1, 1, 1 + OUT, transform(poseStack, 1, 1, 0));
    }

    @Test
    public void testFaceEastAndWest() {
        assertPoint(1 + OUT, 0, 1, transform(face(Direction.EAST, Direction.NORTH), 0, 0, 0));
        assertPoint(1 + OUT, 1, 0, transform(face(Direction.EAST, Direction.NORTH), 1, 1, 0));
        assertPoint(-OUT, 0, 0, transform(face(Direction.WEST, Direction.NORTH), 0, 0, 0));
        assertPoint(-OUT, 1, 1, transform(face(Direction.WEST, Direction.NORTH), 1, 1, 0));
    }

    @Test
    public void testFaceUpPointsAwayFromFront() {
        // A south-facing chest: up on the top face is north.
        PoseStack poseStack = face(Direction.UP, Direction.SOUTH);
        assertPoint(0, 1 + OUT, 1, transform(poseStack, 0, 0, 0));
        assertPoint(0, 1 + OUT, 0, transform(poseStack, 0, 1, 0));
        assertPoint(1, 1 + OUT, 1, transform(poseStack, 1, 0, 0));
        // An east-facing chest: up on the top face is west, so right is north.
        assertPoint(1, 1 + OUT, 0, transform(face(Direction.UP, Direction.EAST), 1, 0, 0));
        assertPoint(0, 1 + OUT, 1, transform(face(Direction.UP, Direction.EAST), 0, 1, 0));
    }

    @Test
    public void testFaceDownPointsToFront() {
        PoseStack poseStack = face(Direction.DOWN, Direction.SOUTH);
        assertPoint(0, -OUT, 0, transform(poseStack, 0, 0, 0));
        assertPoint(0, -OUT, 1, transform(poseStack, 0, 1, 0));
    }

    @Test
    public void testChestModelFillsStructure() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            PoseStack poseStack = new PoseStack();
            // Relative to a core that is not at the structure corner.
            RenderChestCore.applyChestTransform(poseStack, new BlockPos(1, 1, 0), STRUCTURE, facing);
            // The model body spans pixels 1 to 15 horizontally and 0 to 14 vertically.
            Vector3f a = transform(poseStack, 1F / 16, 0, 1F / 16);
            Vector3f b = transform(poseStack, 15F / 16, 14F / 16, 15F / 16);
            assertEquals(-1, Math.min(a.x, b.x), EPSILON);
            assertEquals(2, Math.max(a.x, b.x), EPSILON);
            assertEquals(-1, a.y, EPSILON);
            assertEquals(2, b.y, EPSILON);
            assertEquals(0, Math.min(a.z, b.z), EPSILON);
            assertEquals(3, Math.max(a.z, b.z), EPSILON);
        }
    }

    @Test
    public void testChestModelFrontFacesFacing() {
        // The model's lock side is at pixel z 15.
        PoseStack poseStack = new PoseStack();
        RenderChestCore.applyChestTransform(poseStack, BlockPos.ZERO, STRUCTURE, Direction.EAST);
        assertEquals(3, transform(poseStack, 0.5F, 0, 15F / 16).x, EPSILON);
    }

    @Test
    public void testLidTransformKeepsHingeAndLiftsFront() {
        PoseStack poseStack = new PoseStack();
        RenderChestCore.applyLidTransform(poseStack, BlockPos.ZERO, STRUCTURE, Direction.SOUTH, -(float) Math.PI / 2);
        float hingeY = 3 * 9F / 14;
        // The hinge is the top of the back (north) side.
        assertPoint(1, hingeY, 0, transform(poseStack, 1, hingeY, 0));
        // The front top edge swings up and over the back.
        assertPoint(1, hingeY + 3, -(3 - hingeY), transform(poseStack, 1, 3, 3));
    }

    @Test
    public void testLidTransformClosedIsIdentity() {
        PoseStack poseStack = new PoseStack();
        RenderChestCore.applyLidTransform(poseStack, new BlockPos(1, 1, 0), STRUCTURE, Direction.WEST, 0);
        assertPoint(0.3F, 1.7F, 2.9F, transform(poseStack, 0.3F, 1.7F, 2.9F));
    }

}
