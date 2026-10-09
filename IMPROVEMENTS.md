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

- [ ] **256-color and true-color support**, for example `"x".rgb(255, 128, 0)` and `"x".color256(208)` (SGR codes `38;5;n` and `38;2;r;g;b`, plus their background variants).
- [ ] **More color names:** `magenta` as an alias for `purple`, and `gray` for bright black.
- [ ] **`stripAnsi()` and visible length.** Padding a colored string to align a table counts its escape codes, so columns come out misaligned. Helpers that remove the codes or measure only the visible text would fix this.

## Tests and documentation

- [ ] **Assert the output of the examples.** `TestANSI.mainTest` only prints the examples, so it catches compile errors and exceptions but not wrong output. Comparing the captured output with expected escape sequences would catch regressions.
- [ ] **Fail CI when the README images are stale.** Run `./gradlew readmeImages` in CI and fail if anything in `.images/` changes. The generator is deterministic, so this only fails when an example's output really changed.
- [ ] **Document turning colors off automatically**, for example when the output is not a terminal or the [`NO_COLOR`](https://no-color.org) environment variable is set. This belongs in the README rather than the library, because environment access is platform-specific and would break the single-file design.
- [ ] **Verify Windows support.** The README says Windows is not supported, but current Windows terminals understand ANSI escape codes. Test it and update the README.

## Larger steps

- [ ] **Compile more Kotlin Multiplatform targets** (JS, Wasm, native). The library lives in common code, but only the JVM target is built, so compatibility with other platforms is untested.
- [ ] **Publish to Maven Central or JitPack**, so the library can be added as a dependency instead of copied. The published jar would then need a lower JVM target than the Java 25 bytecode the build produces today.
