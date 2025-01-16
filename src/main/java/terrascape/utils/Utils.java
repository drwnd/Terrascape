package terrascape.utils;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static terrascape.utils.Constants.MAX_CHUNKS_XZ;
import static terrascape.utils.Constants.MAX_CHUNKS_Y;
import static terrascape.utils.Settings.RENDERED_WORLD_HEIGHT;
import static terrascape.utils.Settings.RENDERED_WORLD_WIDTH;

public class Utils {

    public static FloatBuffer storeDateInFloatBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static IntBuffer storeDateInIntBuffer(int[] data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static int floor(float value) {
        int addend = value < 0 ? -1 : 0;
        return (int) value + addend;
    }

    public static int floor(double value) {
        int addend = value < 0 ? -1 : 0;
        return (int) value + addend;
    }

    public static float fraction(float number) {
        int addend = number < 0 ? 1 : 0;
        return (number - (int) number) + addend;
    }

    public static double smoothInOutQuad(double x, double lowBound, double highBound) {

        // Maps x ∈ [lowBound, highBound] to [0, 1]
        x -= lowBound;
        x /= highBound - lowBound;

        if (x < 0.5) return 2 * x * x;
        double oneMinusX = 1 - x;
        return 1 - 2 * oneMinusX * oneMinusX;
    }

    public static float absMax(float a, float b) {
        return Math.abs(a) > Math.abs(b) ? a : b;
    }

    public static float absMin(float a, float b) {
        return Math.abs(a) < Math.abs(b) ? a : b;
    }

    public static long getChunkId(int chunkX, int chunkY, int chunkZ) {
        return (long) (chunkX & MAX_CHUNKS_XZ) << 37 | (long) (chunkY & MAX_CHUNKS_Y) << 27 | chunkZ & MAX_CHUNKS_XZ;
    }

    public static int getChunkIndex(int chunkX, int chunkY, int chunkZ) {

        chunkX %= RENDERED_WORLD_WIDTH;
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkY %= RENDERED_WORLD_HEIGHT;
        if (chunkY < 0) chunkY += RENDERED_WORLD_HEIGHT;

        chunkZ %= RENDERED_WORLD_WIDTH;
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return (chunkX * RENDERED_WORLD_WIDTH + chunkZ) * RENDERED_WORLD_HEIGHT + chunkY;
    }

    public static int getHeightMapIndex(int chunkX, int chunkZ) {

        chunkX = (chunkX % RENDERED_WORLD_WIDTH);
        if (chunkX < 0) chunkX += RENDERED_WORLD_WIDTH;

        chunkZ = (chunkZ % RENDERED_WORLD_WIDTH);
        if (chunkZ < 0) chunkZ += RENDERED_WORLD_WIDTH;

        return chunkX * RENDERED_WORLD_WIDTH + chunkZ;
    }

    public static int getEntityClusterIndex(int clusterX, int clusterY, int clusterZ) {
        return clusterX << 4 | clusterZ << 2 | clusterY;
    }


    public static short[] getBlocks(int blocksLength, int startIndex, byte[] data) {
        short[] blocks = new short[blocksLength];

        for (int i = 0; i < blocksLength; i++) {
            int index = i << 1;
            short block = (short) (((int) data[startIndex + index] & 0xFF) << 8 | ((int) data[startIndex + 1 + index] & 0xFF));
            blocks[i] = block;
        }

        return blocks;
    }

    public static byte[] toByteArray(short s) {
        return new byte[]{(byte) (s >> 8 & 0xFF), (byte) (s & 0xFF)};
    }

    public static byte[] toByteArray(int i) {
        return new byte[]{(byte) (i >> 24 & 0xFF), (byte) (i >> 16 & 0xFF), (byte) (i >> 8 & 0xFF), (byte) (i & 0xFF)};
    }

    public static byte[] toByteArray(long l) {
        return new byte[]{(byte) (l >> 56 & 0xFF), (byte) (l >> 48 & 0xFF), (byte) (l >> 40 & 0xFF), (byte) (l >> 32 & 0xFF),
                (byte) (l >> 24 & 0xFF), (byte) (l >> 16 & 0xFF), (byte) (l >> 8 & 0xFF), (byte) (l & 0xFF)};
    }

    public static byte[] toByteArray(short[] blocks) {
        byte[] byteArray = new byte[blocks.length * 2];

        for (int i = 0; i < blocks.length; i++) {
            byteArray[i << 1] = (byte) (blocks[i] >> 8 & 0xFF);
            byteArray[(i << 1) + 1] = (byte) (blocks[i] & 0xFF);
        }

        return byteArray;
    }

    public static byte[] toByteArray(int[] ints) {
        byte[] byteArray = new byte[(ints.length << 2)];

        for (int i = 0; i < ints.length; i++) {
            byteArray[i << 2] = (byte) (ints[i] >> 24 & 0xFF);
            byteArray[(i << 2) + 1] = (byte) (ints[i] >> 16 & 0xFF);
            byteArray[(i << 2) + 2] = (byte) (ints[i] >> 8 & 0xFF);
            byteArray[(i << 2) + 3] = (byte) (ints[i] & 0xFF);
        }

        return byteArray;
    }

    public static int[] getInts(byte[] bytes, int count) {
        int[] ints = new int[count];

        for (int i = 0; i < count; i++) {
            int index = i << 2;
            ints[i] = getInt(bytes, index);
        }

        return ints;
    }

    public static short getShort(byte[] bytes, int startIndex) {
        short result = 0;
        for (int index = startIndex; index < startIndex + 2; index++) {
            short currentByte = (short) (bytes[index] & 0xFF);
            result <<= 8;
            result |= currentByte;
        }
        return result;
    }

    public static int getInt(byte[] bytes, int startIndex) {
        int result = 0;
        for (int index = startIndex; index < startIndex + 4; index++) {
            int currentByte = bytes[index] & 0xFF;
            result <<= 8;
            result |= currentByte;
        }
        return result;
    }

    public static long getLong(byte[] bytes, int startIndex) {
        long result = 0;
        for (int index = startIndex; index < startIndex + 8; index++) {
            long currentByte = bytes[index] & 0xFF;
            result <<= 8;
            result |= currentByte;
        }
        return result;
    }
}
