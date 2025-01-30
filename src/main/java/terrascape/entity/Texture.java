package terrascape.entity;

import terrascape.player.ObjectLoader;

public record Texture(int id) {

    public static final Texture ATLAS;

    static {
        try {
            ATLAS = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
