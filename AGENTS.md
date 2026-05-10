# AGENTS.md - Development Guide for iris-flw-compat

## Project Overview

iris-flw-compat is a Minecraft NeoForge mod that provides compatibility between Iris (shader loader) and Flywheel (GPU instancing). It allows GPU instancing optimization when using shader packs.

## Technology Stack

- **Language**: Java 21
- **Build System**: Gradle with NeoForge ModDev plugin
- **Mappings**: Parchment (Yarn-based)
- **Platforms**: NeoForge
- **Dependencies**: Iris, Flywheel, Create, Sodium

## Build Commands

### Standard Build
```bash
./gradlew build
```
Builds the mod JAR in `build/libs/`

### Run Development Client
```bash
./gradlew runClient
```
Launches Minecraft with the mod installed for testing

### Run Dedicated Server
```bash
./gradlew runServer
```

### Run with Game Tests
```bash
./gradlew runGameTestServer
```
Runs NeoForge GameTests (configured for `top.leonx.irisflw` namespace)

### Generate Assets
```bash
./gradlew runData
```
Runs data generation (required after modifying recipes/loot tables/tags)

### Clean Build
```bash
./gradlew clean
```

### IDE Sync
```bash
./gradlew genIdeGradleEntries
```
Generates IDE configuration for IntelliJ/Eclipse

## Project Structure

```
src/main/java/top/leonx/irisflw/     # Main source code
  - IrisFlwNeoForge.java             # Main mod entry point
  - IrisFlw.java                     # Common initialization
  - config/                          # Configuration files
  - mixin/                           # Mixin injectors
    - iris/                          # Iris compatibility
    - flw/                          # Flywheel patches
    - create/                       # Create mod patches
  - backend/                        # Rendering backend logic
  - flywheel/                       # Flywheel integration
  - iris/                          # Custom GL uniform handling
  - transformer/                   # GLSL shader transformation
  - accessors/                     # Mixin accessor interfaces
```

## Rendering Compatibility Architecture

详细架构笔记放在 `docs/rendering-architecture.md`。修改 shader 编译、Flywheel 后端、Sable 兼容、Create 渲染修复前，先读这份文档。

快速索引：
- `IrisFlwBackends` 注册 Iris 感知的 Flywheel instancing 后端。只有在 GPU instancing 可用、Iris 正在使用 shader pack、且 `IrisInstancingPrograms` 已加载时才启用。
- `MixinFlwPrograms` 在 Flywheel 重载 shader source 时重建 `IrisInstancingPrograms`。`IrisPipelineCompiler`、`IrisCompilationHarness`、`IrisProgramLinker` 把 Flywheel 原生 GL program 编译链路替换成 Iris `ShaderInstance` 创建链路。
- `GlslTransformerVertPatcher` 是核心 shader 桥接层。它把 Flywheel 顶点管线注入 Iris shaderpack 顶点程序，把 vanilla/Iris terrain attribute 重映射到 Flywheel instance 数据，并在 Sable 加载时处理 Sable 特有的光照函数签名。
- `IrisFlwCompatGlProgram` 把 Iris `ShaderInstance` 适配成 Flywheel `GlProgram`，负责上传 Flywheel material/base-instance 等 uniform，并且只在 Sable 兼容模式下使用 signed Flywheel uniform。
- `IrisInstancedDrawManager` 基本复刻 Flywheel instancing draw manager，但会请求 Iris/Flywheel 混合 program、跳过 invalid program 占位、绑定 Flywheel 光照纹理，并保留 shadow、OIT、crumbling 路径。
- `assets/flywheel/flywheel/internal/iris_instancing/` 下的自定义 shader 替代 Flywheel 原生 instancing 入口，用于被合并进 Iris shaderpack program。

重要兼容规则：
- 不要把 Sable 的 `uint` 到 `int` workaround 全局化。它只用于 `IrisFlw.isSableLoaded()` 为 true 时绕开 Veil 的 `Unsupported Uniform Type: unsigned int` 路径。
- 调用 Sable 的 `flw_light(...)` 或 `flw_lightFetch(...)` 后，只有返回 `true` 才能使用输出值；否则 light/AO 可能是未初始化数据，会导致疯狂闪烁。
- 增加 shader 资源时继续通过 `MixinProgramSamplers` 保留 Flywheel sampler unit，否则 Iris 可能把 shaderpack sampler 分配到同一组纹理单元。
- 对 embedded Flywheel visual，必须保留 Sable 的 lighting scene ID、scene matrix、render-origin 规则和 sky-light scale。

## Code Style Guidelines

### General Conventions

- **Package naming**: `top.leonx.irisflw` (reverse domain)
- **Class naming**: PascalCase (e.g., `IrisFlwBackends`)
- **Method/variable naming**: camelCase (e.g., `init()`, `isShaderPackInUse()`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MOD_ID`)
- **Final modifier**: Use for all constants

### Imports

- Use explicit imports (no wildcards)
- Order: java.*, javax.*, net.minecraft.*, org.spongepowered.*, third-party, project
- Group imports by package with blank lines between groups

### Mixin Conventions

- Mixins go in `mixin/<target-package>/` directories
- Naming: `Mixin<TargetClass>.java`
- Use `@Unique` for methods that must not be renamed
- Use `remap = false` when injecting into non-remapped methods
- Accessors go in `accessors/` package with `*Accessor` suffix
- Mixin JSONs in `src/main/resources/` named `irisflw.mixins.<target>.json`

### Annotations

- `@Mixin(ClassName.class)` - Required for all mixin classes
- `@Inject` / `@Redirect` / `@ModifyArg` - For method modifications
- `@Inject(at = @At("TAIL"))` - Common injection point
- `@Environment(EnvType.CLIENT)` - Client-only code
- `@SubscribeEvent` - Event handlers

### Error Handling

- Use `IrisFlw.LOGGER` for logging (SLF4J)
- Log levels: `warn()`, `info()`, `debug()`, `error()`
- Provide context in error messages
- Use `try-catch` for potentially failing operations

### Null Safety

- Prefer `@Nullable` annotations when null is possible
- Use `Objects.requireNonNull()` for parameters that must not be null
- Prefer `Optional` for return values that may be absent

## Testing

### Manual Testing

1. Run `./gradlew runClient` to launch Minecraft with the mod
2. Enable shader packs in Iris settings
3. Spawn Create mod machinery (gears, cogs, etc.)
4. Verify entities render correctly with instancing enabled

### Game Tests

The project uses NeoForge's built-in game testing framework. Tests are run against the `top.leonx.irisflw` namespace.

```bash
./gradlew runGameTestServer
```

### Debugging Tips

- Enable Mixin debug output in `build.gradle` by uncommenting `'mixin.debug.export'`
- Check `run/logs/` for runtime logs
- Use `IrisFlw.LOGGER.debug()` for development logging

## Common Development Tasks

### Adding a New Mixin

1. Create mixin class in appropriate `mixin/` subdirectory
2. Create/extend accessor interface if needed in `accessors/`
3. Register in appropriate `irisflw.mixins.<target>.json`
4. Add `client` or appropriate array entry

### Updating Dependencies

Edit versions in `gradle.properties`:
- `minecraft_version` - Minecraft version
- `neoforge_version` - NeoForge version
- `iris_version` - Iris version
- `flywheel_neoforge_version` - Flywheel version

### Building for Release

```bash
./gradlew build
```

Output JAR will be in `build/libs/` with version in filename.

### Release Procedure

We follow a **tag-driven release** model. The CI workflow (`.github/workflows/publish.yml`) triggers automatically when a version tag is pushed. Do not create releases manually through the GitHub web UI.

**Prerequisites before tagging:**
1. Update `gradle.properties`:
   - `mod_version` → the new mod version (e.g., `2.4.0`)
   - `minecraft_version` → target Minecraft version (e.g., `1.21.1`)
   - Update any dependency versions as needed
2. Update `CHANGELOG.md` following the [Updating CHANGELOG](#updating-changelog) rules above.
3. Commit these changes: `git commit -am "chore: release 2.4.0"`
4. Push to the target branch: `git push origin main`

**Creating and pushing the release tag:**

Tag format: `<mc_version>+<mod_version>-<version_type>`

```bash
# Example: release
git tag -a "1.21.1+2.4.0-release" -m "Release 1.21.1+2.4.0-release"
git push origin "1.21.1+2.4.0-release"

# Example: beta
git tag -a "1.21.1+2.4.0-beta" -m "Release 1.21.1+2.4.0-beta"
git push origin "1.21.1+2.4.0-beta"
```

**What happens automatically:**
1. CI parses the tag to extract `mc_version`, `mod_version`, and `version_type`.
2. CI validates these match `gradle.properties`. If not, the job fails.
3. CI builds the mod JAR (`./gradlew clean build`) without overriding the version.
4. CI extracts the matching version section from `CHANGELOG.md`.
5. CI publishes to GitHub Releases, CurseForge, and Modrinth.

**Emergency fallback (`workflow_dispatch`):**
If a platform-specific publish step failed on the tag push workflow (e.g., CurseForge API was down), go to the Actions tab and **re-run the failed jobs** from the original tag run. Do not use `workflow_dispatch` for this.

`workflow_dispatch` is reserved for edge cases where you must publish from a branch without a tag. It reads versions from `gradle.properties`, accepts one `version_type` input, skips GitHub Release (no tag = no GitHub release), and publishes only to CurseForge/Modrinth.

### Updating CHANGELOG

- Only include **user-visible** changes: new features, bug fixes, and compatibility improvements.
- Do **not** list internal refactors, dependency bumps, or code cleanups unless they directly fix a user-reported issue.
- Each version entry should focus on the **top 2–3 most impactful** items; secondary fixes can be omitted unless users ask about them.
- Keep only the **latest 5 versions** in the file; delete older entries when adding a new release.
- Write bullet points in plain language (e.g., "Fix ghost block preview turning black under shader packs.") rather than implementation details (e.g., "routing through gbuffers_terrain").

## Configuration

- Mod config file: `irisflw.toml` in `run/config/`
- Config class: `src/main/java/top/leonx/irisflw/config/IrisFlwConfig.java`
- Config screen: Integrated with NeoForge's config GUI

## External Resources

- [NeoForge Docs](https://docs.neoforged.net/)
- [Iris API](https://irisshaders.github.io/)
- [Flywheel](https://github.com/ferriarmedia/flywheel)
- [Parchment Mappings](https://github.com/ParchmentMC/Parchment)
- [glsl-transformer](https://github.com/IrisShaders/glsl-transformer)
