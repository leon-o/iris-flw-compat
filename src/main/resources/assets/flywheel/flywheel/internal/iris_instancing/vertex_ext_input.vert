layout(location = 0) in vec3 _flw_aPos;
layout(location = 1) in vec4 _flw_aColor;
layout(location = 2) in vec2 _flw_aTexCoord;
//layout(location = 3) in vec2 _flw_aOverlay;
layout(location = 3) in vec2 _flw_aLight;
layout(location = 4) in vec4 _flw_aNormal;
layout(location = 5) in ivec4 _flw_aExtended;
layout(location = 6) in vec2 _flw_v_mc_Entity;

vec4 _flw_at_tangent;
vec4 _flw_at_midBlock;
vec4 _flw_mc_midTexCoord;

float unpackTangentX(int val) {
    return float(val & 255) * 0.007874016 - 1.0;
}
float unpackTangentY(int val) {
    return float((val >> 8) & 255) * 0.007874016 - 1.0;
}
float unpackTangentZ(int val) {
    return float((val >> 16) & 255) * 0.007874016 - 1.0;
}
float unpackTangentW(int val) {
    return float((val >> 24) & 255) * 0.007874016 - 1.0;
}
float unpackMidBlockX(int val) {
    return float(val & 255) * 0.015625 - 2.0;
}
float unpackMidBlockY(int val) {
    return float((val >> 8) & 255) * 0.015625 - 2.0;
}
float unpackMidBlockZ(int val) {
    return float((val >> 16) & 255) * 0.015625 - 2.0;
}
// In Iris 1.7 and newer, the last component stores the light level of the current block
float unpackMidBlockW(int val) {
    return float((val >> 24) & 255);
}

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_aPos, 1.0);
    flw_vertexColor = _flw_aColor;
    flw_vertexTexCoord = _flw_aTexCoord;
    // Integer vertex attributes explode on some drivers for some draw calls, so get the driver
    // to cast the int to a float so we can cast it back to an int and reliably get a sane value.
    flw_vertexOverlay = ivec2(0);//ivec2(_flw_aOverlay);
    flw_vertexLight = _flw_aLight / 256.0;
    flw_vertexNormal = _flw_aNormal.xyz;

    _flw_mc_midTexCoord = vec4(intBitsToFloat(_flw_aExtended.xy), 0.0, 1.0);
    int pTangent = _flw_aExtended.z;
    int pMidBlock = _flw_aExtended.w;
    _flw_at_tangent = vec4(unpackTangentX(pTangent), unpackTangentY(pTangent), unpackTangentZ(pTangent), unpackTangentW(pTangent));
    _flw_at_midBlock = vec4(unpackMidBlockX(pMidBlock), unpackMidBlockY(pMidBlock), unpackMidBlockZ(pMidBlock), unpackMidBlockW(pMidBlock));
}
