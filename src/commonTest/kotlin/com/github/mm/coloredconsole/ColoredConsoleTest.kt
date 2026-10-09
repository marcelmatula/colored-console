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
                   black, red, green, yellow, blue, purple, cyan, white).forEach {
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
}

private fun esc(code: Int) = "\u001B[${code}m"