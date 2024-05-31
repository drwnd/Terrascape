#version 400 core

in vec2 textureCoordinates;
in float fragLight;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main(){
    vec4 color = texture2D(textureSampler, textureCoordinates);
    if(color.a == 0.0f){
        discard;
    }
//    fragColor = vec4(color.rgb * fragLight * (time * 0.75 + 0.25), color.a);
    fragColor = vec4(color.rgb * fragLight, color.a);
}
