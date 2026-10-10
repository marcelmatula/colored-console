package readme

import com.github.mm.coloredconsole.ColoredConsole
import com.github.mm.coloredconsole.colored
import com.github.mm.coloredconsole.print
import com.github.mm.coloredconsole.println
import com.github.mm.coloredconsole.stripAnsi
import com.github.mm.coloredconsole.style
import com.github.mm.coloredconsole.visibleLength
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

// Renders what each README example prints as a terminal-window SVG in .images/.
// The examples are copies of the README snippets (with the README's imports):
// change both together, then run `./gradlew readmeImages`.

class Weather(val degrees: Int) : ColoredConsole {
    fun display() = println("Degrees:".blue.bold + " $degrees".italic.bold)
}

internal val examples: List<Pair<String, () -> Unit>> = listOf(
    "palette" to { palette() },
    "usage" to {
        println { "Hello world".cyan.bold }

        colored {
            println("Hello world".cyan.bold)
        }
    },
    "colors" to {
        val pi = 22f / 7
        println { pi.blue.italic.underline }
    },
    "background" to {
        println { "Hello world".black.cyan.bg }
    },
    "bright" to {
        println { "bright blue".blue.bright.bold }
    },
    "extended-colors" to {
        println { "256 colors".color256(208) + " and " + "true color".rgb(95, 135, 255).bold }
        println { " on a background ".black.rgb(255, 215, 95).bg }
    },
    "gradient" to {
        println { "Hello from Colored Console!".gradient(rgb(255, 95, 135), rgb(95, 175, 255)).bold }
        println { "  as a background gradient  ".black.gradient(rgb(95, 215, 175), rgb(175, 135, 255)).bg }
    },
    "custom-style" to {
        val header = style { green + underline + bold }

        println { "Hello world"(header) }
        println { "Hello world".style(header) }
    },
    "custom-style-block" to {
        colored {
            val header = green.underline.bold
            val brightHeader = green.bright + underline + bold
            println("Hello world"(header))
            println("Hello world"(brightHeader))
        }
    },
    "conditional" to {
        // Prints the even numbers in cyan
        println { listOf(1, 2, 3, 4, 5).joinToString { it.cyan { n -> n % 2 == 0 } } }
    },
    "conditional-style" to {
        colored {
            val chapter = cyan + underline + bold
            val chapterNumber = 12
            println("$chapterNumber. Goodbye World"(chapter) { chapterNumber >= 10 })
        }
    },
    "nested" to {
        colored {
            val inner = ("color " + "Yellow".yellow.bold + " normal").faint
            val middle = ("italic " + inner + " italic").italic
            println(("bold " + middle + " bold").bold)
        }
    },
    "styling-on" to {
        println(colored = true) { "Orange".yellow.bold + " Is the New " + "Black".bold.reverse }
    },
    "styling-off" to {
        colored(enabled = false) {
            println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
        }
    },
    "class" to {
        Weather(22).display()
    },
    "visible-length" to {
        colored {
            val steps = listOf("build" to "OK".green, "tests" to "FAILED".red.bold, "deploy" to "SKIPPED".faint)
            for ((step, status) in steps) {
                println(status + " ".repeat(10 - status.visibleLength) + step)
            }
        }
    },
    "strip-ansi" to {
        val message = colored { "Error:".red.bold + " disk full" }
        println(message.stripAnsi())
    },
)

// The README example that turns styling off automatically. It is only compiled, not rendered or tested,
// because what it prints depends on the environment.
@Suppress("unused")
private fun automaticStyling() {
    val useColors = System.console()?.isTerminal == true && System.getenv("NO_COLOR").isNullOrEmpty()

    colored(enabled = useColors) {
        println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
    }
}

// The overview image at the top of the README: every color and style.
private fun palette() = colored {
    val names = listOf("black", "red", "green", "yellow", "blue", "purple", "cyan", "white")
    val colors = listOf(black, red, green, yellow, blue, purple, cyan, white)
    fun row(label: String, cell: (String, ColoredConsole.Style) -> String) =
        println(label.padEnd(12).faint + names.zip(colors).joinToString("") { (name, color) -> cell(name, color) })

    row("color") { name, color -> name.style(color) + " ".repeat(8 - name.length) }
    row(".bright") { name, color -> name.style(color.bright) + " ".repeat(8 - name.length) }
    // Dark text on the background swatches keeps the names readable (white on the black swatch).
    fun swatch(name: String, background: ColoredConsole.Style) =
        " $name ".padEnd(8).style(background + if (name == "black") white.bright else black)
    row(".bg") { name, color -> swatch(name, color.bg) }
    row(".bright.bg") { name, color -> swatch(name, color.bright.bg) }
    println("styles".padEnd(12).faint +
            listOf("bold".bold, "faint".faint, "italic".italic, "underline".underline, "strike".strike, "reverse".reverse)
                .joinToString("  "))
}

fun main(args: Array<String>) {
    val dir = File(args.single())
    for ((name, example) in examples) {
        File(dir, "$name.svg").writeText(svg(parse(capture(example))))
    }
}

internal fun capture(example: () -> Unit): String {
    val buffer = ByteArrayOutputStream()
    val out = System.out
    System.setOut(PrintStream(buffer, true, Charsets.UTF_8))
    try {
        example()
    } finally {
        System.setOut(out)
    }
    return buffer.toString(Charsets.UTF_8).trimEnd('\r', '\n')
}

// region ANSI parsing

// fg and bg are CSS colors; null is the terminal's default.
private data class Attributes(
    val fg: String? = null,
    val bg: String? = null,
    val bold: Boolean = false,
    val faint: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val reverse: Boolean = false,
    val hidden: Boolean = false,
    val strike: Boolean = false,
)

private data class Run(val text: String, val attributes: Attributes)

private val sgr = Regex("\u001B\\[([0-9;]*)m")

private fun parse(output: String): List<List<Run>> = output.lines().map { line ->
    val runs = mutableListOf<Run>()
    var attributes = Attributes()
    var position = 0
    for (match in sgr.findAll(line)) {
        if (match.range.first > position) runs += Run(line.substring(position, match.range.first), attributes)
        attributes = attributes.withCodes(match.groupValues[1].split(';').map { it.toIntOrNull() ?: 0 })
        position = match.range.last + 1
    }
    if (position < line.length) runs += Run(line.substring(position), attributes)
    runs
}

// One escape code can hold several parameters; 38/48 start an extended color: 5;n or 2;r;g;b.
private fun Attributes.withCodes(codes: List<Int>): Attributes {
    var attributes = this
    var i = 0
    while (i < codes.size) {
        val code = codes[i]
        val color = when {
            code != 38 && code != 48 -> null
            codes.getOrNull(i + 1) == 5 -> xterm256(codes[i + 2]).also { i += 3 }
            codes.getOrNull(i + 1) == 2 -> hex(codes[i + 2], codes[i + 3], codes[i + 4]).also { i += 5 }
            else -> error("Unsupported extended color in $codes")
        }
        attributes = when {
            color == null -> attributes.withCode(code).also { i++ }
            code == 38 -> attributes.copy(fg = color)
            else -> attributes.copy(bg = color)
        }
    }
    return attributes
}

// The xterm 256-color palette: the 16 ANSI colors, a 6x6x6 color cube, then 24 grays.
private fun xterm256(index: Int): String = when (index) {
    in 0..15 -> palette[index]
    in 16..231 -> (index - 16).let { cube ->
        val level = { step: Int -> if (step == 0) 0 else 55 + step * 40 }
        hex(level(cube / 36), level(cube / 6 % 6), level(cube % 6))
    }
    else -> (8 + (index - 232) * 10).let { gray -> hex(gray, gray, gray) }
}

private fun hex(red: Int, green: Int, blue: Int) =
    "#" + listOf(red, green, blue).joinToString("") { it.toString(16).padStart(2, '0') }

// Bold and faint share one intensity setting, so the later one wins (as in most terminals).
// Blink (5) is drawn as normal text.
private fun Attributes.withCode(code: Int) = when (code) {
    0 -> Attributes()
    1 -> copy(bold = true, faint = false)
    2 -> copy(faint = true, bold = false)
    3 -> copy(italic = true)
    4 -> copy(underline = true)
    5 -> this
    7 -> copy(reverse = true)
    8 -> copy(hidden = true)
    9 -> copy(strike = true)
    in 30..37 -> copy(fg = palette[code - 30])
    in 40..47 -> copy(bg = palette[code - 40])
    in 90..97 -> copy(fg = palette[code - 90 + 8])
    in 100..107 -> copy(bg = palette[code - 100 + 8])
    else -> error("Unsupported SGR code $code")
}

// endregion

// region SVG rendering

// All sizes are whole pixels, so no number formatting (and no locale) is involved.
private const val FONT_SIZE = 15
private const val CELL_WIDTH = 9 // monospace advance at 15px
private const val LINE_HEIGHT = 22
private const val BASELINE = 16
private const val PADDING_X = 18
private const val PADDING_TOP = 38
private const val PADDING_BOTTOM = 14
private const val MIN_WIDTH = 560

private const val BACKGROUND = "#282c34"
private const val FOREGROUND = "#abb2bf"
private const val BORDER = "#3e4451"
private const val FONTS = "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace"

// black, red, green, yellow, blue, purple, cyan, white; then their bright variants
private val palette = listOf(
    "#3f4451", "#e06c75", "#98c379", "#e5c07b", "#61afef", "#c678dd", "#56b6c2", "#d0d4dc",
    "#7f848e", "#ff7b86", "#b5e48c", "#ffd98e", "#8dc6ff", "#de9bf2", "#7fdbe8", "#ffffff",
)

private fun svg(lines: List<List<Run>>): String {
    val columns = lines.maxOf { runs -> runs.sumOf { it.text.length } }
    val width = maxOf(MIN_WIDTH, columns * CELL_WIDTH + 2 * PADDING_X)
    val height = PADDING_TOP + lines.size * LINE_HEIGHT + PADDING_BOTTOM
    val plainText = lines.joinToString("\n") { runs -> runs.joinToString("") { it.text } }

    return buildString {
        appendLine("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height" role="img">""")
        appendLine("<title>${plainText.escaped()}</title>")
        appendLine("""<rect x="0.5" y="0.5" width="${width - 1}" height="${height - 1}" rx="8" fill="$BACKGROUND" stroke="$BORDER"/>""")
        listOf("#ff5f57", "#febc2e", "#28c840").forEachIndexed { i, color ->
            appendLine("""<circle cx="${20 + i * 20}" cy="19" r="6" fill="$color"/>""")
        }
        appendLine("""<g font-family="$FONTS" font-size="$FONT_SIZE">""")
        lines.forEachIndexed { row, runs ->
            val top = PADDING_TOP + row * LINE_HEIGHT
            var column = 0
            for (run in runs) {
                appendRun(run, x = PADDING_X + column * CELL_WIDTH, top = top)
                column += run.text.length
            }
        }
        appendLine("</g>")
        appendLine("</svg>")
    }
}

private fun StringBuilder.appendRun(run: Run, x: Int, top: Int) {
    val a = run.attributes
    val width = run.text.length * CELL_WIDTH
    val baseline = top + BASELINE
    var fg = a.fg ?: FOREGROUND
    var bg = a.bg
    if (a.reverse) bg = fg.also { fg = bg ?: BACKGROUND }

    if (bg != null) appendLine("""<rect x="$x" y="$top" width="$width" height="$LINE_HEIGHT" fill="$bg"/>""")
    if (a.hidden) return

    val content = buildString {
        if (run.text.isNotBlank()) {
            val weight = if (a.bold) """ font-weight="bold"""" else ""
            val style = if (a.italic) """ font-style="italic"""" else ""
            append("""<text x="$x" y="$baseline" textLength="$width" lengthAdjust="spacing" fill="$fg"$weight$style>""")
            // Browsers collapse spaces at the edges of SVG text even with xml:space, so use no-break spaces.
            append(run.text.escaped().replace(' ', ' '))
            appendLine("</text>")
        }
        // Lines sit at half pixels so a 1px stroke stays sharp.
        if (a.underline) appendLine("""<line x1="$x" y1="${baseline + 3}.5" x2="${x + width}" y2="${baseline + 3}.5" stroke="$fg"/>""")
        if (a.strike) appendLine("""<line x1="$x" y1="${baseline - 5}.5" x2="${x + width}" y2="${baseline - 5}.5" stroke="$fg"/>""")
    }
    if (content.isEmpty()) return
    if (a.faint) append("""<g opacity="0.6">""").append(content).appendLine("</g>") else append(content)
}

private fun String.escaped() = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

// endregion
