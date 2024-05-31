package com.MBEv2.core;

import com.MBEv2.core.entity.GUIElement;
import com.MBEv2.core.entity.Model;
import com.MBEv2.core.entity.SkyBox;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.core.utils.Transformation;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.Launcher;
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

    private final List<Model> chunkModels = new ArrayList<>();
    private final List<Model> transparentChunkModels = new ArrayList<>();
    private final List<GUIElement> GUIElements = new ArrayList<>();
    private GUIElement waterOverlay;
    private SkyBox skyBox;
    private boolean headUnderWater = false;

    private float time = 1.0f;

    private int modelIndexBuffer;

    public RenderManager() {
        window = Launcher.getWindow();
    }

    public void init() throws Exception {

        blockShader = new ShaderManager();
        blockShader.createVertexShader(Utils.loadResources("/shaders/blockVertex.glsl"));
        blockShader.createFragmentShader(Utils.loadResources("/shaders/blockFragment.glsl"));
        blockShader.link();
        blockShader.createUniform("textureSampler");
        blockShader.createUniform("projectionMatrix");
        blockShader.createUniform("viewMatrix");
        blockShader.createUniform("worldPos");
        blockShader.createUniform("time");

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

        int[] indices = new int[98304];
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

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().id());
    }

    public void bindSkyBox(SkyBox skyBox) {
        GL30.glBindVertexArray(skyBox.getId());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture1().id());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture2().id());
    }

    public void bindGUIElement(GUIElement element) {
        GL30.glBindVertexArray(element.getId());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, element.getTexture().id());
    }

    public void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void prepareModel(Model model, Camera camera) {
        blockShader.setUniform("textureSampler", 0);
        blockShader.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        blockShader.setUniform("worldPos", model.getPosition());
        blockShader.setUniform("time", time);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    public void prepareSkyBox(Camera camera) {
        skyBoxShader.setUniform("textureSampler1", 0);
        skyBoxShader.setUniform("textureSampler2", 1);
        skyBoxShader.setUniform("time", time);
        skyBoxShader.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        skyBoxShader.setUniform("projectionMatrix", window.getProjectionMatrix());
        skyBoxShader.setUniform("transformationMatrix", Transformation.createTransformationMatrix(skyBox.getPosition()));
    }

    public void prepareGUIElement() {
        GUIShader.setUniform("textureSampler", 0);
    }

    public void render(Camera camera) {

        time += TIME_SPEED;
        if (time > 1.0f)
            time = -1.0f;

        clear();
        blockShader.bind();
        blockShader.setUniform("projectionMatrix", window.updateProjectionMatrix());
        Vector3f cameraDirection = camera.getDirection();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);

        for (Model model : chunkModels) {
            if (modelIsOutsideView(model, cameraDirection, camera))
                continue;
            bindModel(model);

            prepareModel(model, camera);
            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (model.getVertexCount() * 0.75), GL11.GL_UNSIGNED_INT, 0);

            unbind();
        }
        chunkModels.clear();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        for (Model transparentModel : transparentChunkModels) {
            if (modelIsOutsideView(transparentModel, cameraDirection, camera)) {
                continue;
            }
            bindModel(transparentModel);

            prepareModel(transparentModel, camera);
            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (transparentModel.getVertexCount() * 0.75), GL11.GL_UNSIGNED_INT, 0);

            unbind();
        }
        GL11.glDisable(GL11.GL_BLEND);
        transparentChunkModels.clear();
        blockShader.unBind();

        skyBoxShader.bind();
        bindSkyBox(skyBox);
        prepareSkyBox(camera);
        GL11.glDrawElements(GL11.GL_TRIANGLES, skyBox.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        unbind();
        skyBoxShader.unBind();

        GUIShader.bind();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (headUnderWater) {
            GL11.glEnable(GL11.GL_BLEND);
            bindGUIElement(waterOverlay);

            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, waterOverlay.getVertexCount());

            unbind();
            GL11.glDisable(GL11.GL_BLEND);
        }

        for (GUIElement element : GUIElements) {
            bindGUIElement(element);

            prepareGUIElement();
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, element.getVertexCount());

            unbind();
        }
        GUIElements.clear();
        GUIShader.unBind();
    }

    private boolean modelIsOutsideView(Model model, Vector3f cameraDirection, Camera camera) {
        float fov = FOV * 0.71f;
        Vector3f cP = camera.getPosition();
        Vector3i mP = model.getPosition();

        if (angleToChunkCorner(camera.getPosition(), mP.x, mP.y, mP.z, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x, mP.y, mP.z + CHUNK_SIZE, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x, mP.y + CHUNK_SIZE, mP.z, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x, mP.y + CHUNK_SIZE, mP.z + CHUNK_SIZE, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x + CHUNK_SIZE, mP.y, mP.z, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x + CHUNK_SIZE, mP.y, mP.z + CHUNK_SIZE, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x + CHUNK_SIZE, mP.y + CHUNK_SIZE, mP.z, cameraDirection) < fov)
            return false;
        if (angleToChunkCorner(camera.getPosition(), mP.x + CHUNK_SIZE, mP.y + CHUNK_SIZE, mP.z + CHUNK_SIZE, cameraDirection) < fov)
            return false;

        int mPX = mP.x + CHUNK_SIZE / 2;
        int mPY = mP.y + CHUNK_SIZE / 2;
        int mPZ = mP.z + CHUNK_SIZE / 2;
        float distance = (mPX - cP.x) * (mPX - cP.x) + (mPY - cP.y) * (mPY - cP.y) + (mPZ - cP.z) * (mPZ - cP.z);

        return distance > 768;
    }

    private double angleToChunkCorner(Vector3f cameraPosition, int x, int y, int z, Vector3f cameraDirection) {
        float deltaX = x - cameraPosition.x;
        float deltaY = y - cameraPosition.y;
        float deltaZ = z - cameraPosition.z;

        if (dotProduct(cameraDirection, deltaX, deltaY, deltaZ) < 0)
            return Math.PI;

        float inverseLength = (float) (1f / Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));
        return Math.acos(dotProduct(cameraDirection, deltaX * inverseLength, deltaY * inverseLength, deltaZ * inverseLength));
    }

    private double dotProduct(Vector3f vector, float x, float y, float z) {
        return vector.x * x + vector.y * y + vector.z * z;
    }

    public void processModel(Model model) {
        chunkModels.add(model);
    }

    public void processTransparentModel(Model transparentModel) {
        transparentChunkModels.add(transparentModel);
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

    public void setWaterOverlay(GUIElement waterOverlay) {
        this.waterOverlay = waterOverlay;
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanUp() {
        blockShader.cleanUp();
    }
}
