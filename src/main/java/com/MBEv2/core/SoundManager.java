package com.MBEv2.core;

import com.MBEv2.core.entity.AudioSource;
import com.MBEv2.core.entity.Player;
import org.joml.Vector3f;
import org.lwjgl.openal.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static com.MBEv2.core.utils.Constants.*;
import static com.MBEv2.core.utils.Settings.*;

public class SoundManager {

    long device, context;

    public final int[] digGlass = new int[7];
    public final int[] digGrass = new int[4];
    public final int[] digGravel = new int[4];
    public final int[] digSand = new int[4];
    public final int[] digSnow = new int[4];
    public final int[] digStone = new int[7];
    public final int[] digFoliage = new int[4];
    public final int[] digWood = new int[6];
    public final int[] digIce = new int[2];
    public final int[] digCloth = new int[4];

    public final int[] stepGlass = new int[4];
    public final int[] stepGrass = new int[6];
    public final int[] stepGravel = new int[4];
    public final int[] stepSand = new int[5];
    public final int[] stepSnow = new int[4];
    public final int[] stepStone = new int[6];
    public final int[] stepFoliage = new int[5];
    public final int[] stepWood = new int[6];
    public final int[] swim = new int[5];
    public final int[] stepDirt = new int[4];
    public final int[] stepCloth = new int[4];

    public final int[] explode = new int[3];
    public final int[] splash = new int[1];
    public final int[] lavaPop = new int[1];
    public final int[] submerge = new int[4];

    public int fuse;

    private final AudioSource[] sources;
    private final Vector3f listenerPosition = new Vector3f();

    public SoundManager() {
        sources = new AudioSource[128];
    }

    public void init() throws Exception {
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        if (defaultDeviceName == null) throw new RuntimeException("Could not get default audio device");
        device = ALC10.alcOpenDevice(defaultDeviceName);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        context = ALC10.alcCreateContext(device, (IntBuffer) null);
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(alcCapabilities);

        for (int index = 0; index < sources.length; index++) sources[index] = new AudioSource();
        loadDigSounds();
        loadStepSounds();
        loadMiscellaneousSounds();
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadDigSounds() {
        digGlass[0] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_1.ogg");
        digGlass[1] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_2.ogg");
        digGlass[2] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_3.ogg");
        digGlass[3] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_4.ogg");
        digGlass[4] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_5.ogg");
        digGlass[5] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_6.ogg");
        digGlass[6] = ObjectLoader.loadSound("Sounds/dig/355340__samgd14__glass-breaking_7.ogg");

        digGrass[0] = ObjectLoader.loadSound("Sounds/dig/grass1.ogg");
        digGrass[1] = ObjectLoader.loadSound("Sounds/dig/grass2.ogg");
        digGrass[2] = ObjectLoader.loadSound("Sounds/dig/grass3.ogg");
        digGrass[3] = ObjectLoader.loadSound("Sounds/dig/grass4.ogg");

        digGravel[0] = ObjectLoader.loadSound("Sounds/dig/gravel1.ogg");
        digGravel[1] = ObjectLoader.loadSound("Sounds/dig/gravel2.ogg");
        digGravel[2] = ObjectLoader.loadSound("Sounds/dig/gravel3.ogg");
        digGravel[3] = ObjectLoader.loadSound("Sounds/dig/gravel4.ogg");

        digSand[0] = ObjectLoader.loadSound("Sounds/dig/sand1.ogg");
        digSand[1] = ObjectLoader.loadSound("Sounds/dig/sand2.ogg");
        digSand[2] = ObjectLoader.loadSound("Sounds/dig/sand3.ogg");
        digSand[3] = ObjectLoader.loadSound("Sounds/dig/sand4.ogg");

        digSnow[0] = ObjectLoader.loadSound("Sounds/dig/snow1.ogg");
        digSnow[1] = ObjectLoader.loadSound("Sounds/dig/snow2.ogg");
        digSnow[2] = ObjectLoader.loadSound("Sounds/dig/snow3.ogg");
        digSnow[3] = ObjectLoader.loadSound("Sounds/dig/snow4.ogg");

        digStone[0] = ObjectLoader.loadSound("Sounds/dig/stone1.ogg");
        digStone[1] = ObjectLoader.loadSound("Sounds/dig/stone2.ogg");
        digStone[2] = ObjectLoader.loadSound("Sounds/dig/stone3.ogg");
        digStone[3] = ObjectLoader.loadSound("Sounds/dig/stone4.ogg");
        digStone[4] = ObjectLoader.loadSound("Sounds/dig/stone5.ogg");
        digStone[5] = ObjectLoader.loadSound("Sounds/dig/stone6.ogg");
        digStone[6] = ObjectLoader.loadSound("Sounds/dig/stone7.ogg");

        digFoliage[0] = ObjectLoader.loadSound("Sounds/dig/vines1.ogg");
        digFoliage[1] = ObjectLoader.loadSound("Sounds/dig/vines2.ogg");
        digFoliage[2] = ObjectLoader.loadSound("Sounds/dig/vines3.ogg");
        digFoliage[3] = ObjectLoader.loadSound("Sounds/dig/vines4.ogg");

        digWood[0] = ObjectLoader.loadSound("Sounds/dig/wood1.ogg");
        digWood[1] = ObjectLoader.loadSound("Sounds/dig/wood2.ogg");
        digWood[2] = ObjectLoader.loadSound("Sounds/dig/wood3.ogg");
        digWood[3] = ObjectLoader.loadSound("Sounds/dig/wood4.ogg");
        digWood[4] = ObjectLoader.loadSound("Sounds/dig/wood5.ogg");
        digWood[5] = ObjectLoader.loadSound("Sounds/dig/wood6.ogg");

        digIce[0] = ObjectLoader.loadSound("Sounds/dig/420882__inspectorj__impact-ice-moderate-a.ogg");
        digIce[1] = ObjectLoader.loadSound("Sounds/dig/420880__inspectorj__impact-ice-moderate-c.ogg");

        digCloth[0] = ObjectLoader.loadSound("Sounds/dig/cloth1.ogg");
        digCloth[1] = ObjectLoader.loadSound("Sounds/dig/cloth2.ogg");
        digCloth[2] = ObjectLoader.loadSound("Sounds/dig/cloth3.ogg");
        digCloth[3] = ObjectLoader.loadSound("Sounds/dig/cloth4.ogg");
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadStepSounds() {
        stepGlass[0] = ObjectLoader.loadSound("Sounds/step/glass1.ogg");
        stepGlass[1] = ObjectLoader.loadSound("Sounds/step/glass2.ogg");
        stepGlass[2] = ObjectLoader.loadSound("Sounds/step/glass3.ogg");
        stepGlass[3] = ObjectLoader.loadSound("Sounds/step/glass4.ogg");

        stepGrass[0] = ObjectLoader.loadSound("Sounds/step/grass1.ogg");
        stepGrass[1] = ObjectLoader.loadSound("Sounds/step/grass2.ogg");
        stepGrass[2] = ObjectLoader.loadSound("Sounds/step/grass3.ogg");
        stepGrass[3] = ObjectLoader.loadSound("Sounds/step/grass4.ogg");
        stepGrass[4] = ObjectLoader.loadSound("Sounds/step/grass5.ogg");
        stepGrass[5] = ObjectLoader.loadSound("Sounds/step/grass6.ogg");

        stepGravel[0] = ObjectLoader.loadSound("Sounds/step/gravel1.ogg");
        stepGravel[1] = ObjectLoader.loadSound("Sounds/step/gravel2.ogg");
        stepGravel[2] = ObjectLoader.loadSound("Sounds/step/gravel3.ogg");
        stepGravel[3] = ObjectLoader.loadSound("Sounds/step/gravel4.ogg");

        stepSand[0] = ObjectLoader.loadSound("Sounds/step/sand1.ogg");
        stepSand[1] = ObjectLoader.loadSound("Sounds/step/sand2.ogg");
        stepSand[2] = ObjectLoader.loadSound("Sounds/step/sand3.ogg");
        stepSand[3] = ObjectLoader.loadSound("Sounds/step/sand4.ogg");
        stepSand[4] = ObjectLoader.loadSound("Sounds/step/sand5.ogg");

        stepSnow[0] = ObjectLoader.loadSound("Sounds/step/snow1.ogg");
        stepSnow[1] = ObjectLoader.loadSound("Sounds/step/snow2.ogg");
        stepSnow[2] = ObjectLoader.loadSound("Sounds/step/snow3.ogg");
        stepSnow[3] = ObjectLoader.loadSound("Sounds/step/snow4.ogg");

        stepStone[0] = ObjectLoader.loadSound("Sounds/step/stone1.ogg");
        stepStone[1] = ObjectLoader.loadSound("Sounds/step/stone2.ogg");
        stepStone[2] = ObjectLoader.loadSound("Sounds/step/stone3.ogg");
        stepStone[3] = ObjectLoader.loadSound("Sounds/step/stone4.ogg");
        stepStone[4] = ObjectLoader.loadSound("Sounds/step/stone5.ogg");
        stepStone[5] = ObjectLoader.loadSound("Sounds/step/stone6.ogg");

        stepFoliage[0] = ObjectLoader.loadSound("Sounds/block/vine/climb1.ogg");
        stepFoliage[1] = ObjectLoader.loadSound("Sounds/block/vine/climb2.ogg");
        stepFoliage[2] = ObjectLoader.loadSound("Sounds/block/vine/climb3.ogg");
        stepFoliage[3] = ObjectLoader.loadSound("Sounds/block/vine/climb4.ogg");
        stepFoliage[4] = ObjectLoader.loadSound("Sounds/block/vine/climb5.ogg");

        stepWood[0] = ObjectLoader.loadSound("Sounds/step/wood1.ogg");
        stepWood[1] = ObjectLoader.loadSound("Sounds/step/wood2.ogg");
        stepWood[2] = ObjectLoader.loadSound("Sounds/step/wood3.ogg");
        stepWood[3] = ObjectLoader.loadSound("Sounds/step/wood4.ogg");
        stepWood[4] = ObjectLoader.loadSound("Sounds/step/wood5.ogg");
        stepWood[5] = ObjectLoader.loadSound("Sounds/step/wood6.ogg");

        swim[0] = ObjectLoader.loadSound("Sounds/step/swim1.ogg");
        swim[1] = ObjectLoader.loadSound("Sounds/step/swim2.ogg");
        swim[2] = ObjectLoader.loadSound("Sounds/step/swim3.ogg");
        swim[3] = ObjectLoader.loadSound("Sounds/step/swim4.ogg");
        swim[4] = ObjectLoader.loadSound("Sounds/step/swim5.ogg");

        stepDirt[0] = ObjectLoader.loadSound("Sounds/step/682127__henkonen__footsteps-dirt-road-1_1.ogg");
        stepDirt[1] = ObjectLoader.loadSound("Sounds/step/682127__henkonen__footsteps-dirt-road-1_2.ogg");
        stepDirt[2] = ObjectLoader.loadSound("Sounds/step/682127__henkonen__footsteps-dirt-road-1_3.ogg");
        stepDirt[3] = ObjectLoader.loadSound("Sounds/step/682127__henkonen__footsteps-dirt-road-1_4.ogg");

        stepCloth[0] = ObjectLoader.loadSound("Sounds/step/cloth1.ogg");
        stepCloth[1] = ObjectLoader.loadSound("Sounds/step/cloth2.ogg");
        stepCloth[2] = ObjectLoader.loadSound("Sounds/step/cloth3.ogg");
        stepCloth[3] = ObjectLoader.loadSound("Sounds/step/cloth4.ogg");
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void loadMiscellaneousSounds() {
        explode[0] = ObjectLoader.loadSound("Sounds/random/750677__lukacafuka__firecracker-explosion-1.ogg");
        explode[1] = ObjectLoader.loadSound("Sounds/random/750678__lukacafuka__firecracker-explosion-2.ogg");
        explode[2] = ObjectLoader.loadSound("Sounds/random/750680__lukacafuka__firecracker-explosion-4.ogg");

        fuse = ObjectLoader.loadSound("Sounds/random/fuse.ogg");

        lavaPop[0] = ObjectLoader.loadSound("Sounds/liquid/lavapop.ogg");

        splash[0] = ObjectLoader.loadSound("Sounds/random/splash.ogg");
        submerge[0] = ObjectLoader.loadSound("Sounds/random/splash1.ogg");
        submerge[1] = ObjectLoader.loadSound("Sounds/random/splash2.ogg");
        submerge[2] = ObjectLoader.loadSound("Sounds/random/splash3.ogg");
        submerge[3] = ObjectLoader.loadSound("Sounds/random/splash4.ogg");


    }

    public void setListenerData(Player player) {
        Vector3f position = player.getCamera().getPosition();
        Vector3f velocity = player.getVelocity();
        Vector3f direction = player.getCamera().getDirection();
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
        AL10.alListenerfv(AL10.AL_ORIENTATION, new float[]{direction.x, direction.y, direction.z, 0.0f, 1.0f, 0.0f});

        listenerPosition.set(position.x, position.y, position.z);
    }

    public void playSound(int buffer, float x, float y, float z, float vx, float vy, float vz, float gain) {
        AudioSource source = getNextFreeAudioSource();
        if (source == null) return;
        float disX = (x - listenerPosition.x);
        float disY = (y - listenerPosition.y);
        float disZ = (z - listenerPosition.z);
        if (disX * disX + disY * disY + disZ * disZ > MAX_SOUND_DISTANCE) return;

        source.setPosition(x, y, z);
        source.setVelocity(vx, vy, vz);
        source.setGain(AUDIO_GAIN * gain);
        source.play(buffer);
    }

    public void playRandomSound(int[] sounds, float x, float y, float z, float vx, float vy, float vz, float gain) {
        if (sounds == null) return;
        AudioSource source = getNextFreeAudioSource();
        if (source == null) return;
        float disX = (x - listenerPosition.x);
        float disY = (y - listenerPosition.y);
        float disZ = (z - listenerPosition.z);
        if (disX * disX + disY * disY + disZ * disZ > MAX_SOUND_DISTANCE) return;

        source.setPosition(x, y, z);
        source.setVelocity(vx, vy, vz);
        source.setGain(AUDIO_GAIN * gain);
        source.play(sounds[(int) (Math.random() * sounds.length)]);
    }

    private AudioSource getNextFreeAudioSource() {
        for (AudioSource source : sources) if (!source.isPlaying()) return source;
        return null;
    }

    public AudioSource[] getSources() {
        return sources;
    }

    public void cleanUp() {
        for (AudioSource source : sources) source.delete();
        ALC10.alcMakeContextCurrent(MemoryUtil.NULL);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}
