# Colored Console
Kotlin DSL ANSI Output Colored Console

#### How to use it

Just copy only one file com/github/mm/coloredconsole/ColoredConsole.kt into your project and use it.

* styles - **bold**, *italic*, strike, underline, hidden, faint, reverse, blink

* color - black, white, read, blue, green, yellow, cyan, purple

* background colors support

* works with normal OS - Linux, Mac OS (not Windows) 

#### Simple Examples


```kotlin
colored {
    println("Hello World".cyan.bold) 
}
```

<img src=".images/simple-1.png"/>


```kotlin
colored {
    // use Cyan as background color
    println("Hello World".cyan.bg)  
}
```

<img src=".images/simple-2.png"/>


```kotlin
colored {
    // coloring/styling can by called on any object not just String
    val pi = 22f/7
    println(pi.blue.italic.underline)
}
```

<img src=".images/simple-3.png"/>


#### Custom Styles
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
    println("${chapterNumber}. Goodbye World"(chapter) { chapterNumber >= 10 })
    
    // or
    
    println("${chapterNumber}. Goodbye World".style(chapter) { chapterNumber >= 10 })
}
```
<img src=".images/condition-2.png"/>


#### Disable coloring

```kotlin
colored(enabled = true) {
    println("Orange".yellow.bold + " Is the New " + "Black".bold.reverse)
}
```

<img src=".images/disable-1.png"/>


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
