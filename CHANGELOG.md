**This version only compatible with _Iris_ 1.8.0 above and _Sodium_.**

### 2.0.1
- Fix issue [#203](https://github.com/leon-o/iris-flw-compat/issues/203): Disable shaders will result in improper rendering.
- Maybe fix issue [#205](https://github.com/leon-o/iris-flw-compat/issues/205). Same reason as above issue, I guess.

### 2.0.0
- Support Create 6.0.

### 1.1.4
- Fix issue [#168](https://github.com/leon-o/iris-flw-compat/issues/168)
  - Add compatibility for iris 1.7.5 in 1.20.1.
  - Add compatibility for oculus & DH 2.2.0.

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
