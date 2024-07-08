#version 400 core

in vec2 textureCoordinates;
in float fragLight;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main(){
    vec4 color = texture(textureSampler, textureCoordinates);
    if (color.a == 0.0f){
        discard;
    }
    fragColor = vec4(color.rgb * fragLight, color.a);
}
