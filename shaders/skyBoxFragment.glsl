#version 400 core

in vec2 fragTextureCoordinates;

out vec4 fragColor;

uniform sampler2D textureSampler1;
uniform sampler2D textureSampler2;
uniform float time;

void main() {
    vec4 color1 = texture(textureSampler1, fragTextureCoordinates);
    vec4 color2 = texture(textureSampler2, fragTextureCoordinates);

    float absTime = abs(time);
    fragColor = color1 * absTime + color2 * (1 - absTime);
}
