package readme

import com.github.mm.coloredconsole.print
import com.github.mm.coloredconsole.println
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

// What each README example in ReadmeImages.kt prints, with \e for the escape character (\e[1m is ESC [ 1 m).
// The palette is left out: it is the overview image, not an example to copy.
private val expected = mapOf(
    "usage" to """
        \e[1m\e[36mHello world\e[0m
        \e[1m\e[36mHello world\e[0m
    """.trimIndent(),
    "colors" to """\e[4m\e[3m\e[34m3.142857\e[0m""",
    "background" to """\e[46m\e[30mHello world\e[0m""",
    "bright" to """\e[1m\e[94mbright blue\e[0m""",
    "extended-colors" to """
        \e[38;5;208m256 colors\e[0m and \e[1m\e[38;2;95;135;255mtrue color\e[0m
        \e[48;2;255;215;95m\e[30m on a background \e[0m
    """.trimIndent(),
    "gradient" to
            gradient("Hello from Colored Console!", listOf(255, 95, 135), listOf(95, 175, 255), 38, before = """\e[1m""") +
            "\n" +
            gradient("  as a background gradient  ", listOf(95, 215, 175), listOf(175, 135, 255), 48, after = """\e[30m"""),
    "custom-style" to """
        \e[1m\e[4m\e[32mHello world\e[0m
        \e[1m\e[4m\e[32mHello world\e[0m
    """.trimIndent(),
    "custom-style-block" to """
        \e[1m\e[4m\e[32mHello world\e[0m
        \e[1m\e[4m\e[92mHello world\e[0m
    """.trimIndent(),
    "conditional" to """1, \e[36m2\e[0m, 3, \e[36m4\e[0m, 5""",
    "conditional-style" to """\e[1m\e[4m\e[36m12. Goodbye World\e[0m""",
    "nested" to """\e[1mbold \e[3mitalic \e[2mcolor \e[1m\e[33mYellow\e[0m\e[1m\e[3m\e[2m normal\e[0m\e[1m\e[3m italic\e[0m\e[1m bold\e[0m""",
    "styling-on" to """\e[1m\e[33mOrange\e[0m Is the New \e[7m\e[1mBlack\e[0m""",
    "styling-off" to """Orange Is the New Black""",
    "class" to """\e[1m\e[34mDegrees:\e[0m\e[1m\e[3m 22\e[0m""",
    "visible-length" to """
        \e[32mOK\e[0m        build
        \e[1m\e[31mFAILED\e[0m    tests
        \e[2mSKIPPED\e[0m   deploy
    """.trimIndent(),
    "strip-ansi" to """Error: disk full""",
)

// A gradient gives every character its own segment: the codes before it, the color (interpolated linearly
// between the two ends and rounded), the codes after it, the character and a reset.
private fun gradient(text: String, from: List<Int>, to: List<Int>, layer: Int, before: String = "", after: String = "") =
    text.indices.joinToString("") { i ->
        val color = from.zip(to) { start, end -> (start + (end - start) * i / (text.length - 1.0)).roundToInt() }
        """$before\e[$layer;2;${color.joinToString(";")}m$after${text[i]}\e[0m"""
    }

private fun String.readable() = replace("\r\n", "\n").replace("\u001B", "\\e")

class ReadmeExamplesTest {

    @Test
    fun everyExampleHasAnExpectedOutput() {
        assertEquals(examples.map { it.first } - "palette", expected.keys.toList())
    }

    @Test
    fun examplesPrintTheExpectedCodes() {
        for ((name, example) in examples) {
            val output = expected[name] ?: continue
            assertEquals(output, capture(example).readable(), "README example \"$name\"")
        }
    }

    @Test
    fun printAndPrintlnPrintWhatTheBlockReturns() {
        val output = capture {
            print { "a".red }
            println { "b".bold }
            println(colored = false) { "c".red }
            print(colored = false) { "d".bold }
        }
        assertEquals("""
            \e[31ma\e[0m\e[1mb\e[0m
            c
            d
        """.trimIndent(), output.readable())
    }
}
