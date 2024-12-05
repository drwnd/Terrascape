package terrascape.entity.particles;

import terrascape.dataStorage.Chunk;
import terrascape.core.ShaderManager;
import terrascape.utils.Utils;
import org.joml.Vector3f;

import static terrascape.utils.Constants.*;

public abstract class Particle {

    protected final Vector3f position;
    protected final long emitTime;

    public Particle(Vector3f position) {
        this.position = position;
        emitTime = System.nanoTime();
    }

    public void render(ShaderManager shader, long currentTime, int modelIndexBuffer) {
        int x = Utils.floor(position.x);
        int y = Utils.floor(position.y);
        int z = Utils.floor(position.z);

        shader.setUniform("position", position);
        shader.setUniform("lightLevel", Chunk.getLightInWorld(x, y, z));
        shader.setUniform("particleProperties", 20 * getMaxAliveTime() / NANOSECONDS_PER_SECOND, getParticleSize(), 20 * (currentTime - emitTime) / NANOSECONDS_PER_SECOND, getGravity());

        renderUnique(shader, modelIndexBuffer);
    }

    protected abstract void renderUnique(ShaderManager shaderManager, int modelIndexBuffer);

    public abstract float getGravity();

    public abstract float getMaxAliveTime();

    public abstract float getParticleSize();

    public Vector3f getPosition() {
        return position;
    }

    public long getEmitTime() {
        return emitTime;
    }

    public static void initAll() {
        BlockBreakParticle.init();
        ExplosionParticle.init();
    }
}
