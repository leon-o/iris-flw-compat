#include "flywheel:internal/common.frag"
#include "flywheel:internal/instancing/light.glsl"

#ifdef IRISFLW_SABLE_COMPAT
#define IRISFLW_UNIFORM_UVEC2 ivec2
#define IRISFLW_UNIFORM_TO_UINT(value) uint(value)
#else
#define IRISFLW_UNIFORM_UVEC2 uvec2
#define IRISFLW_UNIFORM_TO_UINT(value) value
#endif

uniform IRISFLW_UNIFORM_UVEC2 _flw_packedMaterial;

void main() {
    _flw_unpackUint2x16(IRISFLW_UNIFORM_TO_UINT(_flw_packedMaterial.x), _flw_uberFogIndex, _flw_uberCutoutIndex);
    _flw_unpackMaterialProperties(IRISFLW_UNIFORM_TO_UINT(_flw_packedMaterial.y), flw_material);

    _flw_main();
}
