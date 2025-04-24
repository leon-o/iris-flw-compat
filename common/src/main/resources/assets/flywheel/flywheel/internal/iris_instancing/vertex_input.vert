layout(location = 0) in vec3 _flw_aPos;
layout(location = 1) in vec4 _flw_aColor;
layout(location = 2) in vec2 _flw_aTexCoord;
layout(location = 3) in vec2 _flw_aOverlay;
layout(location = 4) in vec2 _flw_aLight;
layout(location = 5) in vec3 _flw_aNormal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_aPos, 1.0);
    flw_vertexColor = _flw_aColor;
    flw_vertexTexCoord = _flw_aTexCoord;
    // Integer vertex attributes explode on some drivers for some draw calls, so get the driver
    // to cast the int to a float so we can cast it back to an int and reliably get a sane value.
    flw_vertexOverlay = ivec2(_flw_aOverlay);
    flw_vertexLight = _flw_aLight / 256.0;
    flw_vertexNormal = _flw_aNormal;
}
