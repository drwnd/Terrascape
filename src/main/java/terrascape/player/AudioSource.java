package terrascape.player;

import org.lwjgl.openal.AL10;

import static terrascape.utils.Settings.*;

public final class AudioSource {

    public AudioSource() {
        id = AL10.alGenSources();
        AL10.alSourcef(id, AL10.AL_GAIN, AUDIO_GAIN);
        AL10.alSourcef(id, AL10.AL_PITCH, 1);
    }

    public void setPosition(float x, float y, float z) {
        AL10.alSource3f(id, AL10.AL_POSITION, x, y, z);
    }

    public void setVelocity(float x, float y, float z) {
        AL10.alSource3f(id, AL10.AL_VELOCITY, x, y, z);
    }

    public void setGain(float gain) {
        AL10.alSourcef(id, AL10.AL_GAIN, gain);
    }

    public void play(int buffer) {
        AL10.alSourcei(id, AL10.AL_BUFFER, buffer);
        AL10.alSourcePlay(id);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void delete() {
        AL10.alDeleteSources(id);
    }

    private final int id;
}
