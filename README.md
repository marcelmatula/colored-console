# Colored Console
Kotlin DSL ANSI Ouput Colored Console

#### Simple Examples


```kotlin
colored {
    println("Hello World".cyan.bold) 
}
```

<img src=".images/simple-1.png"/>


```kotlin
colored {
    // use Cyan as backgroud color
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
    println("${chapterNumber}. Good Bye world"(chapter) { chapterNumber >= 10 })
    
    // or
    
    println("${chapterNumber}. Good Bye world".style(chapter) { chapterNumber >= 10 })
}
```
<img src=".images/condition-2.png"/>

