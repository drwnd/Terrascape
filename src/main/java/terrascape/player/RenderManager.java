package terrascape.player;

import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.entity.*;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

import terrascape.entity.entities.Entity;
import terrascape.entity.particles.Particle;
import terrascape.generation.GenerationData;
import terrascape.generation.WorldGeneration;
import terrascape.utils.Transformation;
import terrascape.utils.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

import java.awt.Color;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class RenderManager {

    public RenderManager(Player player) {
        window = Launcher.getWindow();
        this.player = player;
    }

    public void init() throws Exception {

        loadTextures();

        loadShaders();

        createConstantBuffers();
    }

    public void reloadShaders() {
        ShaderManager newBlockShader = null;
        ShaderManager newWaterShader = null;
        ShaderManager newFoliageShader = null;
        ShaderManager newSkyBoxShader = null;
        ShaderManager newGUIShader = null;
        ShaderManager newTextShader = null;
        ShaderManager newEntityShader = null;
        ShaderManager newParticleShader = null;

        try {
            newBlockShader = createBlockShader();
            blockShader.cleanUp();
            blockShader = newBlockShader;
        } catch (Exception exception) {
            if (newBlockShader != null) newBlockShader.cleanUp();
            System.err.println("Failed to reload block shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newWaterShader = createWaterShader();
            waterShader.cleanUp();
            waterShader = newWaterShader;
        } catch (Exception exception) {
            if (newWaterShader != null) newWaterShader.cleanUp();
            System.err.println("Failed to reload water shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newFoliageShader = createFoliageShader();
            foliageShader.cleanUp();
            foliageShader = newFoliageShader;
        } catch (Exception exception) {
            if (newFoliageShader != null) newFoliageShader.cleanUp();
            System.err.println("Failed to reload foliage shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newSkyBoxShader = createSkyBoxShader();
            skyBoxShader.cleanUp();
            skyBoxShader = newSkyBoxShader;
        } catch (Exception exception) {
            if (newSkyBoxShader != null) newSkyBoxShader.cleanUp();
            System.err.println("Failed to reload sky box shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newGUIShader = createGUIShader();
            GUIShader.cleanUp();
            GUIShader = newGUIShader;
        } catch (Exception exception) {
            if (newGUIShader != null) newGUIShader.cleanUp();
            System.err.println("Failed to reload GUI shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newTextShader = createTextShader();
            textShader.cleanUp();
            textShader = newTextShader;
        } catch (Exception exception) {
            if (newTextShader != null) newTextShader.cleanUp();
            System.err.println("Failed to reload text shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newEntityShader = createEntityShader();
            entityShader.cleanUp();
            entityShader = newEntityShader;
        } catch (Exception exception) {
            if (newEntityShader != null) newEntityShader.cleanUp();
            System.err.println("Failed to reload entity shader.");
            System.err.println(exception.getMessage());
        }
        try {
            newParticleShader = createParticleShader();
            particleShader.cleanUp();
            particleShader = newParticleShader;
        } catch (Exception exception) {
            if (newParticleShader != null) newParticleShader.cleanUp();
            System.err.println("Failed to reload particle shader.");
            System.err.println(exception.getMessage());
        }

        System.out.println("Shader reload completed.");
    }

    private void loadShaders() throws Exception {
        blockShader = createBlockShader();
        waterShader = createWaterShader();
        foliageShader = createFoliageShader();
        skyBoxShader = createSkyBoxShader();
        GUIShader = createGUIShader();
        textShader = createTextShader();
        entityShader = createEntityShader();
        particleShader = createParticleShader();
    }

    private void createConstantBuffers() {
        int[] indices = new int[393216];
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

        textRowVertexArray = ObjectLoader.loadTextRow();

        createEntityBuffers(1);
    }

    private void createEntityBuffers(int size) {
        long byteSize = (long) size << 4;

        entityPositionBuffer = GL46.glCreateBuffers();
        entityRotationBuffer = GL46.glCreateBuffers();
        entityIntsBuffers = GL46.glCreateBuffers();
//        entityTranslationsBuffer = GL46.glCreateBuffers();

        GL46.glNamedBufferStorage(entityPositionBuffer, byteSize, GL46.GL_DYNAMIC_STORAGE_BIT);
        GL46.glNamedBufferStorage(entityRotationBuffer, byteSize, GL46.GL_DYNAMIC_STORAGE_BIT);
        GL46.glNamedBufferStorage(entityIntsBuffers, byteSize, GL46.GL_DYNAMIC_STORAGE_BIT);
//        GL46.glNamedBufferStorage(entityTranslationsBuffer, byteSize, GL46.GL_DYNAMIC_STORAGE_BIT);
    }

    private void loadTextures() throws Exception {
        atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));
        textAtlas = new Texture(ObjectLoader.loadTexture("textures/textAtlas.png"));
    }

    private ShaderManager createParticleShader() throws Exception {
        ShaderManager particleShader = new ShaderManager();
        particleShader.createVertexShader(ObjectLoader.loadResources("shaders/ParticleVertex.glsl"));
        particleShader.createFragmentShader(ObjectLoader.loadResources("shaders/ParticleFragment.glsl"));
        particleShader.link();
        particleShader.createUniform("projectionMatrix");
        particleShader.createUniform("viewMatrix");
        particleShader.createUniform("position");
        particleShader.createUniform("cameraPosition");
        particleShader.createUniform("lightLevel");
        particleShader.createUniform("textureSampler");
        particleShader.createUniform("textureOffset_");
        particleShader.createUniform("time");
        particleShader.createUniform("particleProperties");
        particleShader.createUniform("headUnderWater");
        return particleShader;
    }

    private ShaderManager createEntityShader() throws Exception {
        ShaderManager entityShader = new ShaderManager();
        entityShader.createVertexShader(ObjectLoader.loadResources("shaders/EntityVertex.glsl"));
        entityShader.createFragmentShader(ObjectLoader.loadResources("shaders/EntityFragment.glsl"));
        entityShader.link();
        entityShader.createUniform("projectionMatrix");
        entityShader.createUniform("viewMatrix");
        entityShader.createUniform("time");
        entityShader.createUniform("textureSampler");
        entityShader.createUniform("headUnderWater");
        entityShader.createUniform("cameraPosition");
        return entityShader;
    }

    private ShaderManager createTextShader() throws Exception {
        ShaderManager textShader = new ShaderManager();
        textShader.createVertexShader(ObjectLoader.loadResources("shaders/textVertex.glsl"));
        textShader.createFragmentShader(ObjectLoader.loadResources("shaders/textFragment.glsl"));
        textShader.link();
        textShader.createUniform("screenSize");
        textShader.createUniform("charSize");
        textShader.createUniform("string");
        textShader.createUniform("yOffset");
        textShader.createUniform("textureSampler");
        textShader.createUniform("xOffset");
        textShader.createUniform("color");
        return textShader;
    }

    private ShaderManager createGUIShader() throws Exception {
        ShaderManager GUIShader = new ShaderManager();
        GUIShader.createVertexShader(ObjectLoader.loadResources("shaders/GUIVertex.glsl"));
        GUIShader.createFragmentShader(ObjectLoader.loadResources("shaders/GUIFragment.glsl"));
        GUIShader.link();
        GUIShader.createUniform("textureSampler");
        GUIShader.createUniform("position");
        return GUIShader;
    }

    private ShaderManager createSkyBoxShader() throws Exception {
        ShaderManager skyBoxShader = new ShaderManager();
        skyBoxShader.createVertexShader(ObjectLoader.loadResources("shaders/skyBoxVertex.glsl"));
        skyBoxShader.createFragmentShader(ObjectLoader.loadResources("shaders/skyBoxFragment.glsl"));
        skyBoxShader.link();
        skyBoxShader.createUniform("textureSampler1");
        skyBoxShader.createUniform("textureSampler2");
        skyBoxShader.createUniform("projectionMatrix");
        skyBoxShader.createUniform("viewMatrix");
        skyBoxShader.createUniform("transformationMatrix");
        skyBoxShader.createUniform("time");
        return skyBoxShader;
    }

    private ShaderManager createFoliageShader() throws Exception {
        ShaderManager foliageShader = new ShaderManager();
        foliageShader.createVertexShader(ObjectLoader.loadResources("shaders/FoliageVertex.glsl"));
        foliageShader.createFragmentShader(ObjectLoader.loadResources("shaders/blockFragment.glsl"));
        foliageShader.link();
        foliageShader.createUniform("textureSampler");
        foliageShader.createUniform("projectionMatrix");
        foliageShader.createUniform("viewMatrix");
        foliageShader.createUniform("worldPos");
        foliageShader.createUniform("time");
        foliageShader.createUniform("headUnderWater");
        foliageShader.createUniform("cameraPosition");
        foliageShader.createUniform("shouldSimulateWind");
        return foliageShader;
    }

    private ShaderManager createWaterShader() throws Exception {
        ShaderManager waterShader = new ShaderManager();
        waterShader.createVertexShader(ObjectLoader.loadResources("shaders/waterVertex.glsl"));
        waterShader.createFragmentShader(ObjectLoader.loadResources("shaders/waterFragment.glsl"));
        waterShader.link();
        waterShader.createUniform("textureSampler");
        waterShader.createUniform("projectionMatrix");
        waterShader.createUniform("viewMatrix");
        waterShader.createUniform("worldPos");
        waterShader.createUniform("time");
        waterShader.createUniform("headUnderWater");
        waterShader.createUniform("cameraPosition");
        waterShader.createUniform("shouldSimulateWaves");
        return waterShader;
    }

    private ShaderManager createBlockShader() throws Exception {
        ShaderManager blockShader = new ShaderManager();
        blockShader.createVertexShader(ObjectLoader.loadResources("shaders/blockVertex.glsl"));
        blockShader.createFragmentShader(ObjectLoader.loadResources("shaders/blockFragment.glsl"));
        blockShader.link();
        blockShader.createUniform("textureSampler");
        blockShader.createUniform("projectionMatrix");
        blockShader.createUniform("viewMatrix");
        blockShader.createUniform("worldPos");
        blockShader.createUniform("time");
        blockShader.createUniform("headUnderWater");
        blockShader.createUniform("cameraPosition");
        return blockShader;
    }

    private void bindModel(OpaqueModel model) {
        GL30.glBindVertexArray(model.vao);
        GL20.glEnableVertexAttribArray(0);

        blockShader.setUniform("worldPos", model.X, model.Y, model.Z);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    private void bindFoliageModel(OpaqueModel model) {
        GL30.glBindVertexArray(model.vao);
        GL20.glEnableVertexAttribArray(0);

        boolean shouldSimulateWind = model.getDistanceFromPlayer(playerChunkX, playerChunkY, playerChunkZ) <= 1;

        foliageShader.setUniform("shouldSimulateWind", shouldSimulateWind ? 1 : 0);
        foliageShader.setUniform("worldPos", model.X, model.Y, model.Z);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    private void bindSkyBox(SkyBox skyBox, Camera camera) {
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

    private void bindGUIElement(GUIElement element) {
        GL30.glBindVertexArray(element.getVao());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, element.getTexture().id());

        GUIShader.setUniform("textureSampler", 0);
        GUIShader.setUniform("position", element.getPosition());
    }

    private void bindWaterModel(WaterModel model) {
        GL30.glBindVertexArray(model.vao);
        GL20.glEnableVertexAttribArray(0);

        int modelChunkX = model.X >> CHUNK_SIZE_BITS;
        int modelChunkY = model.Y >> CHUNK_SIZE_BITS;
        int modelChunkZ = model.Z >> CHUNK_SIZE_BITS;

        boolean shouldSimulateWaves = Math.abs(playerChunkX - modelChunkX) <= 1 &&
                Math.abs(playerChunkY - modelChunkY) <= 1 &&
                Math.abs(playerChunkZ - modelChunkZ) <= 1;

        waterShader.setUniform("shouldSimulateWaves", shouldSimulateWaves ? 1 : 0);
        waterShader.setUniform("worldPos", model.X, model.Y, model.Z);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);
    }

    private void unbind() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    public void render(Camera camera, float passedTicks) {
        Matrix4f projectionMatrix = window.getProjectionMatrix();
        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);
        Vector3f playerPosition = player.getCamera().getPosition();
        playerChunkX = Utils.floor(playerPosition.x) >> CHUNK_SIZE_BITS;
        playerChunkY = Utils.floor(playerPosition.y) >> CHUNK_SIZE_BITS;
        playerChunkZ = Utils.floor(playerPosition.z) >> CHUNK_SIZE_BITS;

        clear();

        renderSkyBox(camera);

        renderOpaqueChunks(projectionMatrix, viewMatrix, passedTicks);

        renderFoliageChunks(projectionMatrix, viewMatrix, passedTicks);

        renderEntities(projectionMatrix, viewMatrix, passedTicks);

        renderParticles(projectionMatrix, viewMatrix, passedTicks);

        renderWaterChunks(projectionMatrix, viewMatrix, passedTicks);

        renderGUIElements();

        if (player.isDebugScreenOpen()) renderDebugText();

        chunkModels.clear();
        foliageModels.clear();
        waterModels.clear();
        GUIElements.clear();
        entities.clear();
        particles.clear();

        unbind();
    }

    private void renderSkyBox(Camera camera) {
        skyBoxShader.bind();
        bindSkyBox(skyBox, camera);

        GL11.glDrawElements(GL11.GL_TRIANGLES, skyBox.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        skyBoxShader.unBind();
    }

    private void renderOpaqueChunks(Matrix4f projectionMatrix, Matrix4f viewMatrix, float passedTicks) {
        blockShader.bind();
        blockShader.setUniform("projectionMatrix", projectionMatrix);
        blockShader.setUniform("viewMatrix", viewMatrix);
        blockShader.setUniform("textureSampler", 0);
        blockShader.setUniform("time", time + TIME_SPEED * passedTicks);
        blockShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        blockShader.setUniform("cameraPosition", player.getCamera().getPosition());

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.id());

        for (OpaqueModel model : chunkModels) {
            int[] toRenderVertexCounts;

            if (model.getDistanceFromPlayer(playerChunkX, playerChunkY, playerChunkZ) > 2) {
                toRenderVertexCounts = model.getLowDetailVertexCounts(playerChunkX, playerChunkY, playerChunkZ);
            } else {
                toRenderVertexCounts = model.getSolidOnlyVertexCounts(playerChunkX, playerChunkY, playerChunkZ);
                if (model.getFoliageVertexCount() != 0) processFoliageModel(model);
                if (model.getSolidVertexCount(toRenderVertexCounts) == 0) continue;
            }

            bindModel(model);
            GL14.glMultiDrawElements(GL11.GL_TRIANGLES, toRenderVertexCounts, GL11.GL_UNSIGNED_INT, model.getIndices());
        }
        blockShader.unBind();
    }

    private void renderFoliageChunks(Matrix4f projectionMatrix, Matrix4f viewMatrix, float passedTicks) {
        foliageShader.bind();
        foliageShader.setUniform("projectionMatrix", projectionMatrix);
        foliageShader.setUniform("viewMatrix", viewMatrix);
        foliageShader.setUniform("textureSampler", 0);
        foliageShader.setUniform("time", time + TIME_SPEED * passedTicks);
        foliageShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        foliageShader.setUniform("cameraPosition", player.getCamera().getPosition());

        GL11.glDisable(GL11.GL_CULL_FACE);

        for (OpaqueModel model : foliageModels) {
            bindFoliageModel(model);
            int[] vertexCounts = model.getFoliageOnlyVertexCounts();

            GL14.glMultiDrawElements(GL11.GL_TRIANGLES, vertexCounts, GL11.GL_UNSIGNED_INT, model.getIndices());
        }
        foliageShader.unBind();
    }

    private void renderEntities(Matrix4f projectionMatrix, Matrix4f viewMatrix, float passedTicks) {
        if (entities.isEmpty()) return;

        long renderTime = System.nanoTime();

        entityShader.bind();
        entityShader.setUniform("projectionMatrix", projectionMatrix);
        entityShader.setUniform("viewMatrix", viewMatrix);
        entityShader.setUniform("time", time + TIME_SPEED * passedTicks);
        entityShader.setUniform("textureSampler", 0);
        entityShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        entityShader.setUniform("cameraPosition", player.getCamera().getPosition());

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.id());
        GL11.glEnable(GL11.GL_CULL_FACE);
        resizeEntityBuffers();

        int index = 0;

        for (Entity entity : entities) {
            Vector3f position = entity.getToRenderPosition();
            Vector3f velocity = entity.getVelocity();
            float[] aabb = entity.getAabb();
            float[] rotations = entity.getToRenderRotations();
            float[] rotationSpeeds = entity.getRotationSpeeds();
            float timeScalar = 1.0f / TARGET_TPS - passedTicks;

            for (int aabbIndex = 0; aabbIndex < aabb.length; aabbIndex += 6) {

//                entityTranslations[index] = aabb[aabbIndex + MIN_X];
//                entityTranslations[index + 1] = aabb[aabbIndex + MIN_Y];
//                entityTranslations[index + 2] = aabb[aabbIndex + MIN_Z];

                entityPositions[index] = position.x - timeScalar * velocity.x;
                entityPositions[index + 1] = position.y - timeScalar * velocity.y;
                entityPositions[index + 2] = position.z - timeScalar * velocity.z;

                int width = (int) ((aabb[aabbIndex + MAX_X] - aabb[aabbIndex + MIN_X]) * 16.0f) & 0xFF;
                int height = (int) ((aabb[aabbIndex + MAX_Y] - aabb[aabbIndex + MIN_Y]) * 16.0f) & 0xFF;
                int depth = (int) ((aabb[aabbIndex + MAX_Z] - aabb[aabbIndex + MIN_Z]) * 16.0f) & 0xFF;
                int light = Chunk.getLightInWorld(Utils.floor(position.x), Utils.floor(position.y), Utils.floor(position.z)) & 0xFF;

                entityInts[index] = width << 24 | height << 16 | depth << 8 | light;
                entityInts[index + 1] = entity.getTextureUV(NORTH, aabbIndex) << 16 | entity.getTextureUV(TOP, aabbIndex) & 0xFFFF;
                entityInts[index + 2] = entity.getTextureUV(WEST, aabbIndex) << 16 | entity.getTextureUV(SOUTH, aabbIndex) & 0xFFFF;
                entityInts[index + 3] = entity.getTextureUV(BOTTOM, aabbIndex) << 16 | entity.getTextureUV(EAST, aabbIndex) & 0xFFFF;

                int rotationIndex = aabbIndex / 3;
                entityRotations[index + ROTATE_AROUND_X] = rotations[rotationIndex + ROTATE_AROUND_X] - timeScalar * rotationSpeeds[rotationIndex + ROTATE_AROUND_X];
                entityRotations[index + ROTATE_AROUND_Y] = rotations[rotationIndex + ROTATE_AROUND_Y] - timeScalar * rotationSpeeds[rotationIndex + ROTATE_AROUND_Y];

                entityPositions[index + 3] = aabb[aabbIndex + MIN_X];
                entityRotations[index + 2] = aabb[aabbIndex + MIN_Y];
                entityRotations[index + 3] = aabb[aabbIndex + MIN_Z];

                index += 4;
            }
        }
        int boxCount = index >> 2;

        GL46.glNamedBufferSubData(entityPositionBuffer, 0, entityPositions);
        GL46.glNamedBufferSubData(entityRotationBuffer, 0, entityRotations);
        GL46.glNamedBufferSubData(entityIntsBuffers, 0, entityInts);
//        GL46.glNamedBufferSubData(entityTranslationsBuffer, 0, entityTranslations);

        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 0, entityPositionBuffer);
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 1, entityRotationBuffer);
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 2, entityIntsBuffers);
//        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 2, entityTranslationsBuffer);

        GL30.glBindVertexArray(Entity.vao);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL31.glDrawElementsInstanced(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0, boxCount);

        entityShader.unBind();

        if (player.printTimes) System.out.println("Entities " + (System.nanoTime() - renderTime));
    }

    private void renderParticles(Matrix4f projectionMatrix, Matrix4f viewMatrix, float passedTicks) {
        if (particles.isEmpty()) return;

        long renderTime = System.nanoTime();

        particleShader.bind();
        particleShader.setUniform("projectionMatrix", projectionMatrix);
        particleShader.setUniform("viewMatrix", viewMatrix);
        particleShader.setUniform("textureSampler", 0);
        particleShader.setUniform("cameraPosition", player.getCamera().getPosition());
        particleShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        particleShader.setUniform("time", time + TIME_SPEED * passedTicks);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.id());

        long currentTime = System.nanoTime();

        for (Particle particle : particles) particle.render(particleShader, currentTime, modelIndexBuffer);

        particleShader.unBind();

        if (player.printTimes) System.out.println("Particles " + (System.nanoTime() - renderTime));
    }

    private void renderWaterChunks(Matrix4f projectionMatrix, Matrix4f viewMatrix, float passedTicks) {
        waterShader.bind();
        waterShader.setUniform("projectionMatrix", projectionMatrix);
        waterShader.setUniform("viewMatrix", viewMatrix);
        waterShader.setUniform("textureSampler", 0);
        waterShader.setUniform("time", time + TIME_SPEED * passedTicks);
        waterShader.setUniform("headUnderWater", headUnderWater ? 1 : 0);
        waterShader.setUniform("cameraPosition", player.getCamera().getPosition());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_CULL_FACE);

        for (WaterModel waterModel : waterModels) {
            bindWaterModel(waterModel);

            GL11.glDrawElements(GL11.GL_TRIANGLES, (int) (waterModel.vertexCount * 0.75), GL11.GL_UNSIGNED_INT, 0);
        }
        GL11.glDisable(GL11.GL_BLEND);
        waterShader.unBind();
    }

    private void renderGUIElements() {
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
        GUIShader.unBind();

        if (player.isInInventory()) {
            textShader.bind();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textAtlas.id());

            textShader.setUniform("screenSize", Launcher.getWindow().getWidth() >> 1, Launcher.getWindow().getHeight() >> 1);
            textShader.setUniform("charSize", TEXT_CHAR_SIZE_X, TEXT_CHAR_SIZE_Y);

            for (DisplayString string : displayStrings) renderDisplayString(string);
            displayStrings.clear();

            textShader.unBind();
        }
    }

    private void renderDebugText() {
        textShader.bind();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textAtlas.id());

        textShader.setUniform("screenSize", Launcher.getWindow().getWidth() >> 1, Launcher.getWindow().getHeight() >> 1);
        textShader.setUniform("charSize", TEXT_CHAR_SIZE_X, TEXT_CHAR_SIZE_Y);
        textShader.setUniform("xOffset", 0);

        int line = -1;
        final Vector3f position = player.getCamera().getPosition();
        final Vector3f direction = player.getCamera().getDirection();
        final Vector3f velocity = player.getMovement().getVelocity();

        Target target = Target.getTarget(position, direction);

        int x = Utils.floor(position.x), y = Utils.floor(position.y), z = Utils.floor(position.z);
        int chunkX = x >> CHUNK_SIZE_BITS, chunkY = y >> CHUNK_SIZE_BITS, chunkZ = z >> CHUNK_SIZE_BITS;
        int inChunkX = x & CHUNK_SIZE_MASK, inChunkY = y & CHUNK_SIZE_MASK, inChunkZ = z & CHUNK_SIZE_MASK;
        Chunk chunk = Chunk.getChunk(chunkX, chunkY, chunkZ);
        int sourceCounter = 0;
        for (AudioSource source : Launcher.getSound().getSources()) if (source.isPlaying()) sourceCounter++;
        double heightMapValue = GenerationData.heightMapValue(x, z);
        double erosionMapValue = GenerationData.erosionMapValue(x, z);
        double continentalMapValue = GenerationData.continentalMapValue(x, z);
        double riverMapValue = GenerationData.riverMapValue(x, z);
        double ridgeMapValue = GenerationData.ridgeMapValue(x, y);
        double temperatureMapValue = GenerationData.temperatureMapValue(x, z);
        double humidityMapValue = GenerationData.humidityMapValue(x, z);

        renderTextLine("Frame rate:" + EngineManager.currentFrameRate + " last GT-time:" + Launcher.getServer().getLastGameTickProcessingTime() / 1_000_000 + "ms" + " current GT-time:" + Launcher.getServer().getDeltaTime() / 1_000_000 + "ms", Color.RED, ++line);
        renderTextLine("Memory:" + (Runtime.getRuntime().totalMemory() / 1_000_000) + "MB", Color.RED, ++line);
        renderTextLine("Coordinates: X:" + Utils.floor(position.x * 10) / 10f + " Y:" + Utils.floor(position.y * 10) / 10f + " Z:" + Utils.floor(position.z * 10) / 10f, Color.BLUE, ++line);
        renderTextLine("Chunk coordinates: X:" + chunkX + " Y:" + chunkY + " Z:" + chunkZ + " Id" + Utils.getChunkId(chunkX, chunkY, chunkZ), Color.BLUE, ++line);
        renderTextLine("In Chunk coordinates: X:" + inChunkX + " Y:" + inChunkY + " Z:" + inChunkZ, Color.BLUE, ++line);
        renderTextLine("Looking at: X:" + Utils.floor(direction.x * 100) / 100f + " Y:" + Utils.floor(direction.y * 100) / 100f + " Z:" + Utils.floor(direction.z * 100) / 100f, Color.CYAN, ++line);
        renderTextLine("Velocity: X:" + velocity.x + " Y:" + velocity.y + " Z:" + velocity.z, Color.CYAN, ++line);
        if (chunk != null) {
            renderTextLine("OcclusionCullingData:" + Integer.toBinaryString(Chunk.getOcclusionCullingData(chunk.getIndex()) & 0x7FFF) + " Damping:" + (Chunk.getOcclusionCullingDamper(Chunk.getOcclusionCullingData(chunk.getIndex())) == 0 ? "false" : "true"), Color.ORANGE, ++line);
            renderTextLine("Block optimized:" + (chunk.isBlockOptimized() ? "true" : "false") + " Light optimized:" + (chunk.isLightOptimized() ? "true" : "false"), Color.ORANGE, ++line);
            renderTextLine("HeightMap:" + Chunk.getHeightMap(chunkX, chunkZ).map[inChunkX << CHUNK_SIZE_BITS | inChunkZ], Color.GREEN, ++line);
            renderTextLine("BlockLight:" + Chunk.getBlockLightInWorld(x, y, z) + " SkyLight:" + Chunk.getSkyLightInWorld(x, y, z), Color.GREEN, ++line);
            renderTextLine("Block in Head:" + Block.getFullBlockName(Chunk.getBlockInWorld(x, y, z)), Color.GREEN, ++line);
        }
        if (target != null) {
            renderTextLine("Looking at block: X:" + target.position().x + " Y:" + target.position().y + " Z:" + target.position().z, Color.GRAY, ++line);
            renderTextLine("Intersection: X:" + target.inBlockPosition().x + " Y:" + target.inBlockPosition().y + " Z:" + target.inBlockPosition().z, Color.GRAY, ++line);
            renderTextLine("Block:" + Block.getFullBlockName(target.block()), Color.GRAY, ++line);
            renderTextLine("Intersected side:" + target.side(), Color.GRAY, ++line);
        }
        if (player.getMovement().isGrounded()) {
            renderTextLine("Standing on block:" + Block.getFullBlockName(player.getMovement().getStandingBlock()), Color.WHITE, ++line);
        }
        renderTextLine("Seed:" + SEED, Color.GREEN, ++line);
        renderTextLine("Rendered chunk models:" + chunkModels.size() + "/" + Chunk.countOpaqueModels(), Color.RED, ++line);
        renderTextLine("Rendered water models:" + waterModels.size() + "/" + Chunk.countWaterModels(), Color.RED, ++line);
        renderTextLine("Rendered foliage models:" + foliageModels.size(), Color.RED, ++line);
        renderTextLine("Rendered entities:" + entities.size() + "/" + ServerLogic.getAmountOfEntities() + " [" + (entityPositions.length >> 2) + "]", Color.RED, ++line);
        renderTextLine("Rendered particles:" + particles.size() + "/" + ServerLogic.getAmountOfParticles(), Color.RED, ++line);
        renderTextLine("Rendered GUIElements:" + GUIElements.size(), Color.RED, ++line);
        renderTextLine("Render distance XZ:" + RENDER_DISTANCE_XZ + " Render distance Y:" + RENDER_DISTANCE_Y, Color.ORANGE, ++line);
        renderTextLine("Concurrent played sounds:" + sourceCounter, Color.YELLOW, ++line);
        renderTextLine("Tick:" + EngineManager.getTick() + " Time:" + time, Color.WHITE, ++line);
        renderTextLine("To buffer chunks:" + ServerLogic.getAmountOfToBufferChunks(), Color.RED, ++line);
        renderTextLine("Scheduled blockEvents:" + BlockEvent.getAmountOfScheduledEvents(EngineManager.getTick()), Color.RED, ++line);
        renderTextLine("Entities:" + ServerLogic.getAmountOfEntities(), Color.RED, ++line);
        renderTextLine("Hei:" + Utils.floor(heightMapValue * 1000) / 1000d + " Ero:" + Utils.floor(erosionMapValue * 1000) / 1000d + " Con:" + Utils.floor(continentalMapValue * 1000) / 1000d, Color.GRAY, ++line);
        renderTextLine("Riv:" + Utils.floor(riverMapValue * 1000) / 1000d + " Rid:" + Utils.floor(ridgeMapValue * 1000) / 1000d, Color.GRAY, ++line);
        renderTextLine("Tem:" + Utils.floor(temperatureMapValue * 1000) / 1000d + " Hum:" + Utils.floor(humidityMapValue * 1000) / 1000d, Color.GRAY, ++line);
        renderTextLine("Resulting height: " + WorldGeneration.getResultingHeight(heightMapValue, erosionMapValue, continentalMapValue, riverMapValue, ridgeMapValue), Color.GRAY, ++line);

        textShader.unBind();
    }

    private void renderTextLine(String text, Color color, int textLine) {
        textShader.setUniform("string", toIntFormat(text));
        textShader.setUniform("yOffset", textLine * TEXT_LINE_SPACING);
        textShader.setUniform("color", color);

        GL30.glBindVertexArray(textRowVertexArray);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, MAX_TEXT_LENGTH * 6, GL11.GL_UNSIGNED_INT, 0);
    }

    private void renderDisplayString(DisplayString string) {
        if (string.string() == null) return;
        textShader.setUniform("string", toIntFormat(string.string()));
        textShader.setUniform("yOffset", string.y());
        textShader.setUniform("xOffset", string.x());
        textShader.setUniform("color", Color.WHITE);

        GL30.glBindVertexArray(textRowVertexArray);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 384, GL11.GL_UNSIGNED_INT, 0);
    }

    private int[] toIntFormat(String text) {
        int[] array = new int[MAX_TEXT_LENGTH];

        byte[] stringBytes = text.getBytes(StandardCharsets.UTF_8);

        for (int index = 0, max = Math.min(text.length(), MAX_TEXT_LENGTH); index < max; index++) {
            array[index] = stringBytes[index];
        }
        return array;
    }

    private void resizeEntityBuffers() {
        int boxCount = 0;
        for (Entity entity : entities) boxCount += entity.getAabb().length / 6;

        int length = entityPositions.length, requiredLength = boxCount << 2;

        while (true) {
            if (requiredLength > length) length <<= 1;
            else if (requiredLength < length / 3) length >>= 1;
            else break;
        }

        if (length != entityPositions.length) {
            int arrayLength = Math.max(1, length);
            entityPositions = new float[arrayLength];
            entityRotations = new float[arrayLength];
            entityInts = new int[arrayLength];
//            entityTranslations = new float[arrayLength];

            GL46.glDeleteBuffers(entityPositionBuffer);
            GL46.glDeleteBuffers(entityRotationBuffer);
            GL46.glDeleteBuffers(entityIntsBuffers);
//            GL46.glDeleteBuffers(entityTranslationsBuffer);

            createEntityBuffers(length >> 2);
        }
    }

    public void processParticle(Particle particle) {
        particles.add(particle);
    }

    public void processEntity(Entity entity) {
        entities.add(entity);
    }

    public void processOpaqueModel(OpaqueModel model) {
        chunkModels.add(model);
    }

    private void processFoliageModel(OpaqueModel foliageModel) {
        foliageModels.add(foliageModel);
    }

    public void processWaterModel(WaterModel waterModel) {
        waterModels.add(waterModel);
    }

    public void processSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public void processGUIElement(GUIElement element) {
        GUIElements.add(element);
    }

    public void processDisplayString(DisplayString string) {
        displayStrings.add(string);
    }

    public void setHeadUnderWater(boolean headUnderWater) {
        this.headUnderWater = headUnderWater;
    }

    public void setInventoryOverlay(GUIElement inventoryOverlay) {
        this.inventoryOverlay = inventoryOverlay;
    }

    private void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanUp() {
        blockShader.cleanUp();
        skyBoxShader.cleanUp();
        GUIShader.cleanUp();
    }

    public void setXRay(boolean xRay) {
        this.xRay = xRay;
        if (xRay) GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        else GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public boolean isxRay() {
        return xRay;
    }

    public void incrementTime() {
        time += TIME_SPEED;
        if (time > 1.0f) time -= 2.0f;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    private final WindowManager window;
    private ShaderManager blockShader, waterShader, foliageShader, skyBoxShader, GUIShader, textShader, entityShader, particleShader;

    private final ArrayList<OpaqueModel> chunkModels = new ArrayList<>();
    private final ArrayList<OpaqueModel> foliageModels = new ArrayList<>();
    private final ArrayList<WaterModel> waterModels = new ArrayList<>();
    private final ArrayList<Entity> entities = new ArrayList<>();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ArrayList<GUIElement> GUIElements = new ArrayList<>();
    private final ArrayList<DisplayString> displayStrings = new ArrayList<>();
    private final Player player;
    private GUIElement inventoryOverlay;
    private SkyBox skyBox;
    private boolean headUnderWater = false;

    private float time = 1.0f;
    private int playerChunkX, playerChunkY, playerChunkZ;

    private int modelIndexBuffer;
    private int textRowVertexArray;
    private int entityPositionBuffer, entityRotationBuffer, entityIntsBuffers;
    private float[] entityPositions = new float[1];
    private float[] entityRotations = new float[1];
    private int[] entityInts = new int[1];

    private Texture atlas;
    private Texture textAtlas;
    private boolean xRay;
}
