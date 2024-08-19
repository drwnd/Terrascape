#version 400 core

layout(location = 0) in ivec2 data;

out vec2 textureCoordinates;
out float fragLight;
out float distance;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec3 worldPos;
uniform float time;
uniform vec3 cameraPosition;

const vec3[6] normals = vec3[6](vec3(0, 0, 1), vec3(0, 1, 0), vec3(1, 0, 0), vec3(0, 0, -1), vec3(0, -1, 0), vec3(-1, 0, 0));

void main(){

    float x = ((data.x >> 20 & 1023) - 15) * 0.0625;
    float y = ((data.x >> 10 & 1023) - 15) * 0.0625;
    float z = ((data.x & 1023) - 15) * 0.0625;

    //Maybe problem with inplicit type cast
    gl_Position = projectionMatrix * viewMatrix * vec4(vec3(x, y, z) + worldPos, 1.0);

    float u = (((data.y >> 9) & 511) - 15) * 0.00390625;
    float v = ((data.y & 511) - 15) * 0.00390625;

    textureCoordinates = vec2(u, v);

    float blockLight = (data.y >> 18 & 15) * 0.0425;
    float skyLight = (data.y >> 22 & 15) * 0.0625;
    float ambientOcclusionLevel = 1 - (data.x >> 30 & 3) * 0.22;
    int side = data.y >> 26 & 7;

    float alpha = time * 3.1415926536;
    float sinAlpha = sin(alpha);
    float cosAlpha = cos(alpha);
    vec3 sunDirection = vec3(cosAlpha - sinAlpha, -0.3, cosAlpha + sinAlpha);
    float absTime = abs(time);
    float sunIllumination = dot(normals[side], sunDirection) * 0.2 * skyLight * absTime;

    fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25) + sunIllumination) * ambientOcclusionLevel;
    distance = length(vec3(x, y, z) + worldPos - cameraPosition);
}