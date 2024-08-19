#version 400 core

in vec2 textureCoordinates;
in float fragLight;
in vec3 totalPosition;
in vec3 normal;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform int headUnderWater;
uniform float time;
uniform vec3 cameraPosition;

void main(){
    vec4 color = texture(textureSampler, textureCoordinates);

    float distance = length(cameraPosition - totalPosition);
    float angle = abs(dot((totalPosition - cameraPosition) / distance, normal));

    vec3 waterColor = color.rgb + angle * vec3(0.0, 0.4, 0.15);

    float waterFogMultiplier = min(1, headUnderWater * max(0.5, distance * 0.01));
    fragColor = vec4(waterColor * fragLight * (1 - waterFogMultiplier) + vec3(0.0, 0.098, 0.643) * waterFogMultiplier * (abs(time) * 0.75 + 0.25), color.a - angle * 0.3);
}
