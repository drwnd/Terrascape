#version 400 core

in vec2 fragTextureCoordinates;
in float blockLight;
in float skyLight;
in vec3 normal;
in float distance;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform int headUnderWater;
uniform float time;

vec3 getSunDirection() {
    float alpha = time * 3.1415926536;
    float sinAlpha = sin(alpha);
    float cosAlpha = cos(alpha);
    return vec3(cosAlpha - sinAlpha, -0.3, cosAlpha + sinAlpha);
}

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
    float absTime = abs(time);
    vec3 sunDirection = getSunDirection();
    float sunIllumination = dot(normal, sunDirection) * 0.2 * skyLight * absTime;

    float timeLight = max(0.2, easeInOutQuart(absTime));
    float nightLight = 0.6 * (1 - absTime) * (1 - absTime);
    float light = max(blockLight + 0.2, max(0.2, skyLight) * timeLight + sunIllumination);
    vec3 fragLight = vec3(light, light, max(blockLight + 0.2, max(0.2, skyLight + nightLight) * timeLight + sunIllumination));
    float waterFogMultiplier = min(1, headUnderWater * max(0.5, distance * 0.01));

    fragColor = vec4(color.rgb * fragLight * (1 - waterFogMultiplier) + vec3(0.0, 0.098, 0.643) * waterFogMultiplier * timeLight, color.a);
}