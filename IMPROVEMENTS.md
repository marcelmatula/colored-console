# Improvements

Ideas for improving Colored Console, roughly in order of value within each section.

## Project

- [x] **License.** The project is licensed under the [MIT License](LICENSE).
- [x] **Continuous integration.** [`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs `./gradlew check` on every pull request and on every push to `master`.
- [x] **License notice in `ColoredConsole.kt`.** Users copy only this one file, so the `LICENSE` file does not travel with it. The file now starts with the full MIT notice and an `SPDX-License-Identifier: MIT` line, so the license stays attached to every copy.

## Code

- [x] **Handle disabled styling in one place.** Every named style and color now goes through `style()`, and only `style()` and `wrap()` check whether styling is enabled. `ColorConsoleDisabled` no longer overrides each style (that is how `faint` and `strike` leaked codes before 1.0.0), so a new style cannot be missed.
- [x] **Make `.bg` and `.bright` independent of order.** They change the most recently applied color, so `"x".cyan.bold.bg` is the same as `"x".cyan.bg.bold`, and `(green + underline).bright` works. On nested text they now change every segment, not just the first.
- [x] **Consistent function signatures.** `italic(text)`, `red(text)` and the other plain functions accept any value, like `bold(text)` and `faint(text)`.
- [x] **Remove leftover code.** The no-op `check(true)` and the commented-out contract in `colored()` are gone. `wrap()` with several codes also no longer puts a literal `, ` between them.

## Features

- [x] **256-color and true-color support:** `"x".rgb(255, 135, 0)` and `"x".color256(208)`, also as styles (`rgb(…) + bold`) and with `.bg`.
- [x] **More color names:** `magenta` (the same as `purple`) and `gray` (bright black).
- [x] **`stripAnsi()` and `visibleLength`:** remove escape codes, or measure only the visible characters, for example to align styled text in a table.
- [x] **Two-color gradients:** `"text".gradient(from, to)`, also as a style and with `.bg` for a background gradient.

## Tests and documentation

- [x] **Assert the output of the examples.** [`ReadmeExamplesTest`](src/jvmTest/kotlin/readme/ReadmeExamplesTest.kt) captures what each README example prints and compares it with the expected escape codes, so wrong output fails the build. It replaces `TestANSI.mainTest`, which only printed older copies of the examples.
- [x] **Fail CI when the README images are stale.** CI runs `./gradlew readmeImages` and fails if anything in `.images/` changes, including a new image that was not committed. The generator is deterministic, so this only fails when an example's output really changed.
- [x] **Document turning colors off automatically.** The README shows how to turn styling off on the JVM when the output is not a terminal or the [`NO_COLOR`](https://no-color.org) environment variable is set. It stays out of the library, because environment access is platform-specific and would break the single-file design.
- [ ] **Verify Windows support.** Current Windows terminals understand ANSI escape codes when virtual terminal processing is on. CI now also builds and tests on Windows, and the README says Windows is untested rather than unsupported. Still to do: run the examples in Windows Terminal and in the classic console (`cmd.exe`, PowerShell), then update the README.

## Larger steps

- [x] **Compile more Kotlin Multiplatform targets.** The build also targets JS, Wasm (`wasmJs` and `wasmWasi`, both tested on Node.js) and Kotlin/Native (`linuxX64`, `mingwX64`), and the common tests run on each of them. CI runs the native tests on Linux and Windows. Apple targets are left out: their test binaries need Xcode, and they use the same Kotlin/Native backend that `linuxX64` and `mingwX64` test.
- [ ] **Publish to Maven Central or JitPack**, so the library can be added as a dependency instead of copied. The published jar would then need a lower JVM target than the Java 25 bytecode the build produces today.
