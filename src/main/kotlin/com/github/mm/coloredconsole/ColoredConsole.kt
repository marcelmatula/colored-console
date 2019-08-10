package com.github.mm.coloredconsole

import com.github.mm.coloredconsole.ColoredConsole.Style
import com.github.mm.coloredconsole.ColoredConsole.Style.NotApplied
import java.util.regex.Pattern

interface ColoredConsole {

    sealed class Style {

        val bg: Style get() = when (this){
            is NotApplied -> this
            is Background -> this
            is Simple -> if (code.isColor) copy(code = code + 10) else this
            is Composite ->  if (parent is Simple && parent.code.isColor) copy(parent = parent.copy(code = parent.code + 10)) else this
        }

        abstract fun wrap(text: String): String

        object NotApplied : Style() {
            override fun wrap(text: String) = text
        }

        class Background(private val style: Style) : Style() {
            override fun wrap(text: String) = when (style) {
                is Simple -> (if (style.code.isColor) style.copy(code = style.code + 10) else style).wrap(text)
                else -> style.wrap(text)
            }
        }

        data class Simple(val code: Int) : Style() {
            override fun wrap(text: String) = "\u001B[${code}m$text".reset(code)
        }

        data class Composite(val parent: Style, private val child: Style) : Style() {
            override fun wrap(text: String) = parent.wrap(child.wrap(text))
        }

        operator fun plus(style: Style) = when (this) {
            is NotApplied -> this
            is Background -> when (style) {
                is Background -> this
                else -> Composite(style, this)
            }
            is Simple -> when (style) {
                is Background -> if (code.isColor) copy(code = code + 10) else this
                else -> Composite(style, this)
            }
            is Composite -> when (style) {
                is Background -> if (parent is Simple && parent.code.isColor) copy(parent = parent.copy(code = parent.code + 10)) else this
                else -> Composite(style, this)
            }
        }
    }

    fun <N> N.style(style: Style, predicate: (N) -> Boolean = { true }) =
            takeIf { predicate(this) }?.let { style.wrap(this.toString()) } ?: this.toString()

    operator fun <N> N.invoke(style: Style, predicate: (N) -> Boolean = { true }) = style(style, predicate)

    fun <N> N.wrap(vararg ansiCodes: Int) = this.toString().let { text ->
        if (this@ColoredConsole is ColorConsoleDisabled)
            text
        else {
            val codes = ansiCodes.filter { it != RESET }
            text.reset(*codes.toIntArray())
        }
    }

    val String.bg
        get() = pattern.matcher(this).let { matcher ->
            if (matcher.find() && matcher.start() == 0) {
                val code = matcher.group(1).toIntOrNull() ?: 0
                if (code.isColor) {
                    return@let substring(0, 2) + (code + 10) + substring(4)
                }
            }
            this
        }

    //region style
    val bold: Style get() = Style.Simple(HIGH_INTENSITY)
    val <N : Style> N.bold: Style get() = this + this@ColoredConsole.bold
    val <N> N.bold get() = wrap(HIGH_INTENSITY)
    fun <N> N.bold(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.bold?: this.toString()
    fun bold(text: Any) = text.wrap(HIGH_INTENSITY)

    val faint: Style get() = Style.Simple(LOW_INTENSITY)
    val <N : Style> N.faint: Style get() = this + this@ColoredConsole.faint
    val <N> N.faint get() = wrap(LOW_INTENSITY)
    fun <N> N.faint(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.faint?: this.toString()
    fun faint(text: Any) = text.wrap(LOW_INTENSITY)

    val italic: Style get() = Style.Simple(ITALIC)
    val <N : Style> N.italic: Style get() = this + this@ColoredConsole.italic
    val <N> N.italic get() = wrap(ITALIC)
    fun <N> N.italic(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.italic?: this.toString()
    fun italic(text: String) = text.wrap(ITALIC)

    val underline: Style get() = Style.Simple(UNDERLINE)
    val <N : Style> N.underline: Style get() = this + this@ColoredConsole.underline
    val <N> N.underline get() = wrap(UNDERLINE)
    fun <N> N.underline(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.underline?: this.toString()
    fun underline(text: String) = text.wrap(UNDERLINE)

    val blink: Style get() = Style.Simple(BLINK)
    val <N : Style> N.blink: Style get() = this + this@ColoredConsole.blink
    val <N> N.blink get() = wrap(BLINK)
    fun <N> N.blink(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.blink?: this.toString()
    fun blink(text: String) = text.wrap(BLINK)

    val reverse: Style get() = Style.Simple(REVERSE)
    val <N : Style> N.reverse: Style get() = this + this@ColoredConsole.reverse
    val <N> N.reverse get() = wrap(REVERSE)
    fun <N> N.reverse(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.reverse?: this.toString()
    fun reverse(text: String) = text.wrap(REVERSE)

    val hidden: Style get() = Style.Simple(HIDDEN)
    val <N : Style> N.hidden: Style get() = this + this@ColoredConsole.hidden
    val <N> N.hidden get() = wrap(HIDDEN)
    fun <N> N.hidden(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.hidden?: this.toString()
    fun hidden(text: String) = text.wrap(HIDDEN)

    val strike: Style get() = Style.Simple(STRIKE)
    val <N : Style> N.strike: Style get() = this + this@ColoredConsole.strike
    val <N> N.strike get() = wrap(STRIKE)
    fun <N> N.strike(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.strike?: this.toString()
    fun strike(text: String) = text.wrap(STRIKE)
    //endregions

    // region colors
    val black: Style get() = Style.Simple(BLACK)
    val <N : Style> N.black: Style get() = this + this@ColoredConsole.black
    val <N> N.black get() = wrap(BLACK)
    fun <N> N.black(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.black?: this.toString()
    fun black(text: String) = text.wrap(BLACK)

    val red: Style get() = Style.Simple(RED)
    val <N : Style> N.red: Style get() = this + this@ColoredConsole.red
    val <N> N.red get() = wrap(RED)
    fun <N> N.red(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.red?: this.toString()
    fun red(text: String) = text.wrap(RED)

    val green: Style get() = Style.Simple(GREEN)
    val <N : Style> N.green: Style get() = this + this@ColoredConsole.green
    val <N> N.green get() = wrap(GREEN)
    fun <N> N.green(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.green?: this.toString()
    fun green(text: String) = text.wrap(GREEN)

    val yellow: Style get() = Style.Simple(YELLOW)
    val <N : Style> N.yellow: Style get() = this + this@ColoredConsole.yellow
    val <N> N.yellow get() = wrap(YELLOW)
    fun <N> N.yellow(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.yellow?: this.toString()
    fun yellow(text: String) = text.wrap(YELLOW)

    val blue: Style get() = Style.Simple(BLUE)
    val <N : Style> N.blue: Style get() = this + this@ColoredConsole.blue
    val <N> N.blue get() = wrap(BLUE)
    fun <N> N.blue(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.blue?: this.toString()
    fun blue(text: String) = text.wrap(BLUE)

    val purple: Style get() = Style.Simple(PURPLE)
    val <N : Style> N.purple: Style get() = this + this@ColoredConsole.purple
    val <N> N.purple get() = wrap(PURPLE)
    fun <N> N.purple(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.purple?: this.toString()
    fun purple(text: String) = text.wrap(PURPLE)

    val cyan: Style get() = Style.Simple(CYAN)
    val <N : Style> N.cyan: Style get() = this + this@ColoredConsole.cyan
    val <N> N.cyan get() = wrap(CYAN)
    fun <N> N.cyan(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.cyan?: this.toString()
    fun cyan(text: String) = text.wrap(CYAN)

    val white: Style get() = Style.Simple(WHITE)
    val <N : Style> N.white: Style get() = this + this@ColoredConsole.white
    val <N> N.white get() = wrap(WHITE)
    fun <N> N.white(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.white?: this.toString()
    fun white(text: String) = text.wrap(WHITE)
    // endregion

    // region bright colors
    val brightBlack: Style get() = Style.Simple(BRIGHT_BLACK)
    val <N : Style> N.brightBlack: Style get() = this + this@ColoredConsole.brightBlack
    val <N> N.brightBlack get() = wrap(BRIGHT_BLACK)
    fun <N> N.brightBlack(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightBlack?: this.toString()
    fun brightBlack(text: String) = text.wrap(BRIGHT_BLACK)

    val brightRed: Style get() = Style.Simple(BRIGHT_RED)
    val <N : Style> N.brightRed: Style get() = this + this@ColoredConsole.brightRed
    val <N> N.brightRed get() = wrap(BRIGHT_RED)
    fun <N> N.brightRed(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightRed?: this.toString()
    fun brightRed(text: String) = text.wrap(BRIGHT_RED)

    val brightGreen: Style get() = Style.Simple(BRIGHT_GREEN)
    val <N : Style> N.brightGreen: Style get() = this + this@ColoredConsole.brightGreen
    val <N> N.brightGreen get() = wrap(BRIGHT_GREEN)
    fun <N> N.brightGreen(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightGreen?: this.toString()
    fun brightGreen(text: String) = text.wrap(BRIGHT_GREEN)

    val brightYellow: Style get() = Style.Simple(BRIGHT_YELLOW)
    val <N : Style> N.brightYellow: Style get() = this + this@ColoredConsole.brightYellow
    val <N> N.brightYellow get() = wrap(BRIGHT_YELLOW)
    fun <N> N.brightYellow(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightYellow?: this.toString()
    fun brightYellow(text: String) = text.wrap(BRIGHT_YELLOW)

    val brightBlue: Style get() = Style.Simple(BRIGHT_BLUE)
    val <N : Style> N.brightBlue: Style get() = this + this@ColoredConsole.brightBlue
    val <N> N.brightBlue get() = wrap(BRIGHT_BLUE)
    fun <N> N.brightBlue(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightBlue?: this.toString()
    fun brightBlue(text: String) = text.wrap(BRIGHT_BLUE)

    val brightPurple: Style get() = Style.Simple(BRIGHT_PURPLE)
    val <N : Style> N.brightPurple: Style get() = this + this@ColoredConsole.brightPurple
    val <N> N.brightPurple get() = wrap(BRIGHT_PURPLE)
    fun <N> N.brightPurple(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightPurple?: this.toString()
    fun brightPurple(text: String) = text.wrap(BRIGHT_PURPLE)

    val brightCyan: Style get() = Style.Simple(BRIGHT_CYAN)
    val <N : Style> N.brightCyan: Style get() = this + this@ColoredConsole.brightCyan
    val <N> N.brightCyan get() = wrap(BRIGHT_CYAN)
    fun <N> N.brightCyan(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightCyan?: this.toString()
    fun brightCyan(text: String) = text.wrap(BRIGHT_CYAN)

    val brightWhite: Style get() = Style.Simple(BRIGHT_WHITE)
    val <N : Style> N.brightWhite: Style get() = this + this@ColoredConsole.brightWhite
    val <N> N.brightWhite get() = wrap(BRIGHT_WHITE)
    fun <N> N.brightWhite(predicate: (N) -> Boolean = { true }) = takeIf { predicate(this) }?.toString()?.brightWhite?: this.toString()
    fun brightWhite(text: String) = text.wrap(BRIGHT_WHITE)
    // endregion

    companion object {
        const val RESET = 0

        const val HIGH_INTENSITY = 1
        const val LOW_INTENSITY = 2

        const val ITALIC = 3
        const val UNDERLINE = 4
        const val BLINK = 5
        const val REVERSE = 7
        const val HIDDEN = 8
        const val STRIKE = 9

        const val BLACK = 30
        const val RED = 31
        const val GREEN = 32
        const val YELLOW = 33
        const val BLUE = 34
        const val PURPLE = 35
        const val CYAN = 36
        const val WHITE = 37

        const val BRIGHT_BLACK = 90
        const val BRIGHT_RED = 91
        const val BRIGHT_GREEN = 92
        const val BRIGHT_YELLOW = 93
        const val BRIGHT_BLUE = 94
        const val BRIGHT_PURPLE = 95
        const val BRIGHT_CYAN = 96
        const val BRIGHT_WHITE = 97

        val pattern: Pattern = Pattern.compile("\\u001B\\[([0-9]{1,2})m")

    }
}

interface ColorConsoleDisabled : ColoredConsole {

    override val bold get() = NotApplied
    override val <N : Style> N.bold: Style get() = NotApplied
    override val italic get() = NotApplied
    override val <N : Style> N.italic: Style get() = NotApplied
    override val underline get() = NotApplied
    override val <N : Style> N.underline: Style get() = NotApplied
    override val blink get() = NotApplied
    override val <N : Style> N.blink: Style get() = NotApplied
    override val reverse get() = NotApplied
    override val <N : Style> N.reverse: Style get() = NotApplied
    override val hidden get() = NotApplied
    override val <N : Style> N.hidden: Style get() = NotApplied

    override val red get() = NotApplied
    override val <N : Style> N.red: Style get() = NotApplied
    override val black get() = NotApplied
    override val <N : Style> N.black: Style get() = NotApplied
    override val green get() = NotApplied
    override val <N : Style> N.green: Style get() = NotApplied
    override val yellow get() = NotApplied
    override val <N : Style> N.yellow: Style get() = NotApplied
    override val blue get() = NotApplied
    override val <N : Style> N.blue: Style get() = NotApplied
    override val purple get() = NotApplied
    override val <N : Style> N.purple: Style get() = NotApplied
    override val cyan get() = NotApplied
    override val <N : Style> N.cyan: Style get() = NotApplied
    override val white get() = NotApplied
    override val <N : Style> N.white: Style get() = NotApplied

    override val brightBlack get() = NotApplied
    override val <N : Style> N.brightBlack: Style get() = NotApplied
    override val brightRed get() = NotApplied
    override val <N : Style> N.brightRed: Style get() = NotApplied
    override val brightGreen get() = NotApplied
    override val <N : Style> N.brightGreen: Style get() = NotApplied
    override val brightYellow get() = NotApplied
    override val <N : Style> N.brightYellow: Style get() = NotApplied
    override val brightBlue get() = NotApplied
    override val <N : Style> N.brightBlue: Style get() = NotApplied
    override val brightPurple get() = NotApplied
    override val <N : Style> N.brightPurple: Style get() = NotApplied
    override val brightCyan get() = NotApplied
    override val <N : Style> N.brightCyan: Style get() = NotApplied
    override val brightWhite get() = NotApplied
    override val <N : Style> N.brightWhite: Style get() = NotApplied
}

private val Int.isColor get() = this in 30..37 || this in 90..97

private fun String.reset(vararg codes: Int) = "\u001B[${ColoredConsole.RESET}m".let { reset ->
    val tags = codes.joinToString { "\u001B[${it}m" }
    split(reset).filter { it.isNotEmpty() }.joinToString(separator = "") { tags + it + reset }
}

fun <R> colored(enabled: Boolean = true, block: ColoredConsole.() -> R): R =
        if (enabled) object : ColoredConsole {}.block() else object : ColorConsoleDisabled {}.block()

fun main() {

    colored(true) {

        val chapter = bold + cyan + underline
        val name = "John"
        val text = name(chapter) { it.length.rem(2) == 0 }

        println(text)
        val style1 = green + underline + bold
        val style2 = green.underline.bold
        println(true(style1) { it })
        println(true(style2) { it })
        println(true.green.underline.bold)

        val a = true(style1)
        val b = true(style2)
        val c = true.style(style2)
        val d = true.green.underline.bold

        val myStyle = italic + faint
        val e = ("start " + ("ahoj".red + " haha")(myStyle) + " end".brightBlue + " konec").bold
        val f = ("start " + ("ahoj".red + " haha")(myStyle) + " end".brightBlue.faint + " konec").bold.yellow

        println(e)
        println(f)

        println("ahoj".blue.bold.red.bg.reverse)
        println("ahoj".blue.bold.red.bg)

        val header = blue.bg + bold + brightRed
        println("Ahoj"(header))
        println("ahoj"(black.bg.bold.brightRed))
        println("ahoj"(black.bg.bold.red))
        println(("aaaa " + "ahoj"(black.bg.bold.red) + " kkkk".strike).cyan.bold)

    }

    //A().run()
}
