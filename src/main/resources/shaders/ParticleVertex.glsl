#version 400 core

layout (location = 0) in vec3 offset;
layout (location = 1) in vec3 velocity;
layout (location = 2) in vec2 textureCoordinates;

out vec2 fragTextureCoordinates;

uniform vec3 position;
uniform vec3 cameraPosition;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 particleProperties;

void main() {

    float maxAliveTime = particleProperties.x;
    float particleSize = particleProperties.y;
    float aliveTime = particleProperties.z;
    float gravity = particleProperties.w;

    vec3 currentPosition = position + offset + velocity * aliveTime;
    currentPosition.y += aliveTime * aliveTime * gravity;

    vec3 cross1 = cross(cameraPosition - currentPosition, vec3(0.0, 1.0, 0.0));
    vec3 cross2 = cross(cameraPosition - currentPosition, cross1);

    float timeSacalar = (maxAliveTime - aliveTime) / maxAliveTime;

    vec3 offsetLeftRight = normalize(cross1) * (gl_VertexID & 1) * particleSize * timeSacalar;
    vec3 offsetUpDown = normalize(cross2) * (gl_VertexID >> 1 & 1) * particleSize * timeSacalar;

    gl_Position = projectionMatrix * viewMatrix * vec4(currentPosition + offsetLeftRight + offsetUpDown, 1.0);

    fragTextureCoordinates = textureCoordinates + vec2((gl_VertexID & 1) * particleSize, (gl_VertexID >> 1 & 1) * particleSize);
}