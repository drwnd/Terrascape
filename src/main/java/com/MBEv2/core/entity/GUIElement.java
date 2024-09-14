package com.MBEv2.core.entity;

import com.MBEv2.core.Block;
import com.MBEv2.core.WindowManager;
import com.MBEv2.test.Launcher;
import org.joml.Vector2f;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Constants.LEFT;
import static com.MBEv2.core.utils.Settings.GUI_SIZE;

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

        final int textureFrontX = textureIndexFront & 15;
        final int textureFrontY = (textureIndexFront >> 4) & 15;
        final int textureTopX = textureIndexTop & 15;
        final int textureTopY = (textureIndexTop >> 4) & 15;
        final int textureLeftX = textureIndexRight & 15;
        final int textureLeftY = (textureIndexRight >> 4) & 15;

        for (int aabbIndex = 0; aabbIndex < XYZSubData.length; aabbIndex += 6) {
            int offset = aabbIndex * 6;
            int subDataIndex = aabbIndex / 6;

            final float upperFrontX = (textureFrontX + 1 + Block.getSubU(blockType, FRONT, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontX = (textureFrontX + Block.getSubU(blockType, FRONT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperFrontY = (textureFrontY + Block.getSubV(blockType, FRONT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerFrontY = (textureFrontY + 1 + Block.getSubV(blockType, FRONT, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset] = lowerFrontX;
            textureCoordinates[offset + 1] = lowerFrontY;
            textureCoordinates[offset + 2] = lowerFrontX;
            textureCoordinates[offset + 3] = upperFrontY;
            textureCoordinates[offset + 4] = upperFrontX;
            textureCoordinates[offset + 5] = lowerFrontY;
            textureCoordinates[offset + 6] = lowerFrontX;
            textureCoordinates[offset + 7] = upperFrontY;
            textureCoordinates[offset + 8] = upperFrontX;
            textureCoordinates[offset + 9] = upperFrontY;
            textureCoordinates[offset + 10] = upperFrontX;
            textureCoordinates[offset + 11] = lowerFrontY;

            final float upperTopX = (textureTopX + 1 + Block.getSubU(blockType, TOP, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerTopX = (textureTopX + Block.getSubU(blockType, TOP, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperTopY = (textureTopY + Block.getSubV(blockType, TOP, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerTopY = (textureTopY + 1 + Block.getSubV(blockType, TOP, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 12] = lowerTopX;
            textureCoordinates[offset + 13] = lowerTopY;
            textureCoordinates[offset + 14] = lowerTopX;
            textureCoordinates[offset + 15] = upperTopY;
            textureCoordinates[offset + 16] = upperTopX;
            textureCoordinates[offset + 17] = lowerTopY;
            textureCoordinates[offset + 18] = lowerTopX;
            textureCoordinates[offset + 19] = upperTopY;
            textureCoordinates[offset + 20] = upperTopX;
            textureCoordinates[offset + 21] = upperTopY;
            textureCoordinates[offset + 22] = upperTopX;
            textureCoordinates[offset + 23] = lowerTopY;

            final float upperRightX = (textureLeftX + Block.getSubU(blockType, LEFT, 0, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerRightX = (textureLeftX + 1 + Block.getSubU(blockType, LEFT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float upperRightY = (textureLeftY + Block.getSubV(blockType, LEFT, 1, subDataIndex) * 0.0625f) * 0.0625f;
            final float lowerRightY = (textureLeftY + 1 + Block.getSubV(blockType, LEFT, 2, subDataIndex) * 0.0625f) * 0.0625f;
            textureCoordinates[offset + 24] = lowerRightX;
            textureCoordinates[offset + 25] = lowerRightY;
            textureCoordinates[offset + 26] = lowerRightX;
            textureCoordinates[offset + 27] = upperRightY;
            textureCoordinates[offset + 28] = upperRightX;
            textureCoordinates[offset + 29] = lowerRightY;
            textureCoordinates[offset + 30] = lowerRightX;
            textureCoordinates[offset + 31] = upperRightY;
            textureCoordinates[offset + 32] = upperRightX;
            textureCoordinates[offset + 33] = upperRightY;
            textureCoordinates[offset + 34] = upperRightX;
            textureCoordinates[offset + 35] = lowerRightY;
        }
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
}
