#version 400 core

in vec2 fragTextureCoordinates;
in float fragLight;

out vec4 fragColor;

uniform sampler2D textureSampler;


void main() {
    vec4 color = texture(textureSampler, fragTextureCoordinates);

    if (color.a == 0.0) {
        discard;
    }
    fragColor = vec4(color.rgb * fragLight, color.a);
}