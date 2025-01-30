#version 400 core

in int index;

out vec2 textureCoordinates;

uniform ivec2 screenSize;
uniform ivec2 charSize;
uniform int string[128];
uniform int yOffset;
uniform int xOffset;

void main() {
    int deltaX = index >> 7 & 1;
    int deltaY = index >> 8 & 1;

    float x = -1.0 + float(((index & 127) + deltaX) * charSize.x + xOffset) / screenSize.x;
    float y = 1.0 - float(deltaY * charSize.y + yOffset) / screenSize.y;
    gl_Position = vec4(x, y, 0.5, 1);

    int character = string[index & 127];
    float u = ((character & 15) + deltaX) * 0.0625;
    float v = ((character >> 4 & 15) + deltaY) * 0.0625;
    textureCoordinates = vec2(u, v);
}