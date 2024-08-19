#version 400 core

in vec3 position;
in vec2 textureCoordinates;

out vec2 fragTextureCoordinates;
flat out float fragTime;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 transformationMatrix;
uniform float time;

void main() {
    float alpha = time * 3.1415926536 - 0.7;
    vec3 rotatedPosition = vec3(position.x * cos(alpha) - position.z * sin(alpha), position.y, position.z * cos(alpha) + position.x * sin(alpha));
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(rotatedPosition, 1.0);

    fragTextureCoordinates = textureCoordinates;
    fragTime = abs(time);
}
