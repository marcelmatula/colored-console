# Colored Console
Kotlin DSL ANSI Output Colored Console

#### How to use it

Just copy only one file com/github/mm/coloredconsole/ColoredConsole.kt into your project and use it.

* styles - **bold**, *italic*, strike, underline, hidden, faint, reverse, blink

* colors - black, white, red, blue, green, yellow, cyan, purple

* background colors support

* bright colors support

* works with normal OS - Linux, Mac OS (not Windows) 

#### Simple Examples


```kotlin
println { "Hello World".cyan.bold }

// or 

colored {
    println("Hello World".cyan.bold) 
}
```

<img src=".images/simple-1.png"/>


```kotlin
println { "Hello World".cyan.bg }  

// or

colored {
    // use Cyan as background color
    println("Hello World".cyan.bg)  
}
```

<img src=".images/simple-2.png"/>


```kotlin
// coloring/styling can be called on any object not just String
val pi = 22f/7
println { pi.blue.italic.underline }

// or

colored {
    println(pi.blue.italic.underline)
}
```

<img src=".images/simple-3.png"/>


#### Custom Styles

```kotlin
// custom style: characters + or . can be used to group styles
val header = style { green + underline + bold }
println { "Hello World"(header) }

// or

println { "Hello World".style(header) }
```

```kotlin
colored {
    // custom style: characters + or . can be used to group styles
    val header = green + underline + bold 
    println("Hello World"(header))
    
    // or
    
    println("Hello World".style(header))
}
```

<img src=".images/custom-1.png"/>



#### Conditional coloring

```kotlin
// prints all even numbers in Cyan color
println { listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } } }
```

```kotlin
// prints all even numbers in Cyan color
colored {
    println(listOf(1, 2, 3, 4, 5).joinToString { it.cyan { it.rem(2) == 0 } })
}
```

<img src=".images/condition-1.png"/>

```kotlin
// condition on style
colored {
    val chapter = cyan + underline + bold 
    val chapterNumber = 12
    println("$chapterNumber. Goodbye World"(chapter) { chapterNumber >= 10 })
    
    // or
    
    println("$chapterNumber. Goodbye World".style(chapter) { chapterNumber >= 10 })
}
```
<img src=".images/condition-2.png"/>


#### Nested coloring

```kotlin
println { ("bold " + ("italic " + ("color " + "Yellow".yellow.bold + " normal").faint + " italic").italic + " bold").bold }
```

```kotlin
colored {
    println(("bold " + ("italic " + ("color " + "Yellow".yellow.bold + " normal").faint + " italic").italic + " bold").bold)
}
```

<img src=".images/nested-1.png"/>


#### Enabling/Disable coloring

```kotlin
println(colored = true) { "Orange".yellow.bold + " Is the New " + "Black".bold.reverse }
```

or

```kotlin
colored(enabled = true) {
    println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
}
```

<img src=".images/disable-1.png"/>


```kotlin
println(colored = true) { "Orange".yellow.bold + " Is the New " + "Black".bold.reverse }
``` 

or

```kotlin
colored(enabled = false) {
    println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
}
``` 

<img src=".images/disable-2.png"/>

#### Coloring for all class methods 

Your class can implement ColoredConsole and in that case 
you do not need to repeat colored { ... } block in each method but just use color/style directly. 

```kotlin
class Weather(val degrees: Int) : ColoredConsole {
    fun display() = println("Degrees:".blue.bold + " $degrees".italic.bold)
}

```       

<img src=".images/class-1.png"/>

#### Bright Coloring 

```kotlin
val style1 = style { blue.bright + bold }
println { "bright blue".style(style1) }

// or 

println { "bright blue".blue.bright.bold }
```

```kotlin
colored {
    val style1 = blue.bright + bold
    println("bright blue".style(style1))
    
    // or 

    println("bright blue".blue.bright.bold)
}
```

#### Background Coloring 

```kotlin
println { "cyan background".cyan.bg }
```

```kotlin
colored {
    println("cyan background".cyan.bg)
}
```

