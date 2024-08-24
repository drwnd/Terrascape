#version 400 core

layout (location = 0) in ivec2 data;

out vec2 textureCoordinates;
out float fragLight;
out vec3 totalPosition;
out vec3 normal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform ivec3 worldPos;
uniform float time;
uniform vec3 cameraPosition;
uniform int shouldSimulateWaves;

const vec3[6] normals = vec3[6](vec3(0, 0, 1), vec3(0, 1, 0), vec3(1, 0, 0), vec3(0, 0, -1), vec3(0, -1, 0), vec3(-1, 0, 0));

//	Simplex 3D Noise
//	by Ian McEwan, Stefan Gustavson (https://github.com/stegu/webgl-noise)
//
vec4 permute(vec4 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
    vec3 i = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    // Other corners
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    //  x0 = x0 - 0. + 0.0 * C
    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    // Permutations
    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(
                                 i.z + vec4(0.0, i1.z, i2.z, 1.0))
                             + i.y + vec4(0.0, i1.y, i2.y, 1.0))
                     + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    // Gradients
    // ( N*N points uniformly over a square, mapped onto an octahedron.)
    float n_ = 1.0 / 7.0;// N=7
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);//  mod(p,N*N)

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);// mod(j,N)

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    //Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

void main() {

    float x = ((data.x >> 20 & 1023) - 15) * 0.0625;
    float y = ((data.x >> 10 & 1023) - 15) * 0.0625;
    float z = ((data.x & 1023) - 15) * 0.0625;
    int waveMultiplier = data.y >> 29 & 1;

    totalPosition = vec3(x, y, z) + worldPos;

    if (shouldSimulateWaves == 1) {
        totalPosition.y += (snoise(vec3(totalPosition.xz * 0.333, abs(time) * 350)) * 0.09) * waveMultiplier;
    }

    gl_Position = projectionMatrix * viewMatrix * vec4(totalPosition, 1.0);

    float u = (((data.y >> 9) & 511) - 15) * 0.00390625;
    float v = ((data.y & 511) - 15) * 0.00390625;

    textureCoordinates = vec2(u, v);

    float blockLight = (data.y >> 18 & 15) * 0.0425;
    float skyLight = (data.y >> 22 & 15) * 0.0625;
    float ambientOcclusionLevel = 1 - (data.x >> 30 & 3) * 0.22;
    int side = data.y >> 26 & 7;

    normal = normals[side];

    float alpha = time * 3.1415926536;
    float sinAlpha = sin(alpha);
    float cosAlpha = cos(alpha);
    vec3 sunDirection = vec3(cosAlpha - sinAlpha, -0.3, cosAlpha + sinAlpha);
    float absTime = abs(time);
    float sunIllumination = dot(normal, sunDirection) * 0.2 * skyLight * absTime;

    fragLight = max(blockLight + 0.3, max(0.3, skyLight) * (absTime * 0.75 + 0.25) + sunIllumination) * ambientOcclusionLevel;
}