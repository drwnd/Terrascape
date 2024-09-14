#version 400 core

in ivec2 data;

out vec2 fragTextureCoordinates;
out float fragLight;

uniform vec3 position;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform int lightLevel;
uniform float time;

void main() {

    float x = ((data.y >> 20 & 1023) - 511) * 0.0625;
    float y = ((data.y >> 10 & 1023) - 511) * 0.0625;
    float z = ((data.y & 1023) - 511) * 0.0625;
    float u = (((data.x >> 9) & 511) - 15) * 0.00390625;
    float v = ((data.x & 511) - 15) * 0.00390625;

    gl_Position = projectionMatrix * viewMatrix * vec4(position + vec3(x, y, z), 1.0);
    fragTextureCoordinates = vec2(u, v);

    float absTime = abs(time);
    float blockLight = (lightLevel & 15) * 0.0625;
    float skyLight = (lightLevel >> 4 & 15) * 0.0625;
    fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25));
}