**This version only compatible with _Iris_ 1.8.0 above and _Sodium_.**

### 2.4.0
- Fix ghost block preview turning black under shader packs.
- Add ordered dithering (Bayer / IGN) for ghost block translucency; choose the method in config (default IGN).

### 2.3.1
- Fixed Sable embedded Flywheel rendering issues and Veil's "Unsupported Uniform Type: unsigned int" error.

### 2.3.0
- Fixed compatibility issues with Sable and Create Aeronautics.

### 2.2.0
- Refactored to NeoForge-only: removed architectury (Fabric version will be developed separately in a new branch).
- Fixed block breaking crumbling effect: vertex shader now uses position-based UV coordinates for crumbling texture.

### 2.1.2
- It is strongly recommended that you upgrade to this version.
- Completely solved the rendering issue of Create mod's Schematic and Outline rendering.
- Add configuration option to replace Create's checkerboard texture for better shader compatibility.
