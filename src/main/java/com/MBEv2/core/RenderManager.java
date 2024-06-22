package com.MBEv2.core;

import com.MBEv2.core.entity.GUIElement;
import com.MBEv2.core.entity.Model;
import com.MBEv2.core.entity.SkyBox;

import static com.MBEv2.core.utils.Constants.*;

import com.MBEv2.core.utils.Transformation;
import com.MBEv2.core.utils.Utils;
import com.MBEv2.test.Launcher;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().id());
    }

    public void bindSkyBox(SkyBox skyBox) {
        GL30.glBindVertexArray(skyBox.getVao());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture1().id());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyBox.getTexture2().id());
    }

    public void bindGUIElement(GUIElement element) {
        GL30.glBindVertexArray(element.getVao());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, element.getTexture().id());
    }

    public void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void prepareModel(Model model) {
        blockShader.setUniform("worldPos", model.getPosition());
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

        Matrix4f projectionMatrix = window.updateProjectionMatrix();
        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);

        clear();
        blockShader.bind();
        blockShader.setUniform("projectionMatrix", projectionMatrix);
        blockShader.setUniform("viewMatrix", viewMatrix);
        blockShader.setUniform("textureSampler", 0);
        blockShader.setUniform("time", time);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);

        Matrix4f projectionViewMatrix = new Matrix4f();
        projectionMatrix.mul(viewMatrix, projectionViewMatrix);
        FrustumIntersection frustumIntersection = new FrustumIntersection(projectionViewMatrix);

        for (Model model : chunkModels) {
            Vector3f position = new Vector3f(model.getPosition());
            int intersectionType = frustumIntersection.intersectAab(position, new Vector3f(position.x + CHUNK_SIZE, position.y + CHUNK_SIZE, position.z + CHUNK_SIZE));
            if (intersectionType != FrustumIntersection.INTERSECT && intersectionType != FrustumIntersection.INSIDE)
                continue;
            bindModel(model);

            prepareModel(model);
            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (model.getVertexCount() * 0.75), GL11.GL_UNSIGNED_INT, 0);

            unbind();
        }
        chunkModels.clear();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);
        for (Model transparentModel : transparentChunkModels) {
            Vector3f position = new Vector3f(transparentModel.getPosition());
            int intersectionType = frustumIntersection.intersectAab(position, new Vector3f(position.x + CHUNK_SIZE, position.y + CHUNK_SIZE, position.z + CHUNK_SIZE));
            if (intersectionType != FrustumIntersection.INTERSECT && intersectionType != FrustumIntersection.INSIDE)
                continue;
            bindModel(transparentModel);

            prepareModel(transparentModel);
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
