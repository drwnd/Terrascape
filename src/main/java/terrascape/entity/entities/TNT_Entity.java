package terrascape.entity.entities;

import terrascape.core.*;
import terrascape.dataStorage.Chunk;
import terrascape.entity.particles.ExplosionParticle;
import terrascape.utils.Utils;
import terrascape.core.GameLogic;
import terrascape.core.Launcher;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.LinkedList;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public class TNT_Entity extends Entity {
    private static final float[] TNT_AABB = new float[]{-0.5f, 0.5f, -0.25f, 0.75f, -0.5f, 0.5f};
    private static final int EXPLOSION_STRENGTH = 2;
    public static int vao, vbo;

    private int fuse;

    public TNT_Entity(int fuse, Vector3f position, Vector3f velocity) {
        this.fuse = fuse;
        this.position = position;
        this.velocity = velocity;
        aabb = TNT_AABB;
    }

    @Override
    public void update() {
        fuse--;
        move();

        if (fuse <= 0) {
            explode();
            delete();
        }
    }

    @Override
    protected void renderUnique(ShaderManager shader, int modelIndexBuffer, float timeSinceLastTick) {
        GL30.glBindVertexArray(vao);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, modelIndexBuffer);

        GL11.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void delete() {
        isDead = true;
    }

    public static void init() {
        long vao_vbo = ObjectLoader.loadVAO_VBO(0, 2, TNT_Entity.TNTEntityVertices());
        vao = (int) (vao_vbo >> 32 & 0xFFFFFFFFL);
        vbo = (int) (vao_vbo & 0xFFFFFFFFL);
    }

    public void explode() {
        castExplosionRay(position, -0.8164965809277261, -0.4082482904638631, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8944271909999159, -0.4472135954999579, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8164965809277261, -0.4082482904638631, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8944271909999159, 0.0, -0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, -1.0, 0.0, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8944271909999159, 0.0, 0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8164965809277261, 0.4082482904638631, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8944271909999159, 0.4472135954999579, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.8164965809277261, 0.4082482904638631, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, -0.8164965809277261, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4472135954999579, -0.8944271909999159, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, -0.8164965809277261, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, -0.4082482904638631, -0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, -0.4082482904638631, 0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4472135954999579, 0.0, -0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4472135954999579, 0.0, 0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, 0.4082482904638631, -0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, 0.4082482904638631, 0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, 0.8164965809277261, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4472135954999579, 0.8944271909999159, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, -0.4082482904638631, 0.8164965809277261, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, -0.8944271909999159, -0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, -1.0, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, -0.8944271909999159, 0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, -0.4472135954999579, -0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, -0.4472135954999579, 0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.0, -1.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.0, 1.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.4472135954999579, -0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.4472135954999579, 0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.8944271909999159, -0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 1.0, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.0, 0.8944271909999159, 0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, -0.8164965809277261, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4472135954999579, -0.8944271909999159, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, -0.8164965809277261, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, -0.4082482904638631, -0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, -0.4082482904638631, 0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4472135954999579, 0.0, -0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4472135954999579, 0.0, 0.8944271909999159, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, 0.4082482904638631, -0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, 0.4082482904638631, 0.8164965809277261, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, 0.8164965809277261, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4472135954999579, 0.8944271909999159, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.4082482904638631, 0.8164965809277261, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8164965809277261, -0.4082482904638631, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8944271909999159, -0.4472135954999579, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8164965809277261, -0.4082482904638631, 0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8944271909999159, 0.0, -0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 1.0, 0.0, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8944271909999159, 0.0, 0.4472135954999579, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8164965809277261, 0.4082482904638631, -0.4082482904638631, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8944271909999159, 0.4472135954999579, 0.0, EXPLOSION_STRENGTH);
        castExplosionRay(position, 0.8164965809277261, 0.4082482904638631, 0.4082482904638631, EXPLOSION_STRENGTH);

        pushEntities();
        GameLogic.addParticle(new ExplosionParticle(new Vector3f(position.x, position.y + 0.375f, position.z)));

        SoundManager sound = Launcher.getSound();
        sound.playRandomSound(sound.explode, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, MISCELLANEOUS_GAIN);
    }

    public static void castExplosionRay(Vector3f origin, double dirX, double dirY, double dirZ, int blastStrength) {
        int x = Utils.floor(origin.x);
        int y = Utils.floor(origin.y);
        int z = Utils.floor(origin.z);

        int xDir = dirX < 0 ? -1 : 1;
        int yDir = dirY < 0 ? -1 : 1;
        int zDir = dirZ < 0 ? -1 : 1;

        double dirXSquared = dirX * dirX;
        double dirYSquared = dirY * dirY;
        double dirZSquared = dirZ * dirZ;
        double xUnit = (float) Math.sqrt(1 + (dirYSquared + dirZSquared) / dirXSquared);
        double yUnit = (float) Math.sqrt(1 + (dirXSquared + dirZSquared) / dirYSquared);
        double zUnit = (float) Math.sqrt(1 + (dirXSquared + dirYSquared) / dirZSquared);

        double lengthX = xUnit * (dirX < 0 ? Utils.fraction(origin.x) : 1 - Utils.fraction(origin.x));
        double lengthY = yUnit * (dirY < 0 ? Utils.fraction(origin.y) : 1 - Utils.fraction(origin.y));
        double lengthZ = zUnit * (dirZ < 0 ? Utils.fraction(origin.z) : 1 - Utils.fraction(origin.z));

        double length = 0;
        int blastResistance = 0;

        while (blastResistance < blastStrength && length < 8.0) {
            short block = Chunk.getBlockInWorld(x, y, z);
            int blockType = Block.getBlockType(block);
            if ((Block.getBlockProperties(block) & BLAST_RESISTANT) != 0) return;
            if (blockType != AIR_TYPE) {
                blastResistance++;
                GameLogic.placeBlock(AIR, x, y, z, false);
                if (block == TNT) {
                    Vector3i targetPosition = new Vector3i(x, y, z);
                    float deltaX = x + 0.5f - origin.x;
                    float deltaY = y + 0.5f - origin.y;
                    float deltaZ = z + 0.5f - origin.z;
                    float modifier = Math.min(1.0f, 1.0f / (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));
                    Vector3f velocity = new Vector3f(deltaX * modifier, deltaY * modifier, deltaZ * modifier);
                    spawnTNTEntity(targetPosition, velocity, 10 + (int) (Math.random() * 70));
                }
            }

            if (lengthX <= lengthZ && lengthX <= lengthY) {
                x += xDir;
                length = lengthX;
                lengthX += xUnit;
            } else if (lengthZ <= lengthX && lengthZ <= lengthY) {
                z += zDir;
                length = lengthZ;
                lengthZ += zUnit;
            } else {
                y += yDir;
                length = lengthY;
                lengthY += yUnit;
            }
        }
    }

    public void pushEntities() {
        int currentClusterX = Utils.floor(position.x) >> ENTITY_CLUSTER_SIZE_BITS;
        int currentClusterY = Utils.floor(position.y) >> ENTITY_CLUSTER_SIZE_BITS;
        int currentClusterZ = Utils.floor(position.z) >> ENTITY_CLUSTER_SIZE_BITS;

        for (int clusterX = currentClusterX - 1; clusterX <= currentClusterX + 1; clusterX++)
            for (int clusterZ = currentClusterZ - 1; clusterZ <= currentClusterZ + 1; clusterZ++)
                for (int clusterY = currentClusterY - 1; clusterY <= currentClusterY + 1; clusterY++) {

                    LinkedList<Entity> entityCluster = Chunk.getEntityCluster(clusterX, clusterY, clusterZ);
                    if (entityCluster == null) continue;

                    for (Entity entity : entityCluster) {

                        Vector3f position = entity.getPosition();
                        float deltaX = position.x - this.position.x;
                        float deltaY = position.y - this.position.y;
                        float deltaZ = position.z - this.position.z;

                        float distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                        if (distanceSquared < 0.01) continue;

                        float modifier = Math.min(0.5f, 1.0f / distanceSquared);

                        entity.addVelocity(deltaX * modifier, deltaY * modifier, deltaZ * modifier);
                    }
                }

    }

    public static int[] TNTEntityVertices() {
        int sideTL = Byte.toUnsignedInt((byte) -106);
        int sideTR = Byte.toUnsignedInt((byte) -106) + 1;
        int sideBL = Byte.toUnsignedInt((byte) -106) + 16;
        int sideBR = Byte.toUnsignedInt((byte) -106) + 17;

        int topTL = Byte.toUnsignedInt((byte) -122);
        int topTR = Byte.toUnsignedInt((byte) -122) + 1;
        int topBL = Byte.toUnsignedInt((byte) -122) + 16;
        int topBR = Byte.toUnsignedInt((byte) -122) + 17;

        int bottomTL = Byte.toUnsignedInt((byte) -90);
        int bottomTR = Byte.toUnsignedInt((byte) -90) + 1;
        int bottomBL = Byte.toUnsignedInt((byte) -90) + 16;
        int bottomBR = Byte.toUnsignedInt((byte) -90) + 17;

        return new int[]{
                packData((sideTL & 15) << 4, (topTL >> 4) << 4), packData(8, 12, 8),
                packData((sideBL & 15) << 4, (topBL >> 4) << 4), packData(8, 12, -8),
                packData((sideTR & 15) << 4, (topTR >> 4) << 4), packData(-8, 12, 8),
                packData((sideBR & 15) << 4, (topBR >> 4) << 4), packData(-8, 12, -8),

                packData((sideTL & 15) << 4, (bottomTL >> 4) << 4), packData(8, -4, 8),
                packData((sideTR & 15) << 4, (bottomTR >> 4) << 4), packData(-8, -4, 8),
                packData((sideBL & 15) << 4, (bottomBL >> 4) << 4), packData(8, -4, -8),
                packData((sideBR & 15) << 4, (bottomBR >> 4) << 4), packData(-8, -4, -8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, 12, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, -4, 8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(8, 12, -8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(8, -4, -8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(-8, 12, 8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(-8, -4, 8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(8, 12, 8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(8, -4, 8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(-8, 12, -8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(-8, -4, -8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, 12, 8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, -4, 8),

                packData((sideTL & 15) << 4, (sideTL >> 4) << 4), packData(8, 12, -8),
                packData((sideBL & 15) << 4, (sideBL >> 4) << 4), packData(8, -4, -8),
                packData((sideTR & 15) << 4, (sideTR >> 4) << 4), packData(-8, 12, -8),
                packData((sideBR & 15) << 4, (sideBR >> 4) << 4), packData(-8, -4, -8),
        };
    }

    public static int packData(int x, int y, int z) {
        return x + 511 << 20 | y + 511 << 10 | z + 511;
    }

    public static int packData(int u, int v) {
        return u + 15 << 9 | v + 15;
    }

    public static void spawnTNTEntity(Vector3i targetPosition, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.25f, targetPosition.z + 0.5f);
        Vector3f velocity = new Vector3f((float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15));
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        GameLogic.spawnEntity(entity);
        GameLogic.placeBlock(AIR, targetPosition.x, targetPosition.y, targetPosition.z, true);
    }

    public static void spawnTNTEntity(Vector3i targetPosition, Vector3f velocity, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.25f, targetPosition.z + 0.5f);
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        GameLogic.spawnEntity(entity);
    }
}
