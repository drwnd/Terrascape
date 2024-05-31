#version 400 core

layout(location = 0) in int data;

out vec2 textureCoordinates;
out float fragLight;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec3 worldPos;

const float[6] light = float[6](1.0f, 1.2f, 0.9f, 0.8f, 0.6f, 1.1f);

void main(){

    int x = (data >> 12) & 63;
    int y = (data >> 6) & 63;
    int z = data & 63;

    //Maybe problem with inplicit type cast
    gl_Position = projectionMatrix * viewMatrix *  vec4(vec3(x, y, z) + worldPos, 1.0);

    textureCoordinates = vec2(float((data >> 23) & 31) * 0.0625, float((data >> 18) & 31) * 0.0625);

    fragLight = light[(data >> 28) & 7];
}