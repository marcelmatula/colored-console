# Colored Console
Kotlin DSL ANSI Colored Console Ouput Library

#### Simple Examples

```kotlin
colored {
    println("Hello world".cyan.bold) 

    println("Hello world".cyan.bg)  // use Cyan as backgroud color

    val pi = 22f/7
    println(pi.blue.italic.underline)
}
```

#### Conditional coloring
```kotlin
colored {
    println(listOf(1,2,3,4,5).map { it.cyan { it.rem(2) == 0 } }.joinToString())
}
```
Example above prints all even numbers in Cyan color.

#### Custom Styles
```kotlin
colored {
    val header = green + underline + bold // custom style + or . can be used to group styles
    println("Hello world"(header)")
    // or
    println("Hello world".style(header)")
}
```
