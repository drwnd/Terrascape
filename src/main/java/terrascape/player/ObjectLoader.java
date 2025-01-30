package terrascape.player;

import terrascape.entity.GUIElement;
import terrascape.entity.WaterModel;
import terrascape.entity.OpaqueModel;
import terrascape.entity.SkyBox;
import terrascape.utils.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static terrascape.utils.Constants.*;

public final class ObjectLoader {

    public static OpaqueModel loadOpaqueModel(int[] vertices, Vector3i position, int[] vertexCounts) {
        int vao = createVAO();
        int vbo = storeDateInAttributeList(0, 2, vertices);
        unbind();
        return new OpaqueModel(vao, vertexCounts, position, vbo);
    }

    public static WaterModel loadModel(int[] vertices, Vector3i position) {
        int vao = createVAO();
        int vbo = storeDateInAttributeList(0, 2, vertices);
        unbind();
        return new WaterModel(vao, vertices.length, position, vbo);
    }

    public static long loadVoaAndVbo(int attributeNo, int size, int[] vertices) {
        int vao = createVAO();
        int vbo = storeDateInAttributeList(attributeNo, size, vertices);
        unbind();
        return (long) vao << 32 | vbo;
    }

    public static int loadParticle(float[] vertexCoordinates, float[] vertexVelocities, float[] textureCoordinates) {
        int vao = createVAO();
        storeDateInAttributeList(0, 3, vertexCoordinates);
        storeDateInAttributeList(1, 3, vertexVelocities);
        storeDateInAttributeList(2, 2, textureCoordinates);
        unbind();
        return vao;
    }

    public static SkyBox loadSkyBox(float[] vertices, float[] textureCoordinates, int[] indices, Vector3f position) {
        int vao = createVAO();
        storeIndicesInBuffer(indices);
        storeDateInAttributeList(0, 3, vertices);
        storeDateInAttributeList(1, 2, textureCoordinates);
        unbind();
        return new SkyBox(vao, indices.length, position);
    }

    public static GUIElement loadGUIElement(float[] vertices, float[] textureCoordinates, Vector2f position) {
        int vao = createVAO();
        int vbo1 = storeDateInAttributeList(0, 2, vertices);
        int vbo2 = storeDateInAttributeList(1, 2, textureCoordinates);
        unbind();
        return new GUIElement(vao, vertices.length, vbo1, vbo2, position);
    }

    public static int loadTextRow() {
        int vao = createVAO();

        int[] textData = new int[MAX_TEXT_LENGTH * 4];
        for (int i = 0; i < textData.length; i += 4) {
            textData[i] = i >> 2;
            textData[i + 1] = i >> 2 | 128;
            textData[i + 2] = i >> 2 | 256;
            textData[i + 3] = i >> 2 | 384;
        }
        storeDateInAttributeList(0, 1, textData);
        unbind();

        return vao;
    }

    private static int createVAO() {
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        return vao;
    }

    private static void storeIndicesInBuffer(int[] indices) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = Utils.storeDateInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private static int storeDateInAttributeList(int attributeNo, int size, float[] data) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = Utils.storeDateInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNo, size, GL15.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    private static int storeDateInAttributeList(int attributeNo, int size, int[] data) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        GL30.glVertexAttribIPointer(attributeNo, size, GL15.GL_INT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    public static int loadTexture(String filename) throws Exception {
        int width, height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            buffer = STBImage.stbi_load(filename, w, h, c, 4);
            if (buffer == null)
                throw new Exception("Image FIle " + filename + " not loaded " + STBImage.stbi_failure_reason());

            width = w.get();
            height = h.get();
        }

        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        STBImage.stbi_image_free(buffer);
        return id;
    }

    private static void unbind() {
        GL30.glBindVertexArray(0);
    }

    //https://ahbejarano.gitbook.io/lwjglgamedev/chapter-16
    public static int loadSound(String filename) {
        int buffer = AL10.alGenBuffers();

        STBVorbisInfo info = STBVorbisInfo.malloc();
        ShortBuffer pcm = readVorbis(filename, info);

        // Copy to buffer
        AL10.alBufferData(buffer, info.channels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, pcm, info.sample_rate());
//        if (info.channels() != 1) System.out.println(filename);

        return buffer;
    }

    //https://ahbejarano.gitbook.io/lwjglgamedev/chapter-16
    private static ShortBuffer readVorbis(String filename, STBVorbisInfo info) throws RuntimeException {
        MemoryStack stack = MemoryStack.stackPush();
        IntBuffer error = stack.mallocInt(1);
        // IDE has no idea what it's talking about
        @SuppressWarnings("DataFlowIssue") long decoder = STBVorbis.stb_vorbis_open_filename(filename, error, null);
        if (decoder == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        STBVorbis.stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);

        ShortBuffer result = MemoryUtil.memAllocShort(lengthSamples * channels);

        result.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, result) * channels);
        STBVorbis.stb_vorbis_close(decoder);

        return result;
    }

    public static String loadResources(String filename) throws Exception {
        String result;

        InputStream in = new FileInputStream(filename);
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);
        result = scanner.useDelimiter("\\A").next();

        return result;
    }

    public static void removeVAO(int vao) {
        GL30.glBindVertexArray(vao);
        GL30.glDeleteVertexArrays(vao);
    }

    public static void removeVBO(int vbo) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL30.glDeleteBuffers(vbo);
    }

    public static void cleanUp() {

    }

    private ObjectLoader() { }
}
