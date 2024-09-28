#version 400 core

in vec2 fragTextureCoordinates;
in float blockLight;
in float skyLight;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform float time;


float easeInOutQuart(float x) {
    //x < 0.5 ? 8 * x * x * x * x : 1 - pow(-2 * x + 2, 4) / 2;
    float inValue = 8.0 * x * x * x * x;
    float outValue = 1.0 - pow(-2.0 * x + 2.0, 4.0) / 2.0;
    return step(inValue, 0.5) * inValue + step(0.5, outValue) * outValue;
}

void main() {
    vec4 color = texture(textureSampler, fragTextureCoordinates);

    if (color.a == 0.0) {
        discard;
    }

    float fragLight = max(blockLight + 0.3, max(0.3, skyLight) * easeInOutQuart(abs(time)));

    fragColor = vec4(color.rgb * fragLight, color.a);
}