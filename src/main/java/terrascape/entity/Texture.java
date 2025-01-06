package terrascape.entity;

import terrascape.player.ObjectLoader;

public record Texture(int id) {

    public static final Texture atlas;

    static {
        try {
            atlas = new Texture(ObjectLoader.loadTexture("textures/atlas256.png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
