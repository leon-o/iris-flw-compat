**This version only compatible with Iris&Oculus 1.7.0 and above.**

TL;DR: 
- Fix issues in **PBR** and **Parallax Occlusion Mapping**.
- Support **Seus PTGI HRR 3**, **Shrimple**, and **UShader**, etc.
- Includes Flywheel in the jar file, so you don't need to install it separately.

### 1.1.4
- Fix issue [#168](https://github.com/leon-o/iris-flw-compat/issues/168)
  - Add compatibility for iris 1.7.5 in 1.20.1.

### 1.1.3
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
