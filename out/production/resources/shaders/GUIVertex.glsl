#version 400 core

layout(location = 0) in vec2 position;
layout(location = 1) in vec2 textureCoordinate;

out vec2 fragTextureCoordinate;

void main() {
    gl_Position = vec4(position, 0.5, 0.5);
    fragTextureCoordinate = textureCoordinate;
}
