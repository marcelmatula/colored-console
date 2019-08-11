package com.github.mm.coloredconsole


fun main() {

    colored {
        println("Hello World".cyan.bold)
    }

    colored {
        // use Cyan as backgroud color
        println("Hello World".cyan.bg)
    }

    colored {
        // coloring/styling can by called on any object not just String
        val pi = 22f/7
        println(pi.blue.italic.underline)
    }

    colored {
        // custom style: characters + or . can be used to group styles
        val header = green + underline + bold
        println("Hello World"(header))

        // or

        println("Hello World".style(header))
    }

    // prints all even numbers in Cyan color
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
    colored(enabled = true) {
        println(("bold " + ("italic " + ("color " + "Yellow".yellow.bold + " normal").faint + " italic").italic + " bold").bold)
    }

}