#include "flywheel:internal/common.vert"
#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/instancing/light.glsl"

uniform uvec2 _flw_packedMaterial;
uniform int _flw_baseInstance = 0;

#ifdef FLW_EMBEDDED
uniform mat4 _flw_modelMatrixUniform;
uniform mat3 _flw_normalMatrixUniform;
#endif

uniform uint _flw_vertexOffset;

void main() {
    _flw_unpackMaterialProperties(_flw_packedMaterial.y, flw_material);

    FlwInstance instance = _flw_unpackInstance(_flw_baseInstance + gl_InstanceID);

    #ifdef FLW_EMBEDDED
    _flw_modelMatrix = _flw_modelMatrixUniform;
    _flw_normalMatrix = _flw_normalMatrixUniform;
    #endif

    _flw_main(instance, uint(gl_InstanceID), _flw_vertexOffset);

//    tint = flw_vertexColor;
//    uv = flw_vertexTexCoord;
//
//    vec3 skewedNormal = flw_vertexNormal + vec3(0.5,0.5,0.5);
//    vec4 fakeTagent = vec4(normalize(skewedNormal - flw_vertexNormal * dot(skewedNormal, flw_vertexNormal)).xyz,1.0);
//    tbn[0] = mat3(gbufferModelViewInverse) * normalize(fakeTagent.xyz);
//    tbn[2] = mat3(gbufferModelViewInverse) * normalize(flw_vertexNormal);
//    tbn[1] = cross(tbn[0], tbn[2]) * sign(fakeTagent.w);
//
//    light_levels = flw_vertexLight;
    gl_Position = flw_vertexPos;
}
