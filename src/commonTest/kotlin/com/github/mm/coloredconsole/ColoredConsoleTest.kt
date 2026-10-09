package com.github.mm.coloredconsole

import kotlin.test.*

class TestANSI {

    @Test
    fun mainTest() {

        println { "Hello World".cyan.bold }

        colored {
            println("Hello World".cyan.bold)
        }

        println { "Hello World".cyan.bg }

        colored {
            // use Cyan as backgroud color
            println("Hello World".cyan.bg)
        }

        val pi = 22f / 7
        println { pi.blue.italic.underline }

        colored {
            // coloring/styling can by called on any object not just String
            val pi2 = 22f / 7
            println(pi2.blue.italic.underline)
        }

        val header1 = style { green + underline + bold }
        println { "Hello World"(header1) }
        // or
        println { "Hello World".style(header1) }

        colored {
            // custom style: characters + or . can be used to group styles
            val header2 = green + underline + bold
            println("Hello World"(header2))

            // or

            println("Hello World".style(header2))
        }

        colored {
            // custom style: characters + or . can be used to group styles
            val header = green.bright + underline + bold
            println("Hello World"(header))

            // or

            println("Hello World".style(header))
        }

        // prints all even numbers in Cyan color
        println { listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } } }

        colored {
            println(listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } })
        }

        // condition on style
        colored {
            val chapter = cyan + underline + bold
            val chapterNumber = 12
            println("$chapterNumber. Goodbye World"(chapter) { chapterNumber >= 10 })

            // or

            println("$chapterNumber. Goodbye World".style(chapter) { chapterNumber < 10 })
        }

        println(colored = true) { "Orange".yellow.bold + " Is the New " + "Black".bold.reverse }

        colored(enabled = true) {
            println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
        }

        println(colored = false) { "Orange".yellow.bold + " Is the New " + "Black".bold.reverse }

        colored(enabled = false) {
            println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
        }

        class Weather(val degrees: Int) : ColoredConsole {
            fun display() = println("Degrees:".blue.bold + " $degrees".italic.bold)
        }

        Weather(22).display()

        // nested
        colored {
            println(("bold " + ("italic " + ("color " + "Yellow".yellow.bold + " normal").faint + " italic").italic + " bold").bold)
        }

        // bright color
        colored {
            val style = blue.bright + bold
            println("bright blue"(style))

            // or

            println("bright blue".blue.bright.bold)
        }

        // background
        colored {
            println("cyan background".cyan.bg)
        }

        val header = style { blue + bold + underline }
        println { "Chapter 7."(header) }

    }

    @Test
    fun disabledStylesEmitNoCodes() {
        colored(enabled = false) {
            listOf(bold, faint, italic, underline, blink, reverse, hidden, strike,
                   black, red, green, yellow, blue, purple, magenta, cyan, white, gray,
                   rgb(1, 2, 3), color256(208), gradient(red, blue)).forEach {
                assertEquals("x", "x".style(it))
                assertEquals("x", "x"(it + bold))
                assertEquals("x", "x"(bold + it))
            }
        }

        colored {
            assertEquals("\u001B[2mx\u001B[0m", "x".style(faint))
            assertEquals("\u001B[9mx\u001B[0m", "x".style(strike))
        }
    }

    @Test
    fun disabledModeEmitsNoCodesInAnyForm() {
        colored(enabled = false) {
            assertEquals("x", "x".bold)
            assertEquals("x", "x".red.underline)
            assertEquals("x", "x".cyan { true })
            assertEquals("x", italic("x"))
            assertEquals("x", "x".wrap(1, 31))
            assertEquals("x", "x".red.bg.bright)
            assertEquals("x", "x".rgb(1, 2, 3))
            assertEquals("x", "x".color256(208).bg)
            assertEquals("x", "x".magenta { true })
            assertEquals("x", gray("x"))
            assertEquals("ab", "ab".gradient(red, blue))
            assertEquals("ab", "ab".gradient(rgb(1, 2, 3), cyan).bg)
        }
    }

    @Test
    fun wrapAppliesSeveralCodes() {
        colored {
            assertEquals("${esc(1)}${esc(31)}x${esc(0)}", "x".wrap(1, 31))
        }
    }

    @Test
    fun bgAndBrightChangeTheMostRecentColor() {
        colored {
            assertEquals("${esc(1)}${esc(46)}x${esc(0)}", "x".cyan.bold.bg)
            assertEquals("x".cyan.bg.bold, "x".cyan.bold.bg)
            assertEquals("${esc(106)}x${esc(0)}", "x".cyan.bg.bright)
            assertEquals("x".cyan.bright.bg, "x".cyan.bg.bright)
            assertEquals("x".bold, "x".bold.bg)
            assertEquals("x".bold, "x".bold.bright)

            assertEquals("x"(green.bright + underline), "x"((green + underline).bright))
            assertEquals("x"(cyan.bright.bg + bold), "x"((cyan + bold).bg.bright))
            // The most recent color is already a background, so blue stays a text color.
            assertEquals("x"(blue + red.bg), "x"((blue + red.bg).bg))
            assertEquals("x"(bold), "x"(bold.bg))
        }
    }

    @Test
    fun bgChangesEverySegmentOfNestedText() {
        colored {
            assertEquals("${esc(46)}a${esc(31)}b${esc(0)}${esc(46)}c${esc(0)}", ("a" + "b".red + "c").cyan.bg)
        }
    }

    @Test
    fun plainFunctionsAcceptAnyValue() {
        colored {
            assertEquals("${esc(3)}3.14${esc(0)}", italic(3.14))
            assertEquals("${esc(31)}42${esc(0)}", red(42))
        }
    }
    @Test
    fun extendedColors() {
        colored {
            assertEquals("${esc("38;2;255;135;0")}x${esc(0)}", "x".rgb(255, 135, 0))
            assertEquals("${esc("38;5;208")}x${esc(0)}", "x".color256(208))
            assertEquals("${esc(1)}${esc("38;2;1;2;3")}x${esc(0)}", "x"(rgb(1, 2, 3) + bold))
            assertEquals("x"(bold + rgb(1, 2, 3)), "x"(bold.rgb(1, 2, 3)))
            assertEquals("x"(bold + color256(9)), "x"(bold.color256(9)))

            assertEquals("${esc("48;5;208")}x${esc(0)}", "x".color256(208).bg)
            assertEquals("${esc(1)}${esc("48;2;1;2;3")}x${esc(0)}", "x".rgb(1, 2, 3).bold.bg)
            assertEquals("x"(color256(208).bg), "x".color256(208).bg)
            // An extended color is the most recent color: .bg changes it and leaves red as the text color,
            // and .bright stops at it (there is no bright variant) instead of brightening red.
            assertEquals("${esc("48;5;208")}${esc(31)}x${esc(0)}", "x".red.color256(208).bg)
            assertEquals("x".red.color256(208), "x".red.color256(208).bright)
            assertEquals("x"(red + color256(208)), "x"((red + color256(208)).bright))
        }
        assertFailsWith<IllegalArgumentException> { colored { rgb(256, 0, 0) } }
        assertFailsWith<IllegalArgumentException> { colored { "x".color256(-1) } }
    }

    @Test
    fun magentaAndGray() {
        colored {
            assertEquals("${esc(35)}x${esc(0)}", "x".magenta)
            assertEquals("x".purple, "x".magenta)
            assertEquals("x".magenta, "x".magenta { true })
            assertEquals("x".magenta, magenta("x"))
            assertEquals("x".magenta, "x"(magenta))
            assertEquals("x".magenta.bold, "x"(magenta.bold))
            assertEquals("${esc(95)}x${esc(0)}", "x".magenta.bright)

            assertEquals("${esc(90)}x${esc(0)}", "x".gray)
            assertEquals("x".gray, "x".gray { true })
            assertEquals("x".gray, gray("x"))
            assertEquals("x".gray, "x"(gray))
            assertEquals("x".gray.bold, "x"(gray.bold))
            assertEquals("x".gray, "x".gray.bright)
            assertEquals("${esc(100)}x${esc(0)}", "x".gray.bg)
        }
    }

    @Test
    fun stripAnsiAndVisibleLength() {
        val message = colored { "Error:".red.bold + " disk " + "full".rgb(255, 135, 0).bg }
        assertEquals("Error: disk full", message.stripAnsi())
        assertEquals(16, message.visibleLength)
        assertEquals("ab", "a\u001B[2Kb".stripAnsi())
        assertEquals("plain", "plain".stripAnsi())
        assertEquals(5, "plain".visibleLength)
    }
    @Test
    fun gradientColorsEveryCharacter() {
        val from = style { rgb(255, 0, 0) }
        val to = style { rgb(0, 0, 255) }
        colored {
            // every visible character is its own segment, so .bg and outer styles reach all of them
            assertEquals("${esc("38;2;255;0;0")}a${esc(0)}${esc("38;2;128;0;128")}b${esc(0)}${esc("38;2;0;0;255")}c${esc(0)}",
                    "abc".gradient(from, to))
            assertEquals("${esc("38;2;255;0;0")}a${esc(0)}", "a".gradient(from, to))
            assertEquals("", "".gradient(from, to))

            // named colors use the standard xterm values; color256 uses the xterm palette
            assertEquals("${esc("38;2;205;0;0")}a${esc(0)}${esc("38;2;0;0;238")}b${esc(0)}", "ab".gradient(red, blue))
            assertEquals("${esc("38;2;255;0;0")}a${esc(0)}", "a".gradient(color256(196), gray))

            // inner styles are re-applied after the gradient code, so they still win
            assertEquals("${esc("38;2;255;0;0")}a${esc(0)}${esc("38;2;0;0;255")}${esc(1)}b${esc(0)}",
                    ("a" + "b".bold).gradient(from, to))
            assertEquals("${esc("38;2;255;0;0")}a${esc(0)}${esc("38;2;0;0;255")}${esc(32)}b${esc(0)}",
                    ("a" + "b".green).gradient(from, to))

            // a surrogate pair (emoji) stays one character
            assertEquals("${esc("38;2;255;0;0")}\uD83D\uDE00${esc(0)}${esc("38;2;0;0;255")}x${esc(0)}",
                    "\uD83D\uDE00x".gradient(from, to))

            val text = ("Hello " + "World".bold).gradient(from, to)
            assertEquals("Hello World", text.stripAnsi())
            assertEquals(11, text.visibleLength)
        }
    }

    @Test
    fun gradientWorksLikeAColor() {
        val from = style { rgb(255, 0, 0) }
        val to = style { rgb(0, 0, 255) }
        colored {
            assertEquals("${esc("48;2;255;0;0")}a${esc(0)}${esc("48;2;0;0;255")}b${esc(0)}", "ab".gradient(from, to).bg)
            assertEquals("ab".gradient(from, to).bg, "ab"(gradient(from, to).bg))
            assertEquals("ab".gradient(from, to).bold, "ab"(gradient(from, to) + bold))
            assertEquals("ab"(bold + gradient(from, to)), "ab"(bold.gradient(from, to)))
            // a gradient is the most recent color: .bright stops at it instead of brightening red
            assertEquals("ab"(red + gradient(from, to)), "ab"((red + gradient(from, to)).bright))
            assertEquals("ab".red.gradient(from, to), "ab".red.gradient(from, to).bright)
        }
        assertFailsWith<IllegalArgumentException> { colored { gradient(bold, red) } }
        assertFailsWith<IllegalArgumentException> { colored { gradient(red, blue.bg) } }
        assertFailsWith<IllegalArgumentException> { colored { gradient(red + bold, blue) } }
    }
}

private fun esc(code: Int) = "\u001B[${code}m"
private fun esc(codes: String) = "\u001B[${codes}m"