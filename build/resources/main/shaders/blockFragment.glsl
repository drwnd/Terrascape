#version 400 core

in vec2 textureCoordinates;
in vec3 normal;
in float blockLight;
in float skyLight;
in float ambientOcclusionLevel;
in float distance;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform int headUnderWater;
uniform float time;

void main(){
    vec4 color = texture(textureSampler, textureCoordinates);
    if (color.a == 0.0f){
        discard;
    }

    float alpha = time * 3.1415926536;
    float sinAlpha = sin(alpha);
    float cosAlpha = cos(alpha);
    vec3 sunDirection = vec3(cosAlpha - sinAlpha, -0.3, cosAlpha + sinAlpha);
    float absTime = abs(time);
    float sunIllumination = dot(normal, sunDirection) * 0.2 * skyLight * absTime;

    float fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25) + sunIllumination) * ambientOcclusionLevel;

    float waterFogMultiplier = min(1, headUnderWater * max(0.5, distance * 0.01));
    fragColor = vec4(color.rgb * fragLight * (1 - waterFogMultiplier) + vec3(0.0, 0.098, 0.643) * waterFogMultiplier * (abs(time) * 0.75 + 0.25), color.a);
}
