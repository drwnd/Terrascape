#version 400 core

layout (location = 0) in ivec2 data;

out vec2 textureCoordinates;
out vec3 normal;
out float blockLight;
out float skyLight;
out float ambientOcclusionLevel;
out float distance;

uniform mat4 projectionViewMatrix;
uniform ivec3 worldPos;
uniform vec3 cameraPosition;

const vec3[6] normals = vec3[6](vec3(0, 0, 1), vec3(0, 1, 0), vec3(1, 0, 0), vec3(0, 0, -1), vec3(0, -1, 0), vec3(-1, 0, 0));

void main() {

    float x = ((data.x >> 20 & 1023) - 15) * 0.0625;
    float y = ((data.x >> 10 & 1023) - 15) * 0.0625;
    float z = ((data.x & 1023) - 15) * 0.0625;

    //Maybe problem with inplicit type cast
    gl_Position = projectionViewMatrix * vec4(vec3(x, y, z) + worldPos, 1.0);

    float u = (((data.y >> 9) & 511) - 15) * 0.00390625;
    float v = ((data.y & 511) - 15) * 0.00390625;

    textureCoordinates = vec2(u, v);

    blockLight = (data.y >> 18 & 15) * 0.0625;
    skyLight = (data.y >> 22 & 15) * 0.0625;
    ambientOcclusionLevel = 1 - (data.x >> 30 & 3) * 0.22;
    normal = normals[data.y >> 26 & 7];

    distance = length(vec3(x, y, z) + worldPos - cameraPosition);
}