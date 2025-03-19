#version 460 core

in int data;

out vec2 fragTextureCoordinates;
out float blockLight;
out float skyLight;
out vec3 normal;
out float distance;

layout (std430, binding = 0) restrict readonly buffer positionBuffer {
    vec4[] position;
};
layout (std430, binding = 1) restrict readonly buffer rotationBuffer {
    vec4[] rotation;
};
//layout (std430, binding = 2) readonly buffer translationsBuffer {
//    vec4[] translations;
//};
layout (std430, binding = 2) restrict readonly buffer intsBuffers {
    ivec4[] ints;
};

uniform mat4 projectionViewMatrix;
uniform vec3 cameraPosition;

const vec3[6] normals = vec3[6](vec3(0, 0, 1), vec3(0, 1, 0), vec3(1, 0, 0), vec3(0, 0, -1), vec3(0, -1, 0), vec3(-1, 0, 0));

int getTextureIndex(int side) {
    int uvNorthTop = ints[gl_InstanceID][1];
    int uvWestSouth = ints[gl_InstanceID][2];
    int uvBottomEast = ints[gl_InstanceID][3];

    return
    int(side == 0) * ((uvNorthTop >> 16) & 0xFFFF) +
    int(side == 1) * (uvNorthTop & 0xFFFF) +
    int(side == 2) * ((uvWestSouth >> 16) & 0xFFFF) +
    int(side == 3) * (uvWestSouth & 0xFFFF) +
    int(side == 4) * ((uvBottomEast >> 16) & 0xFFFF) +
    int(side == 5) * (uvBottomEast & 0xFFFF);
}

int getDeltaU(int side, int widthHeightDepthLight) {

    int width = (widthHeightDepthLight >> 24) & 255;
    int height = (widthHeightDepthLight >> 16) & 255;
    int depth = (widthHeightDepthLight >> 8) & 255;

    return int(side == 0) * width
    + int(side == 1) * depth
    + int(side == 2) * depth
    + int(side == 3) * width
    + int(side == 4) * width
    + int(side == 5) * depth;
}

int getDeltaV(int side, int widthHeightDepthLight) {

    int width = (widthHeightDepthLight >> 24) & 255;
    int height = (widthHeightDepthLight >> 16) & 255;
    int depth = (widthHeightDepthLight >> 8) & 255;

    return int(side == 0) * height
    + int(side == 1) * width
    + int(side == 2) * height
    + int(side == 3) * height
    + int(side == 4) * depth
    + int(side == 5) * height;
}

vec3 rotate(vec3 vertexPosition) {
    float rotateX = rotation[gl_InstanceID].x;
    float rotateY = rotation[gl_InstanceID].y;

    float cosValue = cos(rotateX);
    float sinValue = sin(rotateX);
    vertexPosition = mat3(1.0, 0.0, 0.0, 0.0, cosValue, -sinValue, 0.0, sinValue, cosValue) * vertexPosition;

    cosValue = cos(rotateY);
    sinValue = sin(rotateY);
    vertexPosition = mat3(cosValue, 0.0, sinValue, 0.0, 1.0, 0.0, -sinValue, 0.0, cosValue) * vertexPosition;

    return vertexPosition;
}

void main() {

    int widthHeightDepthLight = ints[gl_InstanceID][0];
    vec3 translationToEntityOrigin = vec3(position[gl_InstanceID].w, rotation[gl_InstanceID].zw);

    float x = 0.0625 * (data >> 7 & 1) * (widthHeightDepthLight >> 24 & 0xFF) + translationToEntityOrigin.x;
    float y = 0.0625 * (data >> 6 & 1) * (widthHeightDepthLight >> 16 & 0xFF) + translationToEntityOrigin.y;
    float z = 0.0625 * (data >> 5 & 1) * (widthHeightDepthLight >> 8 & 0xFF) + translationToEntityOrigin.z;

    vec3 vertexPosition = vec3(x, y, z);
    vertexPosition = position[gl_InstanceID].xyz + rotate(vertexPosition);

    gl_Position = projectionViewMatrix * vec4(vertexPosition, 1.0);

    int side = data & 7;
    int textureIndex = getTextureIndex(side);
    float u = ((textureIndex >> 8 & 255) + (data >> 3 & 1) * getDeltaU(side, widthHeightDepthLight)) * 0.00390625;
    float v = ((textureIndex & 255) + (data >> 4 & 1) * getDeltaV(side, widthHeightDepthLight)) * 0.00390625;
    fragTextureCoordinates = vec2(u, v);

    blockLight = (widthHeightDepthLight & 15) * 0.0625;
    skyLight = (widthHeightDepthLight >> 4 & 15) * 0.0625;
    normal = rotate(normals[side]);
    distance = length(vertexPosition - cameraPosition);
}