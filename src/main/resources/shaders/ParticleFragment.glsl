#version 400 core

in vec2 fragTextureCoordinates;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec2 textureOffset_;
uniform int lightLevel;
uniform float time;

float easeInOutQuart(float x) {
    //x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2;
    float inValue = 8.0 * x * x * x * x;
    float outValue = 1.0 - pow(-2.0 * x + 2.0, 4.0) / 2.0;
    return step(inValue, 0.5) * inValue + step(0.5, outValue) * outValue;
}

void main() {
    vec4 color = texture(textureSampler, (textureOffset_ + fragTextureCoordinates) * 0.0625);
    if (color.a == 0.0) {
        discard;
    }

    float absTime = abs(time);
    float blockLight = (lightLevel & 15) * 0.0625;
    float skyLight = (lightLevel >> 4 & 15) * 0.0625;
    float fragLight = max(blockLight + 0.3, max(0.3, skyLight) * easeInOutQuart(absTime));

    fragColor = vec4(color.rgb * fragLight, color.a);
}