#version 400 core

in vec2 textureCoordinates;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    fragColor = texture(textureSampler, textureCoordinates);
    if (fragColor.a == 0.0) {
        discard;
    }
}