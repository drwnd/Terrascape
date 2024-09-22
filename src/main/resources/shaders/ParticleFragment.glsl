#version 400 core

in vec2 fragTextureCoordinates;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec2 textureOffset_;
uniform int lightLevel;
uniform float time;

void main() {
    vec4 color = texture(textureSampler, (textureOffset_ + fragTextureCoordinates) * 0.0625);
    if (color.a == 0.0) {
        discard;
    }

    float absTime = abs(time);
    float blockLight = (lightLevel & 15) * 0.0625;
    float skyLight = (lightLevel >> 4 & 15) * 0.0625;
    float fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25));

    fragColor = vec4(color.rgb * fragLight, color.a);
}