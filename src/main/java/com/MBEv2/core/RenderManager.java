package com.MBEv2.core;

import com.MBEv2.core.entity.*;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.core.utils.Transformation;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.Launcher;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderManager {

    private final WindowManager window;
    private ShaderManager blockShader;
    private ShaderManager skyBoxShader;
    private ShaderManager GUIShader;
    private ShaderManager waterShader;

    private final List<Model> chunkModels = new ArrayList<>();
    private final List<Model> waterModels = new ArrayList<>();
    private final List<GUIElement> GUIElements = new ArrayList<>();
    private final Player player;
    private GUIElement inventoryOverlay;
    private SkyBox skyBox;
    private boolean headUnderWater = false;

    private float time = 1.0f;

    private int modelIndexBuffer;

    private Texture xRayAtlas;
    private Texture atlas;
    private boolean xRay;

    public RenderManager(Player player) {
        window = Launcher.getWindow();
        this.player = player;
    }

    public void init() throws Exception {

        xRayAtlas = new Texture(ObjectLoader.loadTexture("textures/XRayAtlas.png"));
        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));

        blockShader = new ShaderManager();
        blockShader.createVertexShader(Utils.loadResources("/shaders/blockVertex.glsl"));
        blockShader.createFragmentShader(Utils.loadResources("/shaders/blockFragment.glsl"));
        blockShader.link();
        blockShader.createUniform("textureSampler");
        blockShader.createUniform("projectionMatrix");
        blockShader.createUniform("viewMatrix");
        blockShader.createUniform("worldPos");
        blockShader.createUniform("time");
        blockShader.createUniform("headUnderWater");
        blockShader.createUniform("cameraPosition");

        waterShader = new ShaderManager();
        waterShader.createVertexShader(Utils.loadResources("/shaders/waterVertex.glsl"));
        waterShader.createFragmentShader(Utils.loadResources("/shaders/waterFragment.glsl"));
        waterShader.link();
        waterShader.createUniform("textureSampler");
        waterShader.createUniform("projectionMatrix");
        waterShader.createUniform("viewMatrix");
        waterShader.createUniform("worldPos");
        waterShader.createUniform("time");
        waterShader.createUniform("headUnderWater");
        waterShader.createUniform("cameraPosition");
        waterShader.createUniform("shouldSimulateWaves");

        skyBoxShader = new ShaderManager();
        skyBoxShader.createVertexShader(Utils.loadResources("/shaders/skyBoxVertex.glsl"));
        skyBoxShader.createFragmentShader(Utils.loadResources("/shaders/skyBoxFragment.glsl"));
        skyBoxShader.link();
        skyBoxShader.createUniform("textureSampler1");
        skyBoxShader.createUniform("textureSampler2");
        skyBoxShader.createUniform("projectionMatrix");
        skyBoxShader.createUniform("viewMatrix");
        skyBoxShader.createUniform("transformationMatrix");
        skyBoxShader.createUniform("time");

        GUIShader = new ShaderManager();
        GUIShader.createVertexShader(Utils.loadResources("/shaders/GUIVertex.glsl"));
        GUIShader.createFragmentShader(Utils.loadResources("/shaders/GUIFragment.glsl"));
        GUIShader.link();
        GUIShader.createUniform("textureSampler");
        GUIShader.createUniform("position");

        int[] indices = new int[786432];
        int index = 0;
        for (int i = 0; i < indices.length; i += 6) {
            indices[i] = index;
            indices[i + 1] = index + 1;
            indices[i + 2] = index + 2;
            indices[i + 3] = index + 3;
            indices[i + 4] = index + 2;
            indices[i + 5] = index + 1;
            index += 4;
        }

        modelIndexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
        IntBuffer buffer = Utils.storeDateInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    public void bindModel(Model model) {
        GL30.glBindVertexArray(model.getVao());
        GL20.glEnableVertexAttribArray(0);

        blockShader.setUniform("worldPos", model.getPosition());
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    public void bindSkyBox(SkyBox skyBox, Camera camera) {
        GL30.glBindVertexArray(skyBox.getVao());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture1().id());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture2().id());

        skyBoxShader.setUniform("textureSampler1", 0);
        skyBoxShader.setUniform("textureSampler2", 1);
        skyBoxShader.setUniform("time", time);
        skyBoxShader.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        skyBoxShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        skyBoxShader.setUniform("transformationMatrix", Transformation.createTransformationMatrix(skyBox.getPosition()));
    }

    public void bindGUIElement(GUIElement element) {
        GL30.glBindVertexArray(element.getVao());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, element.getTexture().id());

        GUIShader.setUniform("textureSampler", 0);
        GUIShader.setUniform("position", element.getPosition());
    }

    public void bindWaterModel(Model model, int chunkX, int chunkY, int chunkZ) {
        GL30.glBindVertexArray(model.getVao());
        GL20.glEnableVertexAttribArray(0);

        Vector3i modelPosition = model.getPosition();
        boolean shouldSimulateWaves = Math.abs(chunkX - (modelPosition.x >> CHUNK_SIZE_BITS)) < 2 && Math.abs(chunkY - (modelPosition.y >> CHUNK_SIZE_BITS)) < 2 && Math.abs(chunkZ - (modelPosition.z >> CHUNK_SIZE_BITS)) < 2;

        waterShader.setUniform("shouldSimulateWaves", shouldSimulateWaves ? 1 : 0);
        waterShader.setUniform("worldPos", modelPosition);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    public void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    public void render(Camera camera) {
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);

        clear();

        renderSkyBox(camera);

        renderOpaqueChunks(projectionMatrix, viewMatrix);

        renderWaterChunks(projectionMatrix, viewMatrix);

        renderGUIElements();

        unbind();
    }

    public void renderOpaqueChunks(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        blockShader.bind();
        blockShader.setUniform("projectionMatrix", projectionMatrix);
        blockShader.setUniform("viewMatrix", viewMatrix);
        blockShader.setUniform("textureSampler", 0);
        blockShader.setUniform("time", time);
        blockShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        blockShader.setUniform("cameraPosition", player.getCamera().getPosition());

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, isxRay() ? xRayAtlas.id() : atlas.id());

        for (Model model : chunkModels) {
            bindModel(model);

            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (model.getVertexCount() * 0.75), GL11.GL_UNSIGNED_INT, 0);
        }
        blockShader.unBind();
        chunkModels.clear();
    }

    public void renderWaterChunks(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        waterShader.bind();
        waterShader.setUniform("projectionMatrix", projectionMatrix);
        waterShader.setUniform("viewMatrix", viewMatrix);
        waterShader.setUniform("textureSampler", 0);
        waterShader.setUniform("time", time);
        waterShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        waterShader.setUniform("cameraPosition", player.getCamera().getPosition());

        Vector3f playerPosition = player.getCamera().getPosition();
        int chunkX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        int chunkY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        int chunkZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        for (Model waterModel : waterModels) {
            bindWaterModel(waterModel, chunkX, chunkY, chunkZ);

            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (waterModel.getVertexCount() * 0.75), GL11.GL_UNSIGNED_INT, 0);
        }
        GL11.glDisable(GL11.GL_BLEND);
        waterModels.clear();
        waterShader.unBind();
    }

    public void renderSkyBox(Camera camera) {
        skyBoxShader.bind();
        bindSkyBox(skyBox, camera);

        GL11.glDrawElements(GL11.GL_TRIANGLES, skyBox.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        skyBoxShader.unBind();
    }

    public void renderGUIElements() {
        GUIShader.bind();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (player.isInInventory()) {
            GL11.glEnable(GL11.GL_BLEND);
            bindGUIElement(inventoryOverlay);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, inventoryOverlay.getVertexCount());

            GL11.glDisable(GL11.GL_BLEND);
        }

        for (GUIElement element : GUIElements) {
            bindGUIElement(element);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, element.getVertexCount());
        }
        GUIElements.clear();
        GUIShader.unBind();
    }

    public void processModel(Model model) {
        chunkModels.add(model);
    }

    public void processWaterModel(Model waterModel) {
        waterModels.add(waterModel);
    }

    public void processSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public void processGUIElement(GUIElement element) {
        GUIElements.add(element);
    }

    public void setHeadUnderWater(boolean headUnderWater) {
        this.headUnderWater = headUnderWater;
    }

    public void setInventoryOverlay(GUIElement inventoryOverlay) {
        this.inventoryOverlay = inventoryOverlay;
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanUp() {
        blockShader.cleanUp();
        skyBoxShader.cleanUp();
        GUIShader.cleanUp();
    }

    public void setXRay(boolean xRay) {
        this.xRay = xRay;
    }

    public boolean isxRay() {
        return xRay;
    }

    public void incrementTime() {
        time += TIME_SPEED;
        if (time > 1.0f) time -= 2.0f;
    }
}
