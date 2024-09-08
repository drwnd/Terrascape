#version 400 core

in int data;

out vec2 fragTextureCoordinates;
out float fragLight;

uniform vec3 position;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform int lightLevel;
uniform float time;

void main() {

    float u = (data >> 23 & 511) * 0.0625;
    float v = (data >> 14 & 511) * 0.0625;
    float x = ((data >> 8 & 15) - 7) * 0.125;
    float y = ((data >> 4 & 15) - 7) * 0.125;
    float z = ((data & 15) - 7) * 0.125;

    gl_Position = projectionMatrix * viewMatrix * vec4(position + vec3(x, y, z), 1.0);
    fragTextureCoordinates = vec2(u, v);

    float absTime = abs(time);
    float blockLight = (lightLevel & 15) * 0.0625;
    float skyLight = (lightLevel >> 4 & 15) * 0.0625;
    fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25));
}