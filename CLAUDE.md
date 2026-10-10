# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A tiny Kotlin DSL for ANSI-colored console output (Kotlin Multiplatform: `jvm`, `js`, `wasmJs`, `wasmWasi` and the native `linuxX64` and `mingwX64` targets; no Apple targets, because their test binaries need Xcode; `linuxX64` and `mingwX64` cover the same Kotlin/Native backend). The whole library is one file, `src/commonMain/kotlin/com/github/mm/coloredconsole/ColoredConsole.kt`, and the README tells users to **copy that single file into their project**. Keep it self-contained: no dependencies, no extra source files, and no `java.*` / platform APIs (it lives in `commonMain`). Its header carries the full MIT notice, copied from `LICENSE`, because copies of the file don't include `LICENSE`; keep the two in sync (e.g. the copyright years).

## Build and test

Gradle 9.8 wrapper, Kotlin 2.4 (version in `gradle/libs.versions.toml`), and `jvmToolchain(25)`: compiles to Java 25 bytecode, and the foojay resolver in `settings.gradle.kts` downloads JDK 25 if it isn't installed. The configuration cache is on (`gradle.properties`). CI (`.github/workflows/ci.yml`) runs `./gradlew check` on Linux and Windows for pull requests and pushes to `master`, and on Linux also fails if `./gradlew readmeImages` changes or adds anything in `.images/`; `IMPROVEMENTS.md` tracks planned work.

```sh
./gradlew check          # compile + all tests this host can run
./gradlew allTests       # tests only
./gradlew jvmTest        # JVM tests only (fastest; the README tests are JVM-only)
./gradlew jsTest wasmJsTest wasmWasiTest   # common tests on Node.js

# single test: the unit tests are class TestANSI (the file is ColoredConsoleTest.kt)
./gradlew jvmTest --tests 'com.github.mm.coloredconsole.TestANSI.gradientColorsEveryCharacter'
./gradlew jvmTest --tests 'readme.ReadmeExamplesTest'
```

Every target compiles `commonMain` and runs `commonTest`, which is what enforces the no-platform-API rule. Native tests run only on their own OS (`linuxX64Test` on Linux, `mingwX64Test` on Windows); on macOS they are compiled and linked but skipped, silently because of `kotlin.native.ignoreDisabledTargets=true` in `gradle.properties`. The first build downloads Node.js (into the Gradle cache) and the Kotlin/Native compiler and its dependencies (into `~/.konan`, about 2.5 GB). `kotlin-js-store/yarn.lock` pins the npm packages of the JS test runner: commit it when it changes (e.g. after a Kotlin upgrade; `./gradlew kotlinUpgradeYarnLock` rewrites it). There is no Wasm lock file: the Wasm tests need no npm packages, and on Windows yarn writes no `build/wasm/yarn.lock`, which made Kotlin's `kotlinWasmStoreYarnLock` fail on its missing input; an `onlyIf` in `build.gradle.kts` skips that task when the file is missing (remove it once a Kotlin version handles this). The Wasm targets need `@OptIn(ExperimentalWasmDsl::class)` in `build.gradle.kts`, and Node prints a harmless `ExperimentalWarning` for WASI.

`TestANSI` (commonTest) holds the unit tests. `src/jvmTest/kotlin/readme/ReadmeExamplesTest.kt` captures what each README example in `ReadmeImages.kt` prints and compares it with an expected string, written with `\e` for ESC in raw strings (`"""\e[1mx\e[0m"""`); captured output goes through the same `\e` replacement, so failures are readable. Every example except `palette` needs an entry (`everyExampleHasAnExpectedOutput`). The gradient's expected codes are computed by a separate linear interpolation in the test, not copied from the library.

The README images (`.images/*.svg`) are generated, not screenshots: `src/jvmTest/kotlin/readme/ReadmeImages.kt` holds verbatim copies of the README snippets (in package `readme`, with only the imports the README lists, so `check` also proves the documented imports compile), captures what each prints, and renders the ANSI codes as a terminal-window SVG (including 256-color and RGB codes, using the xterm palette for 16-255). When you change a README example, change its copy there and its expected output in `ReadmeExamplesTest.kt` too, and run `./gradlew readmeImages`; the output is deterministic, so unchanged examples leave the SVGs untouched, and CI fails when a committed SVG is stale. A README snippet whose output depends on the environment (the `NO_COLOR` / terminal check) is copied into `ReadmeImages.kt` as a function outside `examples`: compiled, but neither rendered nor asserted. Two rendering rules matter: spaces are written as no-break spaces (browsers collapse edge spaces in SVG text), and every run gets an explicit `x` and `textLength` on a 9px grid so backgrounds line up whatever monospace font the viewer has.

## Architecture (`ColoredConsole.kt`)

Everything is a member of the `ColoredConsole` interface, so it is only in scope inside a `ColoredConsole` receiver: `colored { }`, `style { }`, the top-level `print { }` / `println { }` lambda overloads, or a class that implements `ColoredConsole`.

Two parallel APIs produce the escape codes:

1. **Extension properties/functions on any value `N`** (`"x".red.bold`, `x.cyan { predicate }`, `bold(text)`) apply the matching `Style` through `N.style(…)` and return a `String` immediately.
2. **The `Style` sealed class** (`Simple(code)`, `Extended(codes)`, `Gradient(from, to)`, `Composite(parent, child)`, `NotApplied`) is built with `red`, `green + bold`, or `style { … }`, and applied with `"x".style(s)` or `"x"(s)`. `a + b` builds `Composite(parent = b, child = a)`, so the right operand wraps outermost.

Non-obvious behavior:

- **Nesting** works because `applyCodes` splits the text on the reset sequence `ESC[0m` and re-applies the new codes to every segment, so an outer style resumes after an inner one ends.
- **Extended colors** (`rgb`, `color256`) are one escape code with several parameters (`38;2;r;g;b`, `38;5;n`; `48;…` as background), held by `Style.Extended`. So color handling works on parameter lists (`List<Int>.isColor`, `toBackground`, `toBright`) and the string regexes match `[\d;]`; `.bg` turns `38` into `48`, `.bright` leaves extended colors alone but still stops at them. `stripAnsi()` uses the general CSI pattern, so it also removes non-color sequences such as `ESC[2K`.
- **Gradients** (`Style.Gradient`, holding two 0xRRGGBB values and a `background` flag) give *every visible character its own reset-terminated segment*: the gradient's true-color code, then the inner codes seen earlier in that segment (so inner styles still win), the character, a reset. That is what lets `.bg`, `.bright` and outer styles reach every character, as they do for `applyTags` output. Characters are split with the same CSI regex as `stripAnsi` and keep surrogate pairs together. End colors are resolved when the gradient is created (`Style.rgbValue`): named colors and `color256(0..15)` use the `xtermColors` table, not the README generator's display palette.
- **`.bg` and `.bright` change the most recently applied color**, wherever it is: `"x".cyan.bold.bg == "x".cyan.bg.bold`. On a `Style`, `changeLatestColor` searches a `Composite`'s `parent` before its `child` (the parent was added later). On a `String`, there is no structure left, so `String.changeLatestColor` relies on `applyCodes` starting every reset-separated segment with the codes of all styles applied to it, most recent first; it changes the first color among each segment's *leading* codes. A color that is already a background stops `.bg` (and an already bright one stops `.bright`) rather than reaching an older color.
- **Disabled mode** (`colored(enabled = false)`, `println(colored = false) { }`) passes a receiver that implements the empty private marker `ColorConsoleDisabled`. Only `N.style()` and `N.wrap()` emit codes, and both check the private `stylingEnabled`; every named style and color goes through `style()`. `Style` values are the same in both modes, and `Style.wrap()` called directly never checks the flag. `NotApplied` is kept for API compatibility but the library no longer creates it.
- **Adding a style or color** means adding a constant in the `companion object` plus the five-member block (`val x: Style`, `val <N : Style> N.x`, `val <N> N.x`, `fun <N> N.x(predicate)`, `fun x(text: Any)`), copying an existing `region` block. Inside the block, refer to the `Style` as `this@ColoredConsole.x`: an unqualified `x` in the `N.x` members resolves to the `N.x` extension itself and recurses forever. Add the style to the list in `disabledStylesEmitNoCodes`.
- The top-level `print(colored, block)` / `println(colored, block)` overloads take a lambda that returns a `String` and are distinct from `kotlin.io.print(ln)`. Inside a `colored { }` block, `println("…")` is the normal `kotlin.io` one.
