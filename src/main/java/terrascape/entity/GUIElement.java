package terrascape.entity;

import terrascape.core.Block;
import terrascape.core.ObjectLoader;
import terrascape.core.WindowManager;
import terrascape.core.Launcher;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Constants.EAST;
import static terrascape.utils.Settings.GUI_SIZE;

public class GUIElement {

    private final int vao, vertexCount;
    private final int vbo1, vbo2;
    private Texture texture;
    private Vector2f position;

    public GUIElement(int vao, int vertexCount, int vbo1, int vbo2, Vector2f position) {
        this.vao = vao;
        this.vbo1 = vbo1;
        this.vbo2 = vbo2;
        this.vertexCount = vertexCount;
        this.position = position;
    }

    public int getVao() {
        return vao;
    }

    public int getVbo1() {
        return vbo1;
    }

    public int getVbo2() {
        return vbo2;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Vector2f getPosition() {
        return position;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public static float[] getCrossHairVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float size = 16;

        return new float[]{-size * GUI_SIZE / width, size * GUI_SIZE / height,
                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height,

                -size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, -size * GUI_SIZE / height,
                size * GUI_SIZE / width, size * GUI_SIZE / height};
    }

    public static float[] getBlockDisplayVertices(short block) {
        if (block == AIR) return new float[]{};
        WindowManager window = Launcher.getWindow();

        final int width = window.getWidth();
        final int height = window.getHeight();

        final float sin30 = (float) Math.sin(Math.toRadians(30)) * GUI_SIZE;
        final float cos30 = (float) Math.cos(Math.toRadians(30)) * GUI_SIZE;

        byte[] XYZSubData = Block.getXYZSubData(block);
        float[] vertices = new float[XYZSubData.length * 6];

        for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
            int offset = aabbIndex * 6;

            float widthX = XYZSubData[MAX_X + aabbIndex] - XYZSubData[MIN_X + aabbIndex] + 16;
            float widthY = (XYZSubData[MAX_Y + aabbIndex] - XYZSubData[MIN_Y + aabbIndex] + 16) * GUI_SIZE;
            float widthZ = XYZSubData[MAX_Z + aabbIndex] - XYZSubData[MIN_Z + aabbIndex] + 16;

            float startX = -XYZSubData[MAX_X + aabbIndex] * cos30 / width + XYZSubData[MAX_Z + aabbIndex] * cos30 / width;
            float startY = XYZSubData[MIN_Y + aabbIndex] * GUI_SIZE / height - XYZSubData[MAX_X + aabbIndex] * sin30 / height - XYZSubData[MAX_Z + aabbIndex] * sin30 / height;

            float rightCornersX = startX + cos30 * widthX / width;
            float leftCornersX = startX - cos30 * widthZ / width;
            float backCornerX = rightCornersX + leftCornersX - startX;

            float bottomRightCornerY = startY + sin30 * widthX / height;
            float topRightCornerY = startY + widthY / height + sin30 * widthX / height;
            float bottomLeftCornerY = startY + sin30 * widthZ / height;
            float topLeftCornerY = startY + widthY / height + sin30 * widthZ / height;
            float backCornerY = startY + widthY / height + sin30 * widthZ / height + sin30 * widthX / height;
            float centerCornerY = startY + widthY / height;

            vertices[offset] = startX;
            vertices[offset + 1] = startY;
            vertices[offset + 2] = startX;
            vertices[offset + 3] = centerCornerY;
            vertices[offset + 4] = rightCornersX;
            vertices[offset + 5] = bottomRightCornerY;
            vertices[offset + 6] = startX;
            vertices[offset + 7] = centerCornerY;
            vertices[offset + 8] = rightCornersX;
            vertices[offset + 9] = topRightCornerY;
            vertices[offset + 10] = rightCornersX;
            vertices[offset + 11] = bottomRightCornerY;

            vertices[offset + 12] = leftCornersX;
            vertices[offset + 13] = topLeftCornerY;
            vertices[offset + 14] = backCornerX;
            vertices[offset + 15] = backCornerY;
            vertices[offset + 16] = startX;
            vertices[offset + 17] = centerCornerY;
            vertices[offset + 18] = backCornerX;
            vertices[offset + 19] = backCornerY;
            vertices[offset + 20] = rightCornersX;
            vertices[offset + 21] = topRightCornerY;
            vertices[offset + 22] = startX;
            vertices[offset + 23] = centerCornerY;

            vertices[offset + 24] = startX;
            vertices[offset + 25] = startY;
            vertices[offset + 26] = startX;
            vertices[offset + 27] = centerCornerY;
            vertices[offset + 28] = leftCornersX;
            vertices[offset + 29] = bottomLeftCornerY;
            vertices[offset + 30] = startX;
            vertices[offset + 31] = centerCornerY;
            vertices[offset + 32] = leftCornersX;
            vertices[offset + 33] = topLeftCornerY;
            vertices[offset + 34] = leftCornersX;
            vertices[offset + 35] = bottomLeftCornerY;
        }
        return vertices;
    }

    public static float[] getBlockDisplayTextureCoordinates(int textureIndexFront, int textureIndexTop, int textureIndexRight, short block) {
        if (block == AIR) return new float[]{};
        int blockType = Block.getBlockType(block);
        byte[] XYZSubData = Block.getXYZSubData(block);
        float[] textureCoordinates = new float[XYZSubData.length * 6];

        final int textureFrontU = textureIndexFront & 15;
        final int textureFrontV = (textureIndexFront >> 4) & 15;
        final int textureTopU = textureIndexTop & 15;
        final int textureTopV = (textureIndexTop >> 4) & 15;
        final int textureLeftU = textureIndexRight & 15;
        final int textureLeftV = (textureIndexRight >> 4) & 15;

        for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
            int offset = aabbIndex * 6;

            final float upperFrontU = (textureFrontU + 1 + Block.getSubU(blockType, NORTH, 0, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontU = (textureFrontU + Block.getSubU(blockType, NORTH, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float upperFrontV = (textureFrontV + Block.getSubV(blockType, NORTH, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontV = (textureFrontV + 1 + Block.getSubV(blockType, NORTH, 2, aabbIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset] = lowerFrontU;
            textureCoordinates[offset + 1] = lowerFrontV;
            textureCoordinates[offset + 2] = lowerFrontU;
            textureCoordinates[offset + 3] = upperFrontV;
            textureCoordinates[offset + 4] = upperFrontU;
            textureCoordinates[offset + 5] = lowerFrontV;
            textureCoordinates[offset + 6] = lowerFrontU;
            textureCoordinates[offset + 7] = upperFrontV;
            textureCoordinates[offset + 8] = upperFrontU;
            textureCoordinates[offset + 9] = upperFrontV;
            textureCoordinates[offset + 10] = upperFrontU;
            textureCoordinates[offset + 11] = lowerFrontV;

            final float upperTopU = (textureTopU + 1 + Block.getSubU(blockType, TOP, 0, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerTopU = (textureTopU + Block.getSubU(blockType, TOP, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float upperTopV = (textureTopV + Block.getSubV(blockType, TOP, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerTopV = (textureTopV + 1 + Block.getSubV(blockType, TOP, 2, aabbIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 12] = lowerTopU;
            textureCoordinates[offset + 13] = lowerTopV;
            textureCoordinates[offset + 14] = lowerTopU;
            textureCoordinates[offset + 15] = upperTopV;
            textureCoordinates[offset + 16] = upperTopU;
            textureCoordinates[offset + 17] = lowerTopV;
            textureCoordinates[offset + 18] = lowerTopU;
            textureCoordinates[offset + 19] = upperTopV;
            textureCoordinates[offset + 20] = upperTopU;
            textureCoordinates[offset + 21] = upperTopV;
            textureCoordinates[offset + 22] = upperTopU;
            textureCoordinates[offset + 23] = lowerTopV;

            final float upperRightU = (textureLeftU + Block.getSubU(blockType, EAST, 0, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerRightU = (textureLeftU + 1 + Block.getSubU(blockType, EAST, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float upperRightV = (textureLeftV + Block.getSubV(blockType, EAST, 1, aabbIndex) * 0.0625f) * 0.0625f;
            final float lowerRightV = (textureLeftV + 1 + Block.getSubV(blockType, EAST, 2, aabbIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 24] = lowerRightU;
            textureCoordinates[offset + 25] = lowerRightV;
            textureCoordinates[offset + 26] = lowerRightU;
            textureCoordinates[offset + 27] = upperRightV;
            textureCoordinates[offset + 28] = upperRightU;
            textureCoordinates[offset + 29] = lowerRightV;
            textureCoordinates[offset + 30] = lowerRightU;
            textureCoordinates[offset + 31] = upperRightV;
            textureCoordinates[offset + 32] = upperRightU;
            textureCoordinates[offset + 33] = upperRightV;
            textureCoordinates[offset + 34] = upperRightU;
            textureCoordinates[offset + 35] = lowerRightV;
        }
        return textureCoordinates;
    }

    public static float[] getFlatDisplayVertices() {
        float[] vertices = new float[12];

        WindowManager window = Launcher.getWindow();
        final int width = window.getWidth();
        final int height = window.getHeight();

        vertices[0] = 16 * -GUI_SIZE / width;
        vertices[1] = 0;
        vertices[2] = 16 * -GUI_SIZE / width;
        vertices[3] = 32 * GUI_SIZE / height;
        vertices[4] = 16 * GUI_SIZE / width;
        vertices[5] = 0;

        vertices[6] = 16 * -GUI_SIZE / width;
        vertices[7] = 32 * GUI_SIZE / height;
        vertices[8] = 16 * GUI_SIZE / width;
        vertices[9] = 32 * GUI_SIZE / height;
        vertices[10] = 16 * GUI_SIZE / width;
        vertices[11] = 0;

        return vertices;
    }

    public static float[] getFlatDisplayTextureCoordinates(int textureIndex) {
        float[] textureCoordinates = new float[12];

        final int u = textureIndex & 15;
        final int v = (textureIndex >> 4) & 15;

        final float upperFrontU = (u + 1) * 0.0625f;
        final float lowerFrontU = u * 0.0625f;
        final float upperFrontV = v * 0.0625f;
        final float lowerFrontV = (v + 1) * 0.0625f;
        textureCoordinates[0] = lowerFrontU;
        textureCoordinates[1] = lowerFrontV;
        textureCoordinates[2] = lowerFrontU;
        textureCoordinates[3] = upperFrontV;
        textureCoordinates[4] = upperFrontU;
        textureCoordinates[5] = lowerFrontV;
        textureCoordinates[6] = lowerFrontU;
        textureCoordinates[7] = upperFrontV;
        textureCoordinates[8] = upperFrontU;
        textureCoordinates[9] = upperFrontV;
        textureCoordinates[10] = upperFrontU;
        textureCoordinates[11] = lowerFrontV;

        return textureCoordinates;
    }

    public static float[] getHotBarVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();
        float sizeX = 180;
        float sizeY = 40;

        return new float[]{
                -sizeX * GUI_SIZE / width, -0.5f,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f,
                sizeX * GUI_SIZE / width, -0.5f
        };
    }

    public static float[] getHotBarSelectionIndicatorVertices() {
        WindowManager window = Launcher.getWindow();

        int width = window.getWidth();
        int height = window.getHeight();

        float sizeX = 24;
        float sizeY = 48;

        float yOffset = 4 * GUI_SIZE / height;

        return new float[]{
                -sizeX * GUI_SIZE / width, -0.5f - yOffset,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, -0.5f - yOffset,
                -sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, sizeY * GUI_SIZE / height - 0.5f - yOffset,
                sizeX * GUI_SIZE / width, -0.5f - yOffset
        };
    }

    public static short getHoveredOverBlock(float inventoryScroll) {
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        WindowManager window = Launcher.getWindow();
        GLFW.glfwGetCursorPos(window.getWindow(), xPos, yPos);
        double x = xPos[0] / window.getWidth() - 0.5;
        double y = yPos[0] / window.getHeight() - 0.5;

        y += inventoryScroll;

        if (y < -0.5f + GUI_SIZE * 0.04f) {
            if (y < -0.5) return 0;
            if (x < 0.5f - (TO_PLACE_NON_STANDARD_BLOCKS.length + 1) * 0.02f * GUI_SIZE) return 0;
            int value = (int) ((0.5 - 0.01 * GUI_SIZE - x) / (0.02 * GUI_SIZE));
            value = Math.min(TO_PLACE_NON_STANDARD_BLOCKS.length - 1, Math.max(value, 0));
            return TO_PLACE_NON_STANDARD_BLOCKS[value];
        }
        if (x < 0.5f - (TO_PLACE_BLOCK_TYPES.length + 1) * 0.02f * GUI_SIZE) return 0;
        if (y > -0.5f + GUI_SIZE * 0.04f * (AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS)) return 0;

        int valueX = (int) ((0.5 - 0.01 * GUI_SIZE - x) / (0.02 * GUI_SIZE));
        valueX = Math.min(TO_PLACE_BLOCK_TYPES.length - 1, Math.max(valueX, 0));

        int valueY = (int) ((y - 0.005 * GUI_SIZE + 0.5) / (0.04 * GUI_SIZE));
        valueY = Math.min(AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS - 1, Math.max(valueY, 1));

        return (short) (valueY << BLOCK_TYPE_BITS | TO_PLACE_BLOCK_TYPES[valueX]);
    }

    public static void generateInventoryElements(ArrayList<GUIElement> elements, Texture atlas) {
        for (int i = 0; i < TO_PLACE_NON_STANDARD_BLOCKS.length; i++) {
            short block = TO_PLACE_NON_STANDARD_BLOCKS[i];
            GUIElement element;
            if (Block.getBlockType(block) == FLOWER_TYPE) {
                int textureIndex = Block.getTextureIndex(block, 0);
                float[] textureCoordinates = GUIElement.getFlatDisplayTextureCoordinates(textureIndex);
                element = ObjectLoader.loadGUIElement(GUIElement.getFlatDisplayVertices(), textureCoordinates, new Vector2f(0.5f - (i + 1) * 0.02f * GUI_SIZE, 0.5f - GUI_SIZE * 0.04f));
            } else {
                int textureIndexFront = Block.getTextureIndex(block, NORTH);
                int textureIndexTop = Block.getTextureIndex(block, TOP);
                int textureIndexLeft = Block.getTextureIndex(block, EAST);
                float[] textureCoordinates = GUIElement.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
                element = ObjectLoader.loadGUIElement(GUIElement.getBlockDisplayVertices(block), textureCoordinates, new Vector2f(0.5f - (i + 1) * 0.02f * GUI_SIZE, 0.5f - GUI_SIZE * 0.04f));
            }
            element.setTexture(atlas);
            elements.add(element);
        }
        for (int baseBlock = 1; baseBlock < AMOUNT_OF_TO_PLACE_STANDARD_BLOCKS; baseBlock++) {
            for (int blockTypeIndex = 0; blockTypeIndex < TO_PLACE_BLOCK_TYPES.length; blockTypeIndex++) {
                int blockType = TO_PLACE_BLOCK_TYPES[blockTypeIndex];
                short block = (short) (baseBlock << BLOCK_TYPE_BITS | blockType);
                float[] vertices = GUIElement.getBlockDisplayVertices(block);

                int textureIndexFront = Block.getTextureIndex(block, NORTH);
                int textureIndexTop = Block.getTextureIndex(block, TOP);
                int textureIndexLeft = Block.getTextureIndex(block, EAST);
                float[] textureCoordinates = GUIElement.getBlockDisplayTextureCoordinates(textureIndexFront, textureIndexTop, textureIndexLeft, block);
                GUIElement element = ObjectLoader.loadGUIElement(vertices, textureCoordinates, new Vector2f(0.5f - (blockTypeIndex + 1) * 0.02f * GUI_SIZE, 0.5f - GUI_SIZE * 0.04f * (1 + baseBlock)));
                element.setTexture(atlas);
                elements.add(element);
            }
        }
    }
}
