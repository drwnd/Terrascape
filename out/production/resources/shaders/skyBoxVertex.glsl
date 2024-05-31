#version 400 core

in vec3 position;
in vec2 textureCoordinates;

out vec2 fragTextureCoordinates;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 transformationMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position * 100000.0, 1.0);

    fragTextureCoordinates = textureCoordinates;
}
