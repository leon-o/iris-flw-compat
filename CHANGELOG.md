TL;DR: 
- Fix issues in **PBR** and **Parallax Occlusion Mapping**.
- Support **Seus PTGI HRR 3**, **Shrimple**, and **UShader**, etc.
- Includes Flywheel in the jar file, so you don't need to install it separately.

### 1.0.3
- Fix issue [#146](https://github.com/leon-o/iris-flw-compat/issues/146).
  - Fix issues in PBR. Normals and tangents of moving contraptions now are correct.
  - Support Parallax Occlusion Mapping.
- Fix issue [#150](https://github.com/leon-o/iris-flw-compat/issues/150).
  - Replace `vertex v` with `vertex _flw_v` to avoid conflict with existing variables.
- Include Flywheel in jar file.
  - Because some users don't use the Create mod and have trouble with the Flywheel installation, I decided to include it in the jar file.

**New compatible shaders**:
- Seus PTGI HRR 3
- Shrimple
- UShader

There are still many that have not been tested but may be compatible.
