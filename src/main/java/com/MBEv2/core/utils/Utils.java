package com.MBEv2.core.utils;

import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

    public static String loadResources(String filename) throws Exception {
        String result;
        try (InputStream in = Utils.class.getResourceAsStream(filename)) {
            assert in != null;
            try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
                result = scanner.useDelimiter("\\A").next();
            }
        }
        return result;
    }

    public static int floor(float value){
        int addend = value < 0 ? -1 : 0;
        return (int) value + addend;
    }
}
