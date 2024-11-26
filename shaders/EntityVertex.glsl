#version 400 core

in ivec2 data;

out vec2 fragTextureCoordinates;
out float blockLight;
out float skyLight;

uniform vec3 position;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform int lightLevel;

void main() {

    float x = ((data.y >> 20 & 1023) - 511) * 0.0625;
    float y = ((data.y >> 10 & 1023) - 511) * 0.0625;
    float z = ((data.y & 1023) - 511) * 0.0625;
    float u = (((data.x >> 9) & 511) - 15) * 0.00390625;
    float v = ((data.x & 511) - 15) * 0.00390625;

    gl_Position = projectionMatrix * viewMatrix * vec4(position + vec3(x, y, z), 1.0);
    fragTextureCoordinates = vec2(u, v);

    blockLight = (lightLevel & 15) * 0.0625;
    skyLight = (lightLevel >> 4 & 15) * 0.0625;
}