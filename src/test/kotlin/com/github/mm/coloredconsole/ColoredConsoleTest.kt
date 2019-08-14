package com.github.mm.coloredconsole


fun main() {

    println { "Hello World".cyan.bold }

    colored {
        println("Hello World".cyan.bold)
    }

    println { "Hello World".cyan.bg }

    colored {
        // use Cyan as backgroud color
        println("Hello World".cyan.bg)
    }

    val pi = 22f/7
    println { pi.blue.italic.underline }

    colored {
        // coloring/styling can by called on any object not just String
        val pi = 22f/7
        println(pi.blue.italic.underline)
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
    println { listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } }}

    colored {
        println(listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } })
    }

    // condition on style
    colored {
        val chapter = cyan + underline + bold
        val chapterNumber = 12
        println("$chapterNumber. Goodbye World"(chapter) { chapterNumber >= 10 })

        // or

        println("$chapterNumber. Goodbye World".style(chapter) { chapterNumber >= 10 })
    }

    colored(enabled = true) {
        println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
    }

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