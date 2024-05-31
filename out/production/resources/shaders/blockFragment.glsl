#version 400 core

in vec2 textureCoordinates;
in float fragLight;

out vec4 fragColor;

uniform sampler2D textureSampler;
uniform float time;

void main(){
    vec4 color = texture2D(textureSampler, textureCoordinates);
    if(color.a == 0.0f){
        discard;
    }
    vec3 RGBColor = vec3(color.r, color.g, color.b) * fragLight * (time * 0.75 + 0.25);
    fragColor = vec4(RGBColor, color.a);
}
