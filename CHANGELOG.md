**This version only compatible with Iris&Oculus 1.7.0 and above.**

### 1.1.3
- Fix issue [#146](https://github.com/leon-o/iris-flw-compat/issues/146).
  - Fix issues in PBR. Normals and tangents of moving contraptions now are correct.
  - Support Parallax Occlusion Mapping.
- Fix issue [#150](https://github.com/leon-o/iris-flw-compat/issues/150).
  - Replace `vertex v` with `vertex _flw_v` to avoid conflict with existing variables.

**New compatible shaders**:
- Seus PTGI HRR 3
- Shrimple
- UShader

There are still many that have not been tested but may be compatible
