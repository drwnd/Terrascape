package terrascape.entity.entities;

import terrascape.server.*;
import terrascape.dataStorage.Chunk;
import terrascape.entity.particles.ExplosionParticle;
import terrascape.player.SoundManager;
import terrascape.utils.Utils;
import terrascape.server.ServerLogic;
import terrascape.server.Launcher;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.LinkedList;

import static terrascape.utils.Constants.*;
import static terrascape.utils.Settings.*;

public final class TNT_Entity extends Entity {

    public static final int STANDARD_TNT_FUSE = 80;

    public TNT_Entity(int fuse, Vector3f position, Vector3f velocity) {
        this.fuse = fuse;
        this.position = position;
        this.velocity = velocity;
        aabb = TNT_AABB;
        rotations = new float[aabb.length >> 1];
    }

    @Override
    public void update() {
        fuse--;
        move();
        lookAtMovementDirection();

        if (fuse <= 0) {
            explode();
            delete();
        }
    }

    @Override
    public void delete() {
        isDead = true;
    }

    @Override
    public short getTextureUV(int side, int aabbIndex) {
        return switch (side) {
            case TOP -> (6 * 16 & 0xFF) << 8 | (8 * 16 & 0xFF);
            case NORTH, WEST, SOUTH, EAST -> (6 * 16 & 0xFF) << 8 | (9 * 16 & 0xFF);
            case BOTTOM -> (6 * 16 & 0xFF) << 8 | (10 * 16 & 0xFF);
            default -> 0;
        };
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[getByteSize()];
        bytes[0] = TNT_ENTITY_TYPE;
        putBaseByteData(bytes);
        System.arraycopy(Utils.toByteArray(fuse), 0, bytes, BASE_BYTE_SIZE, 4);
        return bytes;
    }

    @Override
    public int getByteSize() {
        return BASE_BYTE_SIZE + 4;
    }

    private void explode() {
        castExplosionRay(position, -0.8164965809277261, -0.4082482904638631, -0.4082482904638631);
        castExplosionRay(position, -0.8944271909999159, -0.4472135954999579, 0.0);
        castExplosionRay(position, -0.8164965809277261, -0.4082482904638631, 0.4082482904638631);
        castExplosionRay(position, -0.8944271909999159, 0.0, -0.4472135954999579);
        castExplosionRay(position, -1.0, 0.0, 0.0);
        castExplosionRay(position, -0.8944271909999159, 0.0, 0.4472135954999579);
        castExplosionRay(position, -0.8164965809277261, 0.4082482904638631, -0.4082482904638631);
        castExplosionRay(position, -0.8944271909999159, 0.4472135954999579, 0.0);
        castExplosionRay(position, -0.8164965809277261, 0.4082482904638631, 0.4082482904638631);
        castExplosionRay(position, -0.4082482904638631, -0.8164965809277261, -0.4082482904638631);
        castExplosionRay(position, -0.4472135954999579, -0.8944271909999159, 0.0);
        castExplosionRay(position, -0.4082482904638631, -0.8164965809277261, 0.4082482904638631);
        castExplosionRay(position, -0.4082482904638631, -0.4082482904638631, -0.8164965809277261);
        castExplosionRay(position, -0.4082482904638631, -0.4082482904638631, 0.8164965809277261);
        castExplosionRay(position, -0.4472135954999579, 0.0, -0.8944271909999159);
        castExplosionRay(position, -0.4472135954999579, 0.0, 0.8944271909999159);
        castExplosionRay(position, -0.4082482904638631, 0.4082482904638631, -0.8164965809277261);
        castExplosionRay(position, -0.4082482904638631, 0.4082482904638631, 0.8164965809277261);
        castExplosionRay(position, -0.4082482904638631, 0.8164965809277261, -0.4082482904638631);
        castExplosionRay(position, -0.4472135954999579, 0.8944271909999159, 0.0);
        castExplosionRay(position, -0.4082482904638631, 0.8164965809277261, 0.4082482904638631);
        castExplosionRay(position, 0.0, -0.8944271909999159, -0.4472135954999579);
        castExplosionRay(position, 0.0, -1.0, 0.0);
        castExplosionRay(position, 0.0, -0.8944271909999159, 0.4472135954999579);
        castExplosionRay(position, 0.0, -0.4472135954999579, -0.8944271909999159);
        castExplosionRay(position, 0.0, -0.4472135954999579, 0.8944271909999159);
        castExplosionRay(position, 0.0, 0.0, -1.0);
        castExplosionRay(position, 0.0, 0.0, 1.0);
        castExplosionRay(position, 0.0, 0.4472135954999579, -0.8944271909999159);
        castExplosionRay(position, 0.0, 0.4472135954999579, 0.8944271909999159);
        castExplosionRay(position, 0.0, 0.8944271909999159, -0.4472135954999579);
        castExplosionRay(position, 0.0, 1.0, 0.0);
        castExplosionRay(position, 0.0, 0.8944271909999159, 0.4472135954999579);
        castExplosionRay(position, 0.4082482904638631, -0.8164965809277261, -0.4082482904638631);
        castExplosionRay(position, 0.4472135954999579, -0.8944271909999159, 0.0);
        castExplosionRay(position, 0.4082482904638631, -0.8164965809277261, 0.4082482904638631);
        castExplosionRay(position, 0.4082482904638631, -0.4082482904638631, -0.8164965809277261);
        castExplosionRay(position, 0.4082482904638631, -0.4082482904638631, 0.8164965809277261);
        castExplosionRay(position, 0.4472135954999579, 0.0, -0.8944271909999159);
        castExplosionRay(position, 0.4472135954999579, 0.0, 0.8944271909999159);
        castExplosionRay(position, 0.4082482904638631, 0.4082482904638631, -0.8164965809277261);
        castExplosionRay(position, 0.4082482904638631, 0.4082482904638631, 0.8164965809277261);
        castExplosionRay(position, 0.4082482904638631, 0.8164965809277261, -0.4082482904638631);
        castExplosionRay(position, 0.4472135954999579, 0.8944271909999159, 0.0);
        castExplosionRay(position, 0.4082482904638631, 0.8164965809277261, 0.4082482904638631);
        castExplosionRay(position, 0.8164965809277261, -0.4082482904638631, -0.4082482904638631);
        castExplosionRay(position, 0.8944271909999159, -0.4472135954999579, 0.0);
        castExplosionRay(position, 0.8164965809277261, -0.4082482904638631, 0.4082482904638631);
        castExplosionRay(position, 0.8944271909999159, 0.0, -0.4472135954999579);
        castExplosionRay(position, 1.0, 0.0, 0.0);
        castExplosionRay(position, 0.8944271909999159, 0.0, 0.4472135954999579);
        castExplosionRay(position, 0.8164965809277261, 0.4082482904638631, -0.4082482904638631);
        castExplosionRay(position, 0.8944271909999159, 0.4472135954999579, 0.0);
        castExplosionRay(position, 0.8164965809277261, 0.4082482904638631, 0.4082482904638631);

        pushEntities();
        ServerLogic.addParticle(new ExplosionParticle(new Vector3f(position.x, position.y + 0.375f, position.z)));

        SoundManager sound = Launcher.getSound();
        sound.playRandomSound(sound.explode, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z, MISCELLANEOUS_GAIN);
    }

    private static void castExplosionRay(Vector3f origin, double dirX, double dirY, double dirZ) {
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

        while (blastResistance < TNT_Entity.EXPLOSION_STRENGTH && length < 8.0) {
            short block = Chunk.getBlockInWorld(x, y, z);
            int blockType = Block.getBlockType(block);
            if ((Block.getBlockProperties(block) & BLAST_RESISTANT) != 0 || Block.isWaterLogged(block)) return;
            if (blockType != AIR_TYPE) {
                blastResistance++;
                ServerLogic.placeBlock(AIR, x, y, z, false);
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

    private void pushEntities() {
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

    public static void spawnTNTEntity(Vector3i targetPosition, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.5f, targetPosition.z + 0.5f);
        Vector3f velocity = new Vector3f((float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15), (float) (Math.random() * 0.3 - 0.15));
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        ServerLogic.spawnEntity(entity);
        ServerLogic.placeBlock(AIR, targetPosition.x, targetPosition.y, targetPosition.z, true);
    }

    public static void spawnTNTEntity(Vector3i targetPosition, Vector3f velocity, int fuse) {
        Vector3f position = new Vector3f(targetPosition.x + 0.5f, targetPosition.y + 0.5f, targetPosition.z + 0.5f);
        TNT_Entity entity = new TNT_Entity(fuse, position, velocity);
        ServerLogic.spawnEntity(entity);
    }

    static TNT_Entity getFromBytesCustom(byte[] bytes, int startIndex) {
        int fuse = Utils.getInt(bytes, startIndex);

        return new TNT_Entity(fuse, null, null);
    }

    private int fuse;

    private static final float[] TNT_AABB = new float[]{-0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f};
    private static final int EXPLOSION_STRENGTH = 2;
}
