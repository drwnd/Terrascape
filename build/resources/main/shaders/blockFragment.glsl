#version 400 core

in vec2 textureCoordinates;
in float fragLight;
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
    float waterFogMultiplier = min(1, headUnderWater * max(0.5, distance * 0.01));
    fragColor = vec4(color.rgb * fragLight * (1 - waterFogMultiplier) + vec3(0.0, 0.098, 0.643) * waterFogMultiplier * (abs(time) * 0.75 + 0.25), color.a);
}
