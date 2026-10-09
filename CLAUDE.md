# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A tiny Kotlin DSL for ANSI-colored console output (Kotlin Multiplatform, only a `jvm()` target configured). The whole library is one file, `src/commonMain/kotlin/com/github/mm/coloredconsole/ColoredConsole.kt`, and the README tells users to **copy that single file into their project**. Keep it self-contained: no dependencies, no extra source files, and no `java.*` / platform APIs (it lives in `commonMain`).

## Build and test

Gradle 9.8 wrapper, Kotlin 2.4 (version in `gradle/libs.versions.toml`), and `jvmToolchain(25)`: compiles to Java 25 bytecode, and the foojay resolver in `settings.gradle.kts` downloads JDK 25 if it isn't installed. The configuration cache is on (`gradle.properties`).

```sh
./gradlew check          # compile + all tests
./gradlew jvmTest        # tests only

# single test: the class is TestANSI (the file is ColoredConsoleTest.kt)
./gradlew jvmTest --tests 'com.github.mm.coloredconsole.TestANSI.mainTest'

# also show the raw colored output the test prints (-i), even if the task is up to date (--rerun)
./gradlew jvmTest --rerun -i
```

`TestANSI.mainTest` has **no assertions**. It prints the README examples, so it only catches compile errors and exceptions; apart from `disabledStylesEmitNoCodes`, the escape sequences have to be checked by eye (`-i` output, or `<system-out>` in `build/test-results/jvmTest/*.xml`, where ESC shows as `?`). The test and the README examples mirror each other; keep them in sync when the API changes.

## Architecture (`ColoredConsole.kt`)

Everything is a member of the `ColoredConsole` interface, so it is only in scope inside a `ColoredConsole` receiver: `colored { }`, `style { }`, the top-level `print { }` / `println { }` lambda overloads, or a class that implements `ColoredConsole`.

Two parallel APIs produce the escape codes:

1. **Extension properties/functions on any value `N`** (`"x".red.bold`, `x.cyan { predicate }`, `bold(text)`) call `N.wrap(code)` and return a `String` immediately.
2. **The `Style` sealed class** (`Simple(code)`, `Composite(parent, child)`, `NotApplied`) is built with `red`, `green + bold`, or `style { … }`, and applied with `"x".style(s)` or `"x"(s)`. `a + b` builds `Composite(parent = b, child = a)`, so the right operand wraps outermost.

Non-obvious behavior:

- **Nesting** works because `applyCodes` splits the text on the reset sequence `ESC[0m` and re-applies the new codes to every segment, so an outer style resumes after an inner one ends.
- **`.bg` and `.bright` only take effect directly after a color.** On a `String` they rewrite only the *first* escape code (the most recently applied one), and they assume it has two digits: `"x".cyan.bg.bold` works, `"x".cyan.bold.bg` silently does nothing. On a `Composite` they only change `parent`: `green.bright + underline` works, `(green + underline).bright` does nothing.
- **Disabled mode** (`colored(enabled = false)`, `println(colored = false) { }`) uses the private `ColorConsoleDisabled` interface. It works in two ways: `wrap()` checks `this is ColorConsoleDisabled` and returns plain text, and `ColorConsoleDisabled` overrides every `Style`-returning member to return `NotApplied`, which also absorbs anything added to it (`NotApplied + x == NotApplied`). `Style.wrap` itself never checks for disabled mode, so a style missing its override there still emits codes when coloring is disabled (`TestANSI.disabledStylesEmitNoCodes` lists every style and catches this).
- **Adding a style or color** means adding a constant in the `companion object` plus the five-member block (`val x: Style`, `val <N : Style> N.x`, `val <N> N.x`, `fun <N> N.x(predicate)`, `fun x(text)`), following the existing `region` sections, both `Style` overrides in `ColorConsoleDisabled`, and an entry in the style list of `disabledStylesEmitNoCodes`.
- The top-level `print(colored, block)` / `println(colored, block)` overloads take a lambda that returns a `String` and are distinct from `kotlin.io.print(ln)`. Inside a `colored { }` block, `println("…")` is the normal `kotlin.io` one.
