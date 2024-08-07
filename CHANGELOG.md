## This version only compatible with Iris&Oculus 1.7.0 and above.

## 1.1.2
1. Fix issue [#143](https://github.com/leon-o/iris-flw-compat/issues/143), [#36](https://github.com/leon-o/iris-flw-compat/issues/136), [#89](https://github.com/leon-o/iris-flw-compat/issues/89): OpenGL error messages occur when shaders have too many attributes. Now is completely compatible with Bliss and Euphoria Patches.

## 1.1.1
1. Fix issue [#125](https://github.com/leon-o/iris-flw-compat/issues/125): occasional crashes when shaders are not enabled. Thanks [MoePus](https://github.com/leon-o/iris-flw-compat/pull/141) for the contribution.

## 1.1.0
1. Fix lighting issues of the moving contraption.
   - The vertex light strength was incorrectly set to a fixed value, causing the moving contraption won't be affected by the light source.
2. Use architecture to reorganize the project.
   - Forge and Fabric forge are now merged into a single project, which is more convenient for future development.
3. New version number format.
   - Now the version number starts from 1.0.0. The first number represents the major version and will be increased when the mod has a significant update. The second number represents the Iris/Oculus compatibility and will be increased when the mod is no longer compatible with older versions of iris.