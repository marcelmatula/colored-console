# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A tiny Kotlin DSL for ANSI-colored console output (Kotlin Multiplatform, only a `jvm()` target configured). The whole library is one file, `src/commonMain/kotlin/com/github/mm/coloredconsole/ColoredConsole.kt`, and the README tells users to **copy that single file into their project**. Keep it self-contained: no dependencies, no extra source files, and no `java.*` / platform APIs (it lives in `commonMain`). Its header carries the full MIT notice, copied from `LICENSE`, because copies of the file don't include `LICENSE`; keep the two in sync (e.g. the copyright years).

## Build and test

Gradle 9.8 wrapper, Kotlin 2.4 (version in `gradle/libs.versions.toml`), and `jvmToolchain(25)`: compiles to Java 25 bytecode, and the foojay resolver in `settings.gradle.kts` downloads JDK 25 if it isn't installed. The configuration cache is on (`gradle.properties`). CI (`.github/workflows/ci.yml`) runs `./gradlew check` on pull requests and pushes to `master`; `IMPROVEMENTS.md` tracks planned work.

```sh
./gradlew check          # compile + all tests
./gradlew jvmTest        # tests only

# single test: the class is TestANSI (the file is ColoredConsoleTest.kt)
./gradlew jvmTest --tests 'com.github.mm.coloredconsole.TestANSI.mainTest'

# also show the raw colored output the test prints (-i), even if the task is up to date (--rerun)
./gradlew jvmTest --rerun -i
```

`TestANSI.mainTest` has **no assertions**. It prints the README examples, so it only catches compile errors and exceptions; apart from `disabledStylesEmitNoCodes`, the escape sequences have to be checked by eye (`-i` output, or `<system-out>` in `build/test-results/jvmTest/*.xml`, where ESC shows as `?`).

The README images (`.images/*.svg`) are generated, not screenshots: `src/jvmTest/kotlin/readme/ReadmeImages.kt` holds verbatim copies of the README snippets (in package `readme`, with only the imports the README lists, so `check` also proves the documented imports compile), captures what each prints, and renders the ANSI codes as a terminal-window SVG (including 256-color and RGB codes, using the xterm palette for 16-255). When you change a README example, change its copy there too and run `./gradlew readmeImages`; the output is deterministic, so unchanged examples leave the SVGs untouched. Two rendering rules matter: spaces are written as no-break spaces (browsers collapse edge spaces in SVG text), and every run gets an explicit `x` and `textLength` on a 9px grid so backgrounds line up whatever monospace font the viewer has.

## Architecture (`ColoredConsole.kt`)

Everything is a member of the `ColoredConsole` interface, so it is only in scope inside a `ColoredConsole` receiver: `colored { }`, `style { }`, the top-level `print { }` / `println { }` lambda overloads, or a class that implements `ColoredConsole`.

Two parallel APIs produce the escape codes:

1. **Extension properties/functions on any value `N`** (`"x".red.bold`, `x.cyan { predicate }`, `bold(text)`) apply the matching `Style` through `N.style(…)` and return a `String` immediately.
2. **The `Style` sealed class** (`Simple(code)`, `Extended(codes)`, `Composite(parent, child)`, `NotApplied`) is built with `red`, `green + bold`, or `style { … }`, and applied with `"x".style(s)` or `"x"(s)`. `a + b` builds `Composite(parent = b, child = a)`, so the right operand wraps outermost.

Non-obvious behavior:

- **Nesting** works because `applyCodes` splits the text on the reset sequence `ESC[0m` and re-applies the new codes to every segment, so an outer style resumes after an inner one ends.
- **Extended colors** (`rgb`, `color256`) are one escape code with several parameters (`38;2;r;g;b`, `38;5;n`; `48;…` as background), held by `Style.Extended`. So color handling works on parameter lists (`List<Int>.isColor`, `toBackground`, `toBright`) and the string regexes match `[\d;]`; `.bg` turns `38` into `48`, `.bright` leaves extended colors alone but still stops at them. `stripAnsi()` uses the general CSI pattern, so it also removes non-color sequences such as `ESC[2K`.
- **`.bg` and `.bright` change the most recently applied color**, wherever it is: `"x".cyan.bold.bg == "x".cyan.bg.bold`. On a `Style`, `changeLatestColor` searches a `Composite`'s `parent` before its `child` (the parent was added later). On a `String`, there is no structure left, so `String.changeLatestColor` relies on `applyCodes` starting every reset-separated segment with the codes of all styles applied to it, most recent first; it changes the first color among each segment's *leading* codes. A color that is already a background stops `.bg` (and an already bright one stops `.bright`) rather than reaching an older color.
- **Disabled mode** (`colored(enabled = false)`, `println(colored = false) { }`) passes a receiver that implements the empty private marker `ColorConsoleDisabled`. Only `N.style()` and `N.wrap()` emit codes, and both check the private `stylingEnabled`; every named style and color goes through `style()`. `Style` values are the same in both modes, and `Style.wrap()` called directly never checks the flag. `NotApplied` is kept for API compatibility but the library no longer creates it.
- **Adding a style or color** means adding a constant in the `companion object` plus the five-member block (`val x: Style`, `val <N : Style> N.x`, `val <N> N.x`, `fun <N> N.x(predicate)`, `fun x(text: Any)`), copying an existing `region` block. Inside the block, refer to the `Style` as `this@ColoredConsole.x`: an unqualified `x` in the `N.x` members resolves to the `N.x` extension itself and recurses forever. Add the style to the list in `disabledStylesEmitNoCodes`.
- The top-level `print(colored, block)` / `println(colored, block)` overloads take a lambda that returns a `String` and are distinct from `kotlin.io.print(ln)`. Inside a `colored { }` block, `println("…")` is the normal `kotlin.io` one.
