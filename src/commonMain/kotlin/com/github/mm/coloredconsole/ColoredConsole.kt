/*
 * Colored Console - https://github.com/marcelmatula/colored-console
 *
 * Copyright (c) 2019-2026 Marcel Matula
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.mm.coloredconsole

import com.github.mm.coloredconsole.ColoredConsole.Companion.BACKGROUND_SHIFT
import com.github.mm.coloredconsole.ColoredConsole.Companion.BLACK
import com.github.mm.coloredconsole.ColoredConsole.Companion.BRIGHT_BLACK
import com.github.mm.coloredconsole.ColoredConsole.Companion.BRIGHT_SHIFT
import com.github.mm.coloredconsole.ColoredConsole.Companion.BRIGHT_WHITE
import com.github.mm.coloredconsole.ColoredConsole.Companion.RESET
import com.github.mm.coloredconsole.ColoredConsole.Companion.WHITE
import com.github.mm.coloredconsole.ColoredConsole.Style

interface ColoredConsole {

    sealed class Style {

        // bg and bright change the most recently added color, wherever it is in the style
        val bg: Style get() = changeLatestColor(Int::toBackground) ?: this

        val bright: Style get() = changeLatestColor(Int::toBright) ?: this

        // A Composite's parent was added after its child, so it is searched first.
        private fun changeLatestColor(change: (Int) -> Int): Style? = when (this) {
            is NotApplied -> null
            is Simple -> if (code.isColor) Simple(change(code)) else null
            is Composite -> parent.changeLatestColor(change)?.let { copy(parent = it) }
                    ?: child.changeLatestColor(change)?.let { copy(child = it) }
        }

        abstract fun wrap(text: String): String

        object NotApplied : Style() {
            override fun wrap(text: String) = text
        }

        data class Simple(val code: Int) : Style() {
            override fun wrap(text: String) = text.applyCodes(code)
        }

        data class Composite(val parent: Style, val child: Style) : Style() {
            override fun wrap(text: String) = parent.wrap(child.wrap(text))
        }

        operator fun plus(style: Style) = when (this) {
            is NotApplied -> this
            is Simple -> Composite(style, this)
            is Composite -> Composite(style, this)
        }
    }

    // Every named style and color goes through style() or wrap(), the only two places that emit codes.
    fun <N> N.style(style: Style, predicate: (N) -> Boolean = { true }) =
            if (predicate(this) && stylingEnabled) style.wrap(toString()) else toString()

    operator fun <N> N.invoke(style: Style, predicate: (N) -> Boolean = { true }) = style(style, predicate)

    fun <N> N.wrap(vararg ansiCodes: Int) =
            if (stylingEnabled) toString().applyCodes(*ansiCodes.filter { it != RESET }.toIntArray()) else toString()

    private val stylingEnabled get() = this !is ColorConsoleDisabled

    val String.bright get() = changeLatestColor(Int::toBright)

    val String.bg get() = changeLatestColor(Int::toBackground)

    // region styles
    val bold: Style get() = Style.Simple(HIGH_INTENSITY)
    val <N : Style> N.bold: Style get() = this + this@ColoredConsole.bold
    val <N> N.bold get() = style(this@ColoredConsole.bold)
    fun <N> N.bold(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.bold, predicate)
    fun bold(text: Any) = text.style(bold)

    val faint: Style get() = Style.Simple(LOW_INTENSITY)
    val <N : Style> N.faint: Style get() = this + this@ColoredConsole.faint
    val <N> N.faint get() = style(this@ColoredConsole.faint)
    fun <N> N.faint(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.faint, predicate)
    fun faint(text: Any) = text.style(faint)

    val italic: Style get() = Style.Simple(ITALIC)
    val <N : Style> N.italic: Style get() = this + this@ColoredConsole.italic
    val <N> N.italic get() = style(this@ColoredConsole.italic)
    fun <N> N.italic(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.italic, predicate)
    fun italic(text: Any) = text.style(italic)

    val underline: Style get() = Style.Simple(UNDERLINE)
    val <N : Style> N.underline: Style get() = this + this@ColoredConsole.underline
    val <N> N.underline get() = style(this@ColoredConsole.underline)
    fun <N> N.underline(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.underline, predicate)
    fun underline(text: Any) = text.style(underline)

    val blink: Style get() = Style.Simple(BLINK)
    val <N : Style> N.blink: Style get() = this + this@ColoredConsole.blink
    val <N> N.blink get() = style(this@ColoredConsole.blink)
    fun <N> N.blink(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.blink, predicate)
    fun blink(text: Any) = text.style(blink)

    val reverse: Style get() = Style.Simple(REVERSE)
    val <N : Style> N.reverse: Style get() = this + this@ColoredConsole.reverse
    val <N> N.reverse get() = style(this@ColoredConsole.reverse)
    fun <N> N.reverse(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.reverse, predicate)
    fun reverse(text: Any) = text.style(reverse)

    val hidden: Style get() = Style.Simple(HIDDEN)
    val <N : Style> N.hidden: Style get() = this + this@ColoredConsole.hidden
    val <N> N.hidden get() = style(this@ColoredConsole.hidden)
    fun <N> N.hidden(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.hidden, predicate)
    fun hidden(text: Any) = text.style(hidden)

    val strike: Style get() = Style.Simple(STRIKE)
    val <N : Style> N.strike: Style get() = this + this@ColoredConsole.strike
    val <N> N.strike get() = style(this@ColoredConsole.strike)
    fun <N> N.strike(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.strike, predicate)
    fun strike(text: Any) = text.style(strike)
    // endregion

    // region colors
    val black: Style get() = Style.Simple(BLACK)
    val <N : Style> N.black: Style get() = this + this@ColoredConsole.black
    val <N> N.black get() = style(this@ColoredConsole.black)
    fun <N> N.black(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.black, predicate)
    fun black(text: Any) = text.style(black)

    val red: Style get() = Style.Simple(RED)
    val <N : Style> N.red: Style get() = this + this@ColoredConsole.red
    val <N> N.red get() = style(this@ColoredConsole.red)
    fun <N> N.red(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.red, predicate)
    fun red(text: Any) = text.style(red)

    val green: Style get() = Style.Simple(GREEN)
    val <N : Style> N.green: Style get() = this + this@ColoredConsole.green
    val <N> N.green get() = style(this@ColoredConsole.green)
    fun <N> N.green(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.green, predicate)
    fun green(text: Any) = text.style(green)

    val yellow: Style get() = Style.Simple(YELLOW)
    val <N : Style> N.yellow: Style get() = this + this@ColoredConsole.yellow
    val <N> N.yellow get() = style(this@ColoredConsole.yellow)
    fun <N> N.yellow(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.yellow, predicate)
    fun yellow(text: Any) = text.style(yellow)

    val blue: Style get() = Style.Simple(BLUE)
    val <N : Style> N.blue: Style get() = this + this@ColoredConsole.blue
    val <N> N.blue get() = style(this@ColoredConsole.blue)
    fun <N> N.blue(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.blue, predicate)
    fun blue(text: Any) = text.style(blue)

    val purple: Style get() = Style.Simple(PURPLE)
    val <N : Style> N.purple: Style get() = this + this@ColoredConsole.purple
    val <N> N.purple get() = style(this@ColoredConsole.purple)
    fun <N> N.purple(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.purple, predicate)
    fun purple(text: Any) = text.style(purple)

    val cyan: Style get() = Style.Simple(CYAN)
    val <N : Style> N.cyan: Style get() = this + this@ColoredConsole.cyan
    val <N> N.cyan get() = style(this@ColoredConsole.cyan)
    fun <N> N.cyan(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.cyan, predicate)
    fun cyan(text: Any) = text.style(cyan)

    val white: Style get() = Style.Simple(WHITE)
    val <N : Style> N.white: Style get() = this + this@ColoredConsole.white
    val <N> N.white get() = style(this@ColoredConsole.white)
    fun <N> N.white(predicate: (N) -> Boolean = { true }) = style(this@ColoredConsole.white, predicate)
    fun white(text: Any) = text.style(white)
    // endregion

    companion object {
        const val RESET = 0

        const val HIGH_INTENSITY = 1
        const val LOW_INTENSITY = 2

        const val BACKGROUND_SHIFT = 10
        const val BRIGHT_SHIFT = 60

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

        const val BRIGHT_BLACK = BLACK + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_RED = RED + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_GREEN = GREEN + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_YELLOW = YELLOW + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_BLUE = BLUE + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_PURPLE = PURPLE + BRIGHT_SHIFT

        @Suppress("unused")
        const val BRIGHT_CYAN = CYAN + BRIGHT_SHIFT

        const val BRIGHT_WHITE = WHITE + BRIGHT_SHIFT

        val reEscape = Regex("\\u001B\\[([0-9]{1,2})m")
    }
}

// Marks the receiver of colored(enabled = false); style() and wrap() emit no codes for it.
private interface ColorConsoleDisabled : ColoredConsole

private val Int.isForegroundColor get() = this in BLACK..WHITE || this in BRIGHT_BLACK..BRIGHT_WHITE
private val Int.isColor get() = isForegroundColor || (this - BACKGROUND_SHIFT).isForegroundColor
private fun Int.toBackground() = if (isForegroundColor) this + BACKGROUND_SHIFT else this
private fun Int.toBright() =
        if (this in BLACK..WHITE || this in BLACK + BACKGROUND_SHIFT..WHITE + BACKGROUND_SHIFT) this + BRIGHT_SHIFT else this

private fun ansi(code: Int) = "\u001B[${code}m"
private val reset = ansi(RESET)
private val leadingCodes = Regex("^(?:\u001B\\[\\d+m)+")
private val ansiCode = Regex("\u001B\\[(\\d+)m")

private fun String.applyCodes(vararg codes: Int): String {
    val tags = codes.joinToString("") { ansi(it) }
    return split(reset).filter { it.isNotEmpty() }.joinToString("") { tags + it + reset }
}

// applyCodes starts every segment between resets with the codes of all styles applied to it, the most
// recent first, so the first color among a segment's leading codes is its most recently applied color.
private fun String.changeLatestColor(change: (Int) -> Int) = split(reset).joinToString(reset) { segment ->
    val codes = leadingCodes.find(segment)?.value.orEmpty()
    val color = ansiCode.findAll(codes).firstOrNull { it.groupValues[1].toInt().isColor }
    if (color == null) segment else segment.replaceRange(color.range, ansi(change(color.groupValues[1].toInt())))
}

fun <R> colored(enabled: Boolean = true, block: ColoredConsole.() -> R): R =
        if (enabled) object : ColoredConsole {}.block() else object : ColorConsoleDisabled {}.block()

fun <R : Style> style(block: ColoredConsole.() -> R): R = object : ColoredConsole {}.block()

@Suppress("unused")
fun print(colored: Boolean = true, block: ColoredConsole.() -> String) = colored(colored) { print(block()) }

fun println(colored: Boolean = true, block: ColoredConsole.() -> String) = colored(colored) { println(block()) }