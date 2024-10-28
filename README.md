<div align="center">
<img src=".art/kflect.webp" alt="KFlect" />
</div>

<div align="center">
  <a href="https://styles.goatbytes.io/lang/kotlin" rel="nofollow">
    <img src="https://img.shields.io/badge/Style%20Guide-Kotlin-7F52FF.svg?style=flat&labelColor=black&color=7F52FF&logo=kotlin" alt="Style Guide-Kotlin">
  </a>
  <a href="https://central.sonatype.com/artifact/io.goatbytes/kflect">
    <img src="https://img.shields.io/badge/-io.goatbytes:kflect:<version>-%230037FF?style=flat-square&logo=gradle" alt="Gradle Dependency" />
    <img src="https://img.shields.io/maven-central/v/io.goatbytes/kflect?logo=maven&color=0398fc" alt="Maven Central" />
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%20|%20JVM-dark" alt="Android | JVM" />
</div>


KFlect is a Kotlin library that provides dynamic access to class members. It enables you to inspect and manipulate classes at runtime, including private members, companion objects, and extension functions. This can be useful for testing, debugging, and advanced programming techniques.

## Getting Started

### Gradle

Add the following to your `build.gradle.kts` in your project:

```kotlin
dependencies {
  implementation("io.goatbytes:kflect:1.0.2")
}
```

## Example Usage

This guide demonstrates how `kflect` provides an efficient approach to interacting with classes dynamically. Using a `Person` class as our example, we’ll walk through instance creation, modifying private members, invoking companion object functions, and even accessing top-level extension functions.

Here’s a simple `Person` class with a private constructor, private fields, and a private function. Normally, these members would be inaccessible from the outside, but we can work with them flexibly through `kflect`.

```kotlin
package io.goatbytes.kflect.example

class Person private constructor(
  val name: String,
  val age: Int,
  private val ssn: String // Private property
) {
  constructor(name: String, age: Int) : this(name, age, generateSSN())

  fun getIntroduction(): String = "Hi, I'm $name and I'm $age years old."

  private fun getPrivateInfo(): String = "SSN: $ssn"

  companion object {
    private fun generateSSN(): String {
      return listOf(3, 2, 4).joinToString("-") {
        (1..it).joinToString("") { (1..9).random().toString() }
      }
    }
  }
}

// Top-level extension functions
fun Person.greet() = println("Hello, $name!")

private fun Person.is21() = age >= 21
```

---

### **Creating Instances**

With `kflect`, you can create an instance of `Person` even though the constructor is private:

```kotlin
kflect {
  val person = "io.goatbytes.kflect.example.Person"("Jared", 39) as Person
  println(person.getIntroduction())  // Output: "Hi, I'm Jared and I'm 39 years old."
}
```

---

### **Accessing and Modifying Private Members**

Sometimes, it’s necessary to read or modify private fields or call private methods. `kflect` allows this by letting us reach into the class and interact with its internal details:

```kotlin
kflect {
  val getPrivateInfo = person.function("getPrivateInfo")
  println(getPrivateInfo(person)) // Output: "SSN: XXX-XX-XXXX"

  val generateSSN = person.function("generateSSN")
  person["ssn"] = generateSSN()
  println(getPrivateInfo(person)) // Updated SSN
}
```

This method is particularly useful for testing, debugging, and working with data that would otherwise be restricted.

---

### **Companion Object Functions**

We can also access companion object functions, including private ones. Here’s an example of calling the private `generateSSN` method:

```kotlin
kflect {
  val generateSSN = person.function("generateSSN")
  println("Generated SSN: ${generateSSN()}")
}
```

---

### **Calling Top-Level Extension Functions**

You can even invoke top-level extension functions dynamically. Here’s how we use `greet` and check the `is21` function:

```kotlin
kflect {
  val greet = person.topLevelExtensionFunction(Person::class, "greet")
  greet(person)  // Output: "Hello, Jared!"

  val is21 = person.topLevelExtensionFunction(Person::class, "is21")
  if (is21(person) == true) {
    println("${person.name} is old enough to attend the conference.")
  } else {
    println("${person.name} isn't old enough for the conference yet.")
  }
}
```

### **Java Examples**

With `kflect`, you can simplify standard reflection tasks, such as invoking a method by name or accessing private fields in Java. This flexibility can save time during debugging or when working with restricted APIs.

#### **Example: Accessing String Substring Method**

```kotlin
val substringMethod = "java.lang.String".method("substring", Int::class, Int::class)
val result = substringMethod("Hello, World!", 7, 12)
println(result)  // Output: "World"
```

In this example, we’re calling `substring` on a `String` instance directly by the method name, which reduces the usual reflection boilerplate.

#### **Example: Modifying Private Fields**

```kotlin
val person = "com.example.Person"("Jane Doe", 25) as Person
person["privateField"] = "newValue"  // Modify a private field directly
```

Here, `kflect` enables modification of private fields without the usual `setAccessible` calls, making code more readable and concise.

---

### **Android Examples: Dynamic Interaction with Android Framework Classes**

On Android, `kflect` becomes particularly valuable when working with the platform's reflection-heavy APIs, such as accessing hidden fields or methods in the Android SDK. Here’s an example of how to use `kflect` for tasks like obtaining the current activity:

#### **Example: Accessing Current Activity**

Android’s framework classes often require reflection for accessing certain hidden APIs or working with internal details. Using `kflect`, you can dynamically obtain the current `Activity` without direct references to `ActivityThread`: 

```kotlin
val currentActivity: Activity = kflect {
  ("android.app.ActivityThread"("currentActivityThread")["mActivities"] as Map<*, *>)
    .values.firstNotNullOf { record ->
      when (record["paused"] == false) {
        true -> record["activity"] as Activity  // return the active android.app.Activity
        else -> null
      }
    }
}
```

This example is practical for use in debugging scenarios, dynamic feature delivery, and analytics libraries where obtaining the current activity context is essential.

### Using Predicates for Reflection Filtering

The `predicates` package in KFlect enables you to filter and search class members—like methods, properties, fields, and constructors—using declarative conditions. This approach simplifies reflection by enabling **selective access** to class members based on your specific criteria.

KFlect’s predicates provide a highly readable, **fluent interface** for querying classes. Rather than manually iterating through members, predicates offer a way to dynamically filter the members you need, reducing boilerplate and increasing readability.

---

#### Available Predicates

KFlect’s `predicates` package offers a range of filters that target different kinds of class members. Each predicate can be chained to form complex criteria, providing you with fine-grained control over member selection.

Here’s a quick overview of the available predicate classes:

- **[ConstructorPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/ConstructorPredicates.kt)**: Filters constructors based on parameters, annotations, and accessibility.
- **[ExecutablePredicates](src/main/kotlin/io/goatbytes/kflect/predicates/ExecutablePredicates.kt)**: General filters for executable members, like functions and methods.
- **[FieldPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/FieldPredicates.kt)**: Allows filtering fields based on type, visibility, and annotations.
- **[KCallablePredicates](src/main/kotlin/io/goatbytes/kflect/predicates/KCallablePredicates.kt)**: A generic set of filters for Kotlin callables, covering properties and functions.
- **[KFunctionPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/KFunctionPredicates.kt)**: Targets Kotlin-specific functions with conditions like return type and parameters.
- **[KPropertyPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/KPropertyPredicates.kt)**: Specific to properties, with filters for mutability, visibility, and initialization.
- **[MemberPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/MemberPredicates.kt)**: Broad member-level predicates to filter by name, annotations, and accessibility.
- **[MethodPredicates](src/main/kotlin/io/goatbytes/kflect/predicates/MethodPredicates.kt)**: Focused on Java methods, filtering by return type, parameters, and annotations.

---

#### Example Usage: Filtering with Predicates

Using KFlect’s predicates, you can filter and select specific members of a class without looping manually. Below are some examples illustrating various use cases.

```kotlin
// Find functions in `Random` with a `Unit` return type and an `Int` parameter
val functions = Random::class.filterFunctions {
    returnType(Unit::class) and hasParameterType(Int::class)
}

// Filter public methods in `String` with one parameter and a return type of `String`
val methods = String::class.java.filterMethods {
    hasParameterCount(1) and returnType(String::class.java) and isPublic()
}

// Locate a property in `StringBuilder` with an `Int` return type and the name "length"
val property = StringBuilder::class.findProperty {
    returnType(Int::class) and name("length")
}

// Find a method in `StringBuilder` named "length" with an `Int` return type
val method = StringBuilder::class.java.findMethod {
    name("length") and returnType(Int::class.java)
}
```

In these examples, `filterFunctions`, `filterMethods`, `findProperty`, and `findMethod` leverage predicates to locate members matching specific conditions.

#### Predicate Chaining

KFlect’s predicates can be chained using `and`, `or`, and `not`, making it easy to build complex conditions. For instance:

```kotlin
val functionsWithConditions = MyClass::class.filterFunctions {
  returnType(Unit::class) and (isPublic() or hasAnnotation<Deprecated>())
}
```

This query retrieves all `Unit`-returning functions in `String` that are either `public` or annotated with `@Deprecated`.

### Lazy Initialization

KFlect includes `LazyKFlect` and `SynchronizedLazyKFlect` for situations where **on-demand initialization** of reflection data is preferred. These classes are particularly useful for performance-sensitive applications or cases where reflection data might not be required immediately.

---

#### `LazyKFlect`

`LazyKFlect` enables lazy initialization of reflection-related objects, ensuring that they’re only created when accessed for the first time. This approach minimizes the initial memory footprint and computation cost, as resources are allocated only when necessary.

```kotlin
val lazyReflection by LazyKFlect { 
  "io.goatbytes.kflect.example.Person"("Alice", 28) as Person 
}
```

---

#### `SynchronizedLazyKFlect`

For **multithreaded environments**, `SynchronizedLazyKFlect` provides thread-safe lazy initialization by ensuring that only one thread can initialize the reflection data at a time.

```kotlin
val synchronizedLazyReflection by SynchronizedLazyKFlect { 
  "io.goatbytes.kflect.example.Person"("Bob", 35) as Person 
}
```

---

### Packages

```
src
└── main
    └── kotlin
        └── io
            └── goatbytes
                └── kflect
                    ├── cache        // Caching mechanism for reflection data
                    ├── dsl          // DSL for simplified reflection queries
                    ├── exceptions   // Custom exceptions for reflection handling
                    ├── ext          // Extension functions for reflection utilities
                    ├── lazy         // Lazy initialization utilities for reflection
                    ├── misc         // Miscellaneous helpers, like unsafe operations
                    ├── os           // OS-specific utilities
                    ├── predicates   // Predicates for filtering reflection data
                    └── traverser    // Traversal utilities for classes and functions
                        └── ext      // Extensions supporting traversal operations

```

## Contributing

Contributions are welcome! Please read our [contributing guide](CONTRIBUTING.md) and submit pull
requests to our repository.

## License

This project is licensed under the Apache 2.0 License — see the [LICENSE](LICENSE) file for details.

## ℹ️ About GoatBytes.IO <a name="about"></a>

![GoatBytesLogo](.art/logo_with_text_white_bg.svg)

<p align="center">
<a href="https://github.com/goatbytes" target="_blank">
    <img src="https://img.shields.io/badge/GitHub-GoatBytes-181717?logo=github" alt="GitHub">
</a>
<a href="https://twitter.com/goatbytes" target="_blank">
    <img src="https://img.shields.io/badge/Twitter-GoatBytes-1DA1F2?logo=twitter" alt="Twitter">
</a>
<a href="https://www.linkedin.com/company/goatbytes" target="_blank">
    <img src="https://img.shields.io/badge/LinkedIn-GoatBytes-0077B5?logo=linkedin" alt="LinkedIn">
</a>
<a href="https://www.instagram.com/goatbytes.io/" target="_blank">
    <img src="https://img.shields.io/badge/Instagram-GoatBytes.io-E4405F?logo=instagram" alt="Instagram">
</a>
</p>

At **GoatBytes.IO**, our mission is to develop secure software solutions that empower businesses to
transform the world. With a focus on innovation and excellence, we strive to deliver cutting-edge
products that meet the evolving needs of businesses across various industries.
