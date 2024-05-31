#version 400 core

uniform sampler2D textureSampler;

in vec2 fragTextureCoordinate;

out vec4 fragColor;

void main() {
    vec4 color = texture(textureSampler, fragTextureCoordinate);
    if(color.a == 0.0){
        discard;
    }
    fragColor = color;
}
