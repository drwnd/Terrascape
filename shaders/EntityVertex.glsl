#version 460 core

in ivec2 data;

out vec2 fragTextureCoordinates;
out float blockLight;
out float skyLight;

layout (binding = 0, std430) readonly buffer instanceDataBuffer {
    vec4[] instanceData;
};

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

int getTextureIndex(int side, int textureCoordinatesAndLightLevel) {
    int topWeight = int(side == 1);
    int bottomWeight = int(side == 4);
    int sideWeight = 1 - topWeight - bottomWeight;

    return
    (textureCoordinatesAndLightLevel >> 24 & 255) * topWeight +
    (textureCoordinatesAndLightLevel >> 16 & 255) * sideWeight +
    (textureCoordinatesAndLightLevel >> 8 & 255) * bottomWeight;
}

void main() {

    float x = ((data.y >> 20 & 1023) - 511) * 0.0625;
    float y = ((data.y >> 10 & 1023) - 511) * 0.0625;
    float z = ((data.y & 1023) - 511) * 0.0625;

    gl_Position = projectionMatrix * viewMatrix * vec4(instanceData[gl_InstanceID].xyz + vec3(x, y, z), 1.0);

    int textureCoordinatesAndLightLevel = floatBitsToInt(instanceData[gl_InstanceID].w);

    int textureIndex = getTextureIndex(data.x & 7, textureCoordinatesAndLightLevel);
    float u = ((textureIndex & 15) + (data.x >> 3 & 1)) * 0.0625;
    float v = ((textureIndex >> 4 & 15) + (data.x >> 4 & 1)) * 0.0625;
    fragTextureCoordinates = vec2(u, v);

    blockLight = (textureCoordinatesAndLightLevel & 15) * 0.0625;
    skyLight = (textureCoordinatesAndLightLevel >> 4 & 15) * 0.0625;
}