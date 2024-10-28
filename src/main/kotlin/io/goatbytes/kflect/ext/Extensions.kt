/*
 * Copyright (c) 2024 GoatBytes.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.goatbytes.kflect.ext

import io.goatbytes.kflect.Annotations
import io.goatbytes.kflect.GenericParameterTypes
import io.goatbytes.kflect.JavaClass
import io.goatbytes.kflect.KConstructor
import io.goatbytes.kflect.KParameterTypes
import io.goatbytes.kflect.KotlinClass
import io.goatbytes.kflect.NullableArgs
import io.goatbytes.kflect.ParameterTypes
import io.goatbytes.kflect.Parameters
import io.goatbytes.kflect.Reflective
import io.goatbytes.kflect.TypeParameters
import io.goatbytes.kflect._INIT_
import io.goatbytes.kflect.dsl.accessible
import io.goatbytes.kflect.dsl.accessibleOrNull
import io.goatbytes.kflect.dsl.attempt
import io.goatbytes.kflect.dsl.constructorPredicates
import io.goatbytes.kflect.dsl.fieldPredicates
import io.goatbytes.kflect.dsl.functionPredicates
import io.goatbytes.kflect.dsl.methodPredicates
import io.goatbytes.kflect.dsl.propertyPredicates
import io.goatbytes.kflect.dsl.tryOrNull
import io.goatbytes.kflect.exceptions.NoSuchFunctionException
import io.goatbytes.kflect.exceptions.NoSuchPropertyException
import io.goatbytes.kflect.isReflectionBlocked
import io.goatbytes.kflect.misc.TheUnsafe
import io.goatbytes.kflect.predicates.ConstructorPredicates
import io.goatbytes.kflect.predicates.FieldPredicates
import io.goatbytes.kflect.predicates.KFunctionPredicates
import io.goatbytes.kflect.predicates.KPropertyPredicates
import io.goatbytes.kflect.predicates.MethodPredicates
import io.goatbytes.kflect.predicates.Predicate
import io.goatbytes.kflect.traverser.ClassTraverser
import io.goatbytes.kflect.traverser.ext.callableTraverser
import io.goatbytes.kflect.traverser.ext.functionTraverser
import io.goatbytes.kflect.traverser.ext.propertyTraverser
import io.goatbytes.kflect.traverser.ext.traverser
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.InaccessibleObjectException
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.kotlinProperty

/**
 * Traverses the class hierarchy starting from this class and executes the given [block] for each
 * class.
 *
 * This function begins with the current class and traverses up the hierarchy, invoking the provided
 * [block] on each class, including the superclasses.
 *
 * @param block The action to be performed on each class in the hierarchy.
 */
inline infix fun JavaClass.traverse(block: JavaClass.() -> Unit) {
  var jClass: JavaClass? = this
  while (jClass != null) {
    jClass.block()
    jClass = jClass.superclass
  }
}

/**
 * Traverses the class hierarchy and returns the first result from the given block.
 *
 * @param T The expected return type.
 * @param block A block to be executed on each class in the hierarchy.
 * @return The first non-null result from the block, or null if none is found.
 */
inline fun <reified T> JavaClass.traverseFirstNonNullOrNullOf(block: JavaClass.() -> T?): T? {
  var jClass: JavaClass? = this
  while (jClass != null) {
    when (val value = jClass.block()) {
      null -> jClass = jClass.superclass
      else -> return value
    }
  }
  return null
}

/**
 * Traverses the class hierarchy and returns the first result from the given block.
 *
 * @param T The expected return type.
 * @param block A block to be executed on each class in the hierarchy.
 * @return The first non-null result from the block.
 * @throws NoSuchElementException if no result is found.
 */
@Throws(NoSuchElementException::class)
inline fun <reified T> JavaClass.traverseFirstNonNullOf(block: JavaClass.() -> T?): T {
  var jClass: JavaClass? = this
  while (jClass != null) {
    when (val value = jClass.block()) {
      null -> jClass = jClass.superclass
      else -> return value
    }
  }
  throw NoSuchElementException("${T::class.java.name} not found in the class hierarchy")
}

/**
 * Traverses the class hierarchy starting from this class and executes the given [block] for each
 * class.
 *
 * This function begins with the current class and traverses up the hierarchy, invoking the provided
 * [block] on each class, including the superclasses.
 *
 * @param block The action to be performed on each class in the hierarchy.
 */
inline fun KotlinClass.traverse(block: KotlinClass.() -> Unit) {
  block(this)
  superclasses.forEach(block)
}

/**
 * Traverses the class hierarchy and returns the first result from the given block.
 *
 * @param T The expected return type.
 * @param block A block to be executed on each class in the hierarchy.
 * @return The first non-null result from the block, or null if none is found.
 */
inline fun <reified T> KotlinClass.traverseFirstNonNullOrNullOf(block: KotlinClass.() -> T?): T? {
  traverse { block(this)?.let { result -> return result } }
  return null
}

/**
 * Traverses the class hierarchy and returns the first result from the given block.
 *
 * @param T The expected return type.
 * @param block A block to be executed on each class in the hierarchy.
 * @return The first non-null result from the block.
 * @throws NoSuchElementException if no result is found.
 */
@Throws(NoSuchElementException::class)
inline fun <reified T> KotlinClass.traverseFirstNonNullOf(block: KotlinClass.() -> T?): T {
  traverse { block(this)?.let { return it } }
  throw NoSuchElementException("${T::class.name} not found in the class hierarchy")
}

/**
 * Returns the [Class] object associated with the class or interface with the given string name.
 * Invoking this method is equivalent to:
 *
 * ```kotlin
 * Class.forName(className)
 * ```
 *
 * @receiver The fully qualified name of the desired class.
 * @return The [Class] object for the class with the specified name.
 * @throws ClassNotFoundException if the class cannot be located
 */
@Throws(ClassNotFoundException::class)
fun String.toJavaClass(): JavaClass = Class.forName(this)

/**
 * Checks if the current Java class was compiled with Kotlin.
 *
 * This function verifies if the Java class is a Kotlin-compiled class by checking for
 * the presence of the `kotlin.Metadata` annotation. The `Metadata` annotation is
 * added by the Kotlin compiler to all Kotlin classes.
 *
 * @return `true` if the class was compiled with Kotlin, `false` otherwise.
 */
fun JavaClass.isKotlinCompiled(): Boolean = annotations.any { annotation ->
  annotation.annotationClass == Metadata::class
}

/**
 * Checks if the current Java class was compiled with Java.
 *
 * This function determines if the class is a Java-compiled class by checking that
 * none of the annotations on the class are `kotlin.Metadata`. If all annotations
 * are different from `Metadata`, the class is considered a Java-compiled class.
 *
 * @return `true` if the class was compiled with Java, `false` if it was compiled with Kotlin.
 */
fun JavaClass.isJavaCompiledClass(): Boolean = annotations.all { annotation ->
  annotation.annotationClass != Metadata::class
}

/**
 * Checks if the current Kotlin class was compiled with Kotlin.
 *
 * This function verifies if the class was compiled with Kotlin by inspecting the
 * underlying Java class for the `kotlin.Metadata` annotation, which is applied
 * to Kotlin-compiled classes.
 *
 * @return `true` if the Kotlin class was compiled with Kotlin, `false` otherwise.
 */
fun KotlinClass.isKotlinCompiledClass(): Boolean = java.annotations.any { annotation ->
  annotation.annotationClass == Metadata::class
}

/**
 * Checks if the current Kotlin class was compiled with Java.
 *
 * This function determines if the class was compiled with Java by checking that
 * none of the annotations on the underlying Java class are `kotlin.Metadata`. If all
 * annotations differ from `Metadata`, the class is considered a Java-compiled class.
 *
 * @return `true` if the class was compiled with Java, `false` if it was compiled with Kotlin.
 */
fun KotlinClass.isJavaCompiledClass(): Boolean = java.annotations.all { annotation ->
  annotation.annotationClass != Metadata::class
}

/**
 * Returns the [KClass] object associated with the class or interface with the given string name.
 * Invoking this method is equivalent to:
 *
 * ```
 * Class.forName(className).kotlin
 * ```
 *
 * @receiver The fully qualified name of the desired class.
 * @return The [KClass] object for the class with the specified name.
 * @throws ClassNotFoundException if the class cannot be located
 */
@Throws(ClassNotFoundException::class)
fun String.toKotlinClass(): KotlinClass = toJavaClass().kotlin

/**
 * Extension property to retrieve the Java class for an object.
 * Handles different types: [Class], [KClass], and class names represented as [String].
 *
 * @receiver The object to retrieve the Java class for.
 * @return The Java class of the receiver.
 */
val Reflective.jClass: JavaClass
  get() = this as? JavaClass
    ?: (this as? KotlinClass)?.java
    ?: (this as? String)?.java
    ?: this::class.java

/**
 * Extension property to retrieve the Kotlin class for an object.
 * Handles different types: [KClass], [Class], and class names represented as [String].
 *
 * @receiver The object to retrieve the Kotlin class for.
 * @return The Kotlin class of the receiver.
 */
val Reflective.kClass: KotlinClass
  get() = this as? KotlinClass
    ?: (this as? JavaClass)?.kotlin
    ?: (this as? String)?.kotlin
    ?: this::class

/**
 * Retrieves the synthetic file class (i.e., the top-level Kotlin class) associated with this
 * [KClass].
 *
 * In Kotlin, when top-level functions or properties are declared in a file, the compiler generates
 * a synthetic class with the name of the file followed by "Kt" (unless overridden by `@JvmName`
 * annotation).
 *
 * This property appends "Kt" to the current class name and attempts to retrieve the corresponding
 * [KClass].
 *
 * Example:
 * For a Kotlin file `Example.kt`, this will return the `ExampleKt` class which contains the
 * top-level members declared in the file.
 *
 * @return The [KClass] corresponding to the synthetic file class, or `null` if the file class
 * cannot be found.
 */
val KotlinClass.topLevelClass: KotlinClass? get() = "$name$TOP_LEVEL_SUFFIX".kotlin

/**
 * Determines if the current [KClass] is a top-level class (i.e., a Kotlin file class).
 *
 * A top-level class is one that represents a synthetic class generated for top-level
 * functions, properties, and other members in a Kotlin file. These classes typically
 * do not have constructors, companion objects, or nested classes, and their name
 * usually ends with "Kt" unless explicitly changed via the `@JvmName` annotation.
 *
 * This method relies on several key characteristics:
 * - The class name ends with "Kt".
 * - The class has no constructors (since it's synthetic).
 * - The class has no companion objects or is not a companion object itself.
 * - The class has no nested classes.
 *
 * @return `true` if this [KClass] is a top-level (file) class, `false` otherwise.
 */
fun KotlinClass.isTopLevelClass(): Boolean {
  return name.endsWith("Kt") &&
    constructors.isEmpty() &&
    companionObject == null &&
    !isCompanion &&
    nestedClasses.isEmpty()
}

/**
 * Retrieves the collection of top-level functions for this [KClass].
 *
 * If the current class is identified as a top-level class (e.g., a file class), it retrieves
 * all the top-level functions declared within it. If the current class is not a top-level class,
 * it attempts to find the top-level class corresponding to the current class and retrieves
 * the top-level functions from there.
 *
 * @receiver The [KClass] to inspect for top-level functions.
 * @return A collection of [KFunction] objects representing the top-level functions
 *         declared in the class. If no functions are found, an empty list is returned.
 */
val KotlinClass.topLevelFunctions: Collection<KFunction<*>>
  get() = when (isTopLevelClass()) {
    true -> java
    else -> topLevelClass?.java
  }?.declaredMethods?.mapNotNull { method ->
    method.kotlinFunction
  } ?: emptyList()

/**
 * Retrieves the collection of top-level properties for this [KClass].
 *
 * If the current class is identified as a top-level class (e.g., a file class), it retrieves
 * all the top-level properties declared within it. If the current class is not a top-level class,
 * it attempts to find the top-level class corresponding to the current class and retrieves
 * the top-level properties from there.
 *
 * @receiver The [KClass] to inspect for top-level properties.
 * @return A collection of [KProperty] objects representing the top-level properties
 *         declared in the class. If no properties are found, an empty list is returned.
 */
val KotlinClass.topLevelProperties: Collection<KProperty<*>>
  get() = when {
    isTopLevelClass() -> this
    else -> topLevelClass
  }?.java?.declaredFields?.mapNotNull { field ->
    field.kotlinProperty
  } ?: emptyList()

/**
 * Retrieves the collection of top-level members for this [KClass].
 *
 * If the current class is identified as a top-level class (e.g., a file class), it retrieves
 * all the top-level members declared within it. If the current class is not a top-level class,
 * it attempts to find the top-level class corresponding to the current class and retrieves
 * the top-level members from there.
 *
 * @receiver The [KClass] to inspect for top-level members.
 * @return A collection of [KCallable] objects representing the top-level members
 *         declared in the class. If no members are found, an empty list is returned.
 */
val KotlinClass.topLevelMembers: Collection<KCallable<*>>
  get() = topLevelFunctions + topLevelProperties

/**
 * Retrieves the collection of companion object functions for this [KClass].
 *
 * This property provides access to all the functions declared within the companion object.
 * If the current class is itself a companion object, it returns its functions.
 * If there is no companion object, an empty collection is returned.
 *
 * Example usage:
 * ```kotlin
 * val companionFunctions = MyClass::class.companionFunctions
 * companionFunctions.forEach { function ->
 *     println(function.name)
 * }
 * ```
 *
 * @receiver The [KClass] to retrieve the companion object functions from.
 * @return A collection of [KFunction]s representing the functions in the companion object.
 */
val KotlinClass.companionFunctions: Collection<KFunction<*>>
  get() = when {
    isCompanion -> functions
    else -> companionObject?.functions ?: emptyList()
  }

/**
 * Retrieves all functions for this [KClass], including regular functions, companion object
 * functions, and top-level functions.
 *
 * This property provides a comprehensive list of all the functions associated with this class,
 * combining functions declared in the class itself, in its companion object, and top-level
 * functions.
 *
 * Example usage:
 * ```kotlin
 * val allFunctions = MyClass::class.allFunctions
 * allFunctions.forEach { function ->
 *     println(function.name)
 * }
 * ```
 *
 * @receiver The [KClass] containing the functions.
 * @return A collection of [KFunction]s representing all functions in this class.
 */
val KotlinClass.allFunctions: Collection<KFunction<*>>
  get() = buildList {
    addAll(functions)
    addAll(companionFunctions)
    addAll(topLevelFunctions)
  }

/**
 * Retrieves all members for this [KClass], including regular members, companion object
 * members, top-level members.
 *
 * This property provides a comprehensive list of all the members associated with this class,
 * combining members declared in the class itself, in its companion object, and top-level
 * members.
 *
 * Example usage:
 * ```kotlin
 * val allMembers = MyClass::class.allMembers
 * allMembers.forEach { member ->
 *     println(member.name)
 * }
 * ```
 *
 * @receiver The [KClass] containing the members.
 * @return A collection of [KCallable]s representing all members in this class.
 */
val KotlinClass.allMembers: Collection<KCallable<*>>
  get() = buildList {
    addAll(members)
    companionObject?.run {
      addAll(members)
    }
    addAll(topLevelMembers)
  }

/**
 * Retrieves the collection of companion object properties for this [KClass].
 *
 * This property provides access to all the properties declared within the companion object.
 * If the current class is itself a companion object, it returns its properties.
 * If there is no companion object, an empty collection is returned.
 *
 * Example usage:
 * ```kotlin
 * val companionProperties = MyClass::class.companionProperties
 * companionProperties.forEach { property ->
 *     println(property.name)
 * }
 * ```
 *
 * @receiver The [KClass] to retrieve the companion object properties from.
 * @return A collection of [KProperty]s representing the properties in the companion object.
 */
val KotlinClass.companionProperties: Collection<KProperty<*>>
  get() = when {
    isCompanion -> memberProperties
    else -> companionObject?.memberProperties ?: emptyList()
  }

/**
 * Retrieves all properties for this [KClass], including regular properties, companion object
 * properties, top-level properties.
 *
 * This property provides a comprehensive list of all the properties associated with this class,
 * combining properties declared in the class itself, in its companion object, and top-level
 * properties.
 *
 * Example usage:
 * ```kotlin
 * val allMembers = MyClass::class.allProperties
 * allMembers.forEach { member ->
 *     println(member.name)
 * }
 * ```
 *
 * @receiver The [KClass] containing the properties.
 * @return A collection of [KCallable]s representing all properties in this class.
 */
val KotlinClass.allProperties: Collection<KCallable<*>>
  get() = buildList {
    addAll(memberProperties)
    addAll(staticProperties)
    companionObject?.run {
      addAll(memberProperties)
      addAll(staticProperties)
    }
    addAll(topLevelProperties)
  }

/**
 * Returns the fully qualified dot-separated name of the class.
 *
 * @return the name of the class, interface, or other entity represented by this [KClass] object.
 * @see KClass.qualifiedName
 * @see Class.name
 */
val KotlinClass.name: String get() = qualifiedName ?: java.name

/**
 * Retrieves the name of the classifier, if available.
 *
 * This property returns the name of the classifier depending on its specific type. It supports
 * several classifier types including `KClass`, `KFunction`, `KParameter`, and `KTypeParameter`.
 * If the classifier type is unknown, it returns `"<unknown>"`.
 *
 * @receiver The classifier whose name is being retrieved.
 * @return The name of the classifier if available, or `"<unknown>"` if the type is not supported.
 */
val KClassifier.name: String
  get() = when (this) {
    is KClass<*> -> name
    is KFunction<*> -> name
    is KParameter -> name.orEmpty()
    is KTypeParameter -> name
    else -> "<unknown>"
  }

private val String.kotlin: KotlinClass?
  get() = tryOrNull { if (isClass()) toKotlinClass() else null }

private val String.java: Class<*>?
  get() = tryOrNull { if (isClass()) toJavaClass() else null }

private const val TOP_LEVEL_SUFFIX = "Kt"

// -------------------------------------------------------------------------------------------------

/**
 * Retrieves a method by name and parameter types.
 *
 * @receiver The class to retrieve the method for.
 * @param name The method name.
 * @param types The parameter types.
 * @return The method.
 * @throws NoSuchElementException if the method was not found in the class hierarchy.
 */
@Throws(NoSuchElementException::class)
fun JavaClass.method(name: String, vararg types: JavaClass): Method =
  accessible(this, name, *types)

/**
 * Retrieves a method by name and parameter types, or null if not found.
 *
 * @receiver The class to retrieve the method for.
 * @param name The method name.
 * @param types The parameter types.
 * @return The method or null if not found.
 */
fun JavaClass.methodOrNull(name: String, vararg types: JavaClass): Method? =
  accessibleOrNull(this, name, *types)

/**
 * Finds a field by name, and returns it.
 *
 * @receiver The class to retrieve the field for.
 * @param name The name of the field.
 * @throws NoSuchElementException if the field was not found in the class hierarchy.
 */
@Throws(IllegalStateException::class, NoSuchFieldException::class)
infix fun JavaClass.field(name: String): Field =
  accessible(this, name)

/**
 * Finds a field by name, returning it or `null` if not found.
 *
 * @receiver The class to retrieve the field for.
 * @param name The name of the field.
 * @return The found [Field], or `null` if not found.
 */
fun JavaClass.fieldOrNull(name: String): Field? =
  accessibleOrNull(this, name)

/**
 * Finds a constructor by its parameter types and returns it.
 *
 * @receiver The class to retrieve the constructor for.
 * @param types The parameter types of the constructor.
 * @return The found [Constructor].
 * @throws NoSuchElementException if the constructor was not found in the class hierarchy.
 */
fun JavaClass.constructor(vararg types: JavaClass): Constructor<*> =
  accessible(this, _INIT_, *types)

/**
 * Finds a constructor by its parameter types, returning it or `null` if not found.
 *
 * @receiver The class to retrieve the constructor for.
 * @param types The parameter types of the constructor.
 * @return The found [Constructor], or `null` if not found.
 */
fun JavaClass.constructorOrNull(vararg types: JavaClass): Constructor<*>? =
  accessibleOrNull(this, _INIT_, *types)

/**
 * Retrieves a function by name and parameter types.
 *
 * @receiver The class to retrieve the function for.
 * @param name The function name.
 * @param types The parameter types.
 * @return The function.
 * @throws NoSuchFunctionException if the function was not found in the class hierarchy
 */
@Throws(NoSuchFunctionException::class)
fun KotlinClass.function(name: String, vararg types: KClassifier): KFunction<*> {
  return functionOrNull(name, *types) ?: throw NoSuchFunctionException(
    "'fun $name${types.signature()}' not found in '${this.name}'"
  )
}

/**
 * Retrieves a function by name and parameter types, or null if not found.
 *
 * @receiver The class to retrieve the function for.
 * @param name The function name.
 * @param types The parameter types.
 * @return The function or null if not found.
 */
fun KotlinClass.functionOrNull(name: String, vararg types: KClassifier): KFunction<*>? {
  return findFunction { name(name) and parameters(*types) }?.apply { accessible = true }
}

/**
 * Finds the first non-null function in this [KClass] that matches the given condition.
 *
 * This function searches through the functions, companion functions, and top-level functions
 * of this class and returns the first function that satisfies the provided condition.
 * If no such function is found, `null` is returned.
 *
 * Example usage:
 * ```kotlin
 * val functionOrNull = MyClass::class.firstNonNullFunctionOrNull { it.name == "myFunction" }
 * if (functionOrNull != null) {
 *     println("Found function: ${'$'}{functionOrNull.name}")
 * }
 * ```
 *
 * @receiver The [KClass] to traverse functions from.
 * @param block The condition to apply to each function.
 * @return The first matching [KFunction], or `null` if no match is found.
 */
fun KotlinClass.firstNonNullFunctionOrNull(block: KFunction<*>.() -> Boolean): KFunction<*>? {
  return buildList {
    add { functions }
    add { companionFunctions }
    add { topLevelFunctions }
  }.firstNotNullOfOrNull { getFunctions ->
    val functions = getFunctions()
    functions.find(block)
  }
}

/**
 * Finds the first non-null function in this [KClass] that matches the given condition.
 *
 * This function searches through the functions, companion functions, and top-level functions
 * of this class and returns the first function that satisfies the provided condition.
 * If no such function is found, a [NoSuchFunctionException] is thrown.
 *
 * Example usage:
 * ```kotlin
 * val function = MyClass::class.firstNonNullFunction { it.name == "myFunction" }
 * println("Found function: ${'$'}{function.name}")
 * ```
 *
 * @receiver The [KClass] to traverse functions from.
 * @param block The condition to apply to each function.
 * @return The first matching [KFunction].
 * @throws NoSuchFunctionException if no matching function is found.
 */
@Throws(NoSuchFunctionException::class)
fun KotlinClass.firstNonNullFunction(block: KFunction<*>.() -> Boolean): KFunction<*> =
  firstNonNullFunctionOrNull(block) ?: throw NoSuchFunctionException()

/**
 * Checks if the value parameters of the function match the given classifiers in both size and type.
 *
 * This infix function compares the classifiers of the function's value parameters with the given
 * array of classifiers. It ensures that each value parameter has a corresponding classifier that
 * matches.
 *
 * @receiver The function whose value parameters are being checked.
 * @param classifiers An array of classifiers to compare against the function's value parameters.
 * @return `true` if the function's value parameters match the given classifiers in both size and
 * type, `false` otherwise.
 */
infix fun KFunction<*>.valueParametersEqual(classifiers: Array<out KClassifier>): Boolean {
  return valueParameters.run {
    size == classifiers.size && zip(classifiers).all { types ->
      val (parameter, classifier) = types
      parameter.type.classifier == classifier
    }
  }
}

/**
 * Checks if the value parameters of the function match the given classifiers in both size and type.
 *
 * This infix function compares the classifiers of the function's value parameters with the given
 * list of classifiers. It ensures that each value parameter has a corresponding classifier that
 * matches.
 *
 * @receiver The function whose value parameters are being checked.
 * @param classifiers A list of classifiers to compare against the function's value parameters.
 * @return `true` if the function's value parameters match the given classifiers in both size and
 * type, `false` otherwise.
 */
infix fun KFunction<*>.valueParametersEqual(classifiers: List<KClassifier>): Boolean {
  return valueParameters.run {
    size == classifiers.size && zip(classifiers).all { types ->
      val (parameter, classifier) = types
      parameter.type.classifier == classifier
    }
  }
}

/**
 * Checks if the return type of the function matches the given classifier.
 *
 * This infix function compares the classifier of the function's return type with the given
 * classifier.
 *
 * @receiver The function whose return type is being checked.
 * @param classifier The classifier to compare against the function's return type.
 * @return `true` if the function's return type matches the given classifier, `false` otherwise.
 */
infix fun KFunction<*>.returnTypeEquals(classifier: KClassifier): Boolean {
  return returnType.classifier == classifier
}

/**
 * Checks if the declaring class of the function matches the given classifier.
 *
 * This infix function compares the classifier of the callable's instance parameter type with the
 * given classifier, indicating whether the callable belongs to the specified class.
 *
 * @receiver The callable whose declaring class is being checked.
 * @param classifier The classifier to compare against the function's declaring class.
 * @return `true` if the callable's declaring class matches the given classifier, `false` otherwise.
 */
infix fun KCallable<*>.declaringClassEquals(classifier: KClassifier): Boolean {
  return instanceParameter?.type?.classifier == when {
    classifier is KClass<*> && !classifier.isCompanion -> classifier.companionObject ?: classifier
    else -> classifier
  }
}

/**
 * Checks if the extension class of the function matches the given classifier.
 *
 * This infix function compares the classifier of the function's extension parameter type with the
 * given classifier, indicating whether the function extends the specified class.
 *
 * @receiver The function whose extension class is being checked.
 * @param classifier The classifier to compare against the function's extension class.
 * @return `true` if the function's extension class matches the given classifier, `false` otherwise.
 */
infix fun KFunction<*>.extensionClassEquals(classifier: KClassifier?): Boolean {
  return extensionParameter?.type?.classifier == classifier
}

/**
 * Finds a constructor in this [KClass] that matches the provided parameter types.
 *
 * This function attempts to find a constructor that matches the given parameter types.
 * If no such constructor is found, a [NoSuchFunctionException] is thrown.
 *
 * @param types The parameter types for the constructor to find.
 * @return The found constructor of type [KConstructor].
 * @throws NoSuchFunctionException if no matching constructor is found.
 */
@Throws(NoSuchFunctionException::class)
fun KotlinClass.constructor(vararg types: KotlinClass): KConstructor {
  return constructorOrNull(*types) ?: throw NoSuchFunctionException(
    "constructor with parameters ${types.signature()} not found in $name"
  )
}

/**
 * Attempts to find a constructor in this [KClass] that matches the provided parameter types.
 *
 * This function searches for a constructor with the given parameter types. If found, the matching
 * constructor is returned. If no such constructor is found, `null` is returned.
 *
 * @param types The parameter types for the constructor to find.
 * @return The found constructor of type [KConstructor], or `null` if not found.
 */
fun KotlinClass.constructorOrNull(vararg types: KClassifier): KConstructor? {
  return constructors.find { constructor ->
    constructor.parameters.run {
      size == types.size && zip(types)
        .all { (param: KParameter, type: KClassifier) ->
          param.type.classifier == type
        }
    }
  }?.apply { accessible = true }
}

/**
 * Finds an extension function in this [KClass] that matches the specified receiver type, name,
 * and parameter types, returning it if found, or `null` otherwise.
 *
 * @param receiver The class type of the extension function's receiver.
 * @param name The name of the extension function to find.
 * @param types The parameter types of the extension function.
 * @return The matching [KFunction] or `null` if no match is found.
 */
fun KotlinClass.extensionFunctionOrNull(
  receiver: KotlinClass,
  name: String,
  vararg types: KClassifier
): KFunction<*>? = find(
  functionPredicates {
    name(name) and extensionOf(receiver) and parameters(*types) and setAccessible()
  }
)?.apply { isAccessible = true }

/**
 * Finds an extension function in this [KClass] that matches the specified receiver type, name,
 * and parameter types, throwing [NoSuchFunctionException] if no match is found.
 *
 * @param receiver The class type of the extension function's receiver.
 * @param name The name of the extension function to find.
 * @param types The parameter types of the extension function.
 * @return The matching [KFunction].
 * @throws NoSuchFunctionException if no matching function is found.
 */
@Throws(NoSuchFunctionException::class)
fun KotlinClass.extensionFunction(
  receiver: KotlinClass,
  name: String,
  vararg types: KClassifier
): KFunction<*> = extensionFunctionOrNull(receiver, name, *types) ?: throw NoSuchFunctionException(
  "fun ${receiver.name}.$name${types.signature()}' not found in '${this.name}'"
)

/**
 * Retrieves a top-level function by its [name] and parameter types [types] from this `KotlinClass`.
 * Throws a `NoSuchFunctionException` if the function is not found.
 *
 * This function performs a search for top-level functions that match the specified name and types
 * in the current `KotlinClass` or in its `topLevelClass` if applicable. The lookup is limited to
 * accessible functions and supports both Kotlin and Java functions.
 *
 * @param name The name of the top-level function to search for.
 * @param types The parameter types expected for the function.
 * @return The matching `KFunction` if found.
 * @throws NoSuchFunctionException if no matching function is found.
 */
fun KotlinClass.topLevelFunction(name: String, vararg types: KClassifier): KFunction<*> =
  topLevelFunctionOrNull(name, *types) ?: throw NoSuchFunctionException(
    "fun ${(topLevelClass ?: this).name} $name${types.signature()}' not found in '${this.name}'"
  )

/**
 * Attempts to retrieve a top-level function by its [name] and parameter types [types] from this
 * `KotlinClass`, returning `null` if not found.
 *
 * This function checks if the class is a top-level class or if it has a [topLevelClass], then
 * searches for the specified function within those contexts. It also supports searching within
 * all top-level application classes if necessary. Only accessible functions are considered.
 *
 * @param name The name of the function to search for.
 * @param types The expected parameter types of the function.
 * @return The matching `KFunction` if found, or `null` if no such function exists.
 */
fun KotlinClass.topLevelFunctionOrNull(name: String, vararg types: KClassifier): KFunction<*>? =
  tryOrNull {
    fun JavaClass.findFunction(): KFunction<*>? = tryOrNull {
      functionPredicates {
        name(name) and parameters(*types) and setAccessible()
      }.let { predicate ->
        declaredMethods.firstNotNullOfOrNull { method ->
          method.kotlinFunction?.takeIf { func -> predicate.test(func) }
        }
      }
    }

    when (val javaClass: JavaClass? = if (isTopLevelClass()) java else topLevelClass?.java) {
      null -> allTopLevelApplicationClasses.firstNotNullOfOrNull { kClass ->
        kClass.java.findFunction()
      }

      else -> javaClass.findFunction()
    }
  }

/**
 * Finds a top-level extension function in this [KClass] that matches the specified receiver type,
 * name, and parameter types, returning it if found, or `null` otherwise.
 *
 * @param receiver The class type of the extension function's receiver.
 * @param name The name of the top-level extension function to find.
 * @param types The parameter types of the extension function.
 * @return The matching top-level [KFunction] or `null` if no match is found.
 */
fun KotlinClass.topLevelExtensionFunctionOrNull(
  receiver: KClassifier,
  name: String,
  vararg types: KClassifier
): KFunction<*>? = tryOrNull {
  fun JavaClass.findExtensionFunction(): KFunction<*>? = tryOrNull {
    functionPredicates {
      name(name) and extensionOf(receiver) and parameters(*types) and setAccessible()
    }.let { predicate ->
      declaredMethods.firstNotNullOfOrNull { method ->
        method.kotlinFunction?.takeIf { func -> predicate.test(func) }
      }
    }
  }

  when (val javaClass: JavaClass? = if (isTopLevelClass()) java else topLevelClass?.java) {
    null -> allTopLevelApplicationClasses.firstNotNullOfOrNull { kClass ->
      kClass.java.findExtensionFunction()
    }

    else -> javaClass.findExtensionFunction()
  }
}

/**
 * Finds a top-level extension function in this [KClass] that matches the specified receiver type,
 * name, and parameter types, throwing [NoSuchFunctionException] if no match is found.
 *
 * @param receiver The class type of the extension function's receiver.
 * @param name The name of the top-level extension function to find.
 * @param types The parameter types of the extension function.
 * @return The matching top-level [KFunction].
 * @throws NoSuchFunctionException if no matching function is found.
 */
@Throws(NoSuchFunctionException::class)
fun KotlinClass.topLevelExtensionFunction(
  receiver: KClassifier,
  name: String,
  vararg types: KClassifier
): KFunction<*> = topLevelExtensionFunctionOrNull(receiver, name, *types)
  ?: throw NoSuchFunctionException(
    "'fun ${receiver.name}.$name${types.signature()}' not found in '${this.name}'"
  )

/**
 * Retrieves a property by name.
 * Throws an [NoSuchPropertyException] if the property is not found.
 *
 * @receiver The class to retrieve the property for.
 * @param name The property name.
 * @return The property.
 * @throws NoSuchPropertyException if the property was not found in the class hierarchy
 */
@Throws(NoSuchPropertyException::class)
infix fun KotlinClass.property(name: String): KProperty<*> = propertyOrNull(name)
  ?: throw NoSuchPropertyException("${this.name}.$name")

/**
 * Retrieves a property by name, or null if not found.
 *
 * @receiver The class to retrieve the property for.
 * @param name The property name.
 * @return The property or null if not found.
 */
fun KotlinClass.propertyOrNull(name: String): KProperty<*>? {
  return (
    memberProperties.firstOrNull { it.name == name }
      ?: staticProperties.firstOrNull { it.name == name }
    )?.apply { accessible = true }
}

/**
 * Operator function to invoke a [Member], either a [Field], [Method], or [Constructor],
 * on the provided [obj] with optional arguments [args].
 * This is a shorthand for invoking the [access] method on the [Member].
 *
 * @param obj   The instance of the object on which the member will be invoked.
 *              Use `null` for static members.
 * @param args  Optional arguments required for the member invocation
 *              (used for methods and constructors).
 * @return The result of the field access, method invocation, or constructor call.
 * @throws UnsupportedOperationException if the [Member] is not a supported type.
 */
operator fun Member.get(obj: Any?, vararg args: Any?): Any? = access(obj, *args)

/**
 * Accesses the value of a [Field], invokes a [Method], or creates an instance via a [Constructor].
 * Handles both static and instance members appropriately.
 *
 * @param obj   The instance of the object on which the member will be invoked.
 *              For static members, pass `null`.
 * @param args  Optional arguments required for the member invocation (for methods and constructors)
 * @return The result of the field access, method invocation, or constructor call.
 * @throws UnsupportedOperationException if the [Member] is not a valid type.
 */
fun Member.access(obj: Any?, vararg args: Any?): Any? {
  return when (this) {
    is Field -> get(obj.takeIf { isNotStatic })
    is Method -> invoke(obj.takeIf { isNotStatic }, *args)
    is Constructor<*> -> newInstance(*args)
    else -> throw UnsupportedOperationException("${this::class.name} is not supported.")
  }
}

/**
 * Gets or sets the static value of a [Field]. If the field is static, it retrieves the value
 * using `get(null)`. If setting a value, it assigns the given value to the field using the
 * field's generic type.
 *
 * @return The value of the static field or `null` if the field is not static.
 */
var Field.staticValue
  get() = if (isStatic) get(null) else null
  set(value) {
    assign(genericType, value)
  }

/**
 * Gets or sets the accessibility of an accessible object.
 */
@Suppress("DEPRECATION")
var AccessibleObject.accessible: Boolean
  get() = isAccessible
  set(value) {
    try {
      if (isAccessible != value) {
        isAccessible = value
      }
    } catch (_: InaccessibleObjectException) {
    } catch (_: SecurityException) {
    }
  }

/**
 * Gets or sets the accessibility of a [KCallable].
 */
var KCallable<*>.accessible: Boolean
  get() = isAccessible
  set(value) {
    try {
      if (isAccessible != value) {
        isAccessible = value
      }
    } catch (_: InaccessibleObjectException) {
    } catch (_: SecurityException) {
    }
  }

/**
 * Retrieves the memory offset of the [Field], either as a static or instance field.
 * If the field is static, it calls [TheUnsafe.staticFieldOffset], otherwise, it uses
 * [TheUnsafe.objectFieldOffset].
 *
 * @return The memory offset of the field.
 */
val Field.offset: Long
  get() = when (isStatic) {
    true -> TheUnsafe.staticFieldOffset(this)
    else -> TheUnsafe.objectFieldOffset(this)
  }

/**
 * Assigns an [value] to the specified [obj] for this [Field], handling both volatile and static
 * fields. If the field is volatile, static, or final, it uses [TheUnsafe.putObjectVolatile],
 * otherwise it uses [TheUnsafe.putObject].
 *
 * @param obj The object containing the field to modify, or `null` for static fields.
 * @param value The value to assign to the field.
 */
fun Field.putObject(obj: Any?, value: Any?) = attempt {
  when (isVolatile || isStatic || isFinal) {
    true -> TheUnsafe.putObjectVolatile(obj, offset, value)
    else -> TheUnsafe.putObject(obj, offset, value)
  }
}

/**
 * Sets a final [Field] to a new [value] for the provided [obj]. Temporarily removes the final
 * modifier, assigns the value, and then restores the original modifiers. Returns `true` if the
 * operation succeeded, `false` otherwise.
 *
 * @param obj The object containing the field to modify, or `null` for static fields.
 * @param value The value to assign to the final field.
 * @return `true` if the value was successfully set, `false` otherwise.
 */
fun Field.setFinal(obj: Any?, value: Any?): Boolean = attempt {
  isAccessible = true
  val original = modifiers
  val nonFinal = modifiers and Modifier.FINAL.inv()
  val modField = jClass.field("modifiers")
  modField.setInt(this, nonFinal)
  set(obj, value)
  modField.set(this, original)
}

/**
 * Assigns a [value] to a [Field] for the specified [obj], taking into account whether the field is
 * final or not. If the field is final, it uses the [setFinal] function or falls back to
 * [putObject] if reflection is restricted.
 *
 * @param obj The object containing the field to modify, or `null` for static fields.
 * @param value The value to assign to the field.
 */
fun Field.assign(obj: Any?, value: Any?) = attempt {
  try {
    if (canAccess(obj)) {
      try {
        set(obj.takeIf { isNotStatic }, value)
        return@attempt
      } catch (_: IllegalAccessException) {
      }
    }
  } catch (_: IllegalArgumentException) {
  }
  if (isFinal) {
    if (isReflectionBlocked) {
      putObject(obj, value)
    } else {
      setFinal(obj, value) || putObject(obj, value)
    }
  } else {
    set(obj.takeIf { isNotStatic }, value)
  }
}

/**
 * Calls this property's [setter][KMutableProperty.setter] with the [receiver] and [value] or,
 * if the [KProperty] is not mutable, sets the value using the java field corresponding to the
 * provided property.
 *
 * @param receiver The object containing the property to modify, or null for top-level properties.
 * @param value The value to assign to the property.
 */
operator fun KProperty<*>.set(receiver: Any?, value: Any?) {
  if (this is KMutableProperty<*>) {
    accessible = true
    setter.call(receiver, value)
  } else {
    javaField?.let { field ->
      if (field.genericType == Lazy::class.java) {
        field.assign(receiver, lazy { value })
      } else if (field.isStatic) {
        field.staticValue = value
      } else {
        field.assign(receiver, value)
      }
    }
  }
}

/**
 * Retrieves a declared annotation of type [A].
 *
 * @receiver The java class to retrieve the annotation class for.
 * @param A the type of the annotation to query for and return if present
 * @return this element's annotation for the specified annotation type if present, else null
 */
@Throws(IllegalArgumentException::class)
inline fun <reified A : Annotation> JavaClass.annotation(): A =
  requireNotNull(annotationOrNull())

/**
 * Retrieves a declared annotation of type [A].
 *
 * @receiver The java class to retrieve the annotation class for.
 * @param A the type of the annotation to query for and return if present
 * @return this element's annotation for the specified annotation type if present, else null
 */
inline fun <reified A : Annotation> JavaClass.annotationOrNull(): A? =
  getAnnotation(A::class.java)

/**
 * Returns a parameter representing the extension receiver instance needed to call this callable,
 * or `null` if this callable is not an extension.
 */
val KCallable<*>.extensionParameter: KParameter?
  get() = parameters.firstOrNull { param -> param.kind == KParameter.Kind.EXTENSION_RECEIVER }

/**
 * Returns true if the callable is a top-level callable.
 */
val KCallable<*>.isTopLevel: Boolean
  get() = (instanceParameter?.type?.classifier as? KotlinClass)?.isTopLevelClass() ?: true

/**
 * Retrieves the instance parameter of the callable, if it exists.
 *
 * This property returns the parameter of kind `INSTANCE` from the callable's parameter list,
 * which typically represents the instance on which the callable is invoked
 * (e.g., `this` in member callables).
 *
 * @receiver The callable whose instance parameter is being retrieved.
 * @return The instance parameter if present, or `null` if the callable is not a member function.
 */
val KCallable<*>.instanceParameter: KParameter?
  get() = parameters.find { param -> param.kind == KParameter.Kind.INSTANCE }

/**
 * Extension property to check whether a [KFunction] is a constructor.
 *
 * This property checks if the function is part of the constructors for the class of its return type
 *
 * @return `true` if the function is a constructor, `false` otherwise.
 */
val KFunction<*>.isConstructor: Boolean
  get() = (returnType.classifier as? KotlinClass)?.constructors?.contains(this) ?: false

/**
 * Extension property to check whether a [callable][KCallable] (function or property)
 * is an extension function.
 *
 * @return `true` if the callable is an extension member, `false` otherwise.
 */
val KCallable<*>.isExtension: Boolean
  get() = parameters.any { param -> param.kind == KParameter.Kind.EXTENSION_RECEIVER }

/**
 * Extension property to check whether a [callable][KCallable] (function or property) is a member
 * of a companion object.
 *
 * @return `true` if the callable is declared in a companion object, `false` otherwise.
 */
val KCallable<*>.isCompanion: Boolean
  get() = (instanceParameter?.type?.classifier as? KotlinClass)?.isCompanion == true

/**
 * Retrieves the declaring Java class for this [KCallable].
 *
 * This property returns the Java class in which this callable is declared.
 * For instance members, it uses the [instanceParameter]'s type classifier.
 * For top-level functions and properties, it attempts to resolve the Java method or field
 * backing the callable, if available.
 *
 * Note: This may return `null` if the callable is a standalone top-level function or property
 * without an associated Java declaration, as they do not have a conventional declaring class.
 *
 * @return The [Class] object representing the declaring class, or `null` if the callable does
 *         not have one.
 */
val KCallable<*>.declaringClass: Class<*>? get() {
  // Try to get the instance parameter's classifier if it exists
  (instanceParameter?.type?.classifier as? KClass<*>)?.let { return it.java }

  // If no instance parameter, attempt to get the Kotlin class directly
  return (this as? KFunction<*>)?.javaMethod?.declaringClass
    ?: (this as? KProperty<*>)?.javaField?.declaringClass
}

/**
 * Calls this function with the specified list of arguments and returns the result.
 *
 * @param args The arguments for the function.
 * @throws ReflectiveOperationException If the number of specified arguments is not equal to the
 * size of [KCallable.parameters], or if their types do not match the types of the parameters.
 */
@Suppress("TooGenericExceptionCaught")
@Throws(ReflectiveOperationException::class)
operator fun KFunction<*>.invoke(vararg args: Any?): Any? = try {
  call(*args)
} catch (e: IllegalArgumentException) {
  when (val obj = companionObjectInstance()) {
    null -> throw ReflectiveOperationException(e)
    else -> call(obj, *args) // implicit companion for functions declared in a companion object
  }
} catch (e: Exception) {
  throw ReflectiveOperationException(e)
}

/**
 * Calls this property with the specified receiver and returns the result.
 *
 * @receiver The property to retrieve.
 * @param receiver The owner of the property.
 * @throws ReflectiveOperationException If an error occurs while calling the property.
 */
@Suppress("TooGenericExceptionCaught")
operator fun KProperty<*>.invoke(receiver: Any): Any? = try {
  call(receiver)
} catch (e: Exception) {
  throw ReflectiveOperationException(e)
}

/**
 * Converts an array of objects (arguments) to an array of corresponding [KClassifier] types.
 *
 * This extension property is used to map the types of the elements in the array to their
 * corresponding Kotlin class representations (`KClassifier`). If an element in the array is `null`,
 * it is mapped to `Any::class` to represent the absence of type information.
 *
 * @receiver An array of arguments, which can include `null` values.
 * @return An array of `KClassifier` objects representing the types of the arguments.
 *         - If an argument is non-null, the type is determined by `arg::class`.
 *         - If an argument is `null`, it is mapped to `Any::class`.
 *
 * Example:
 * ```
 * val args: Array<out Any?> = arrayOf("String", 42, null)
 * val types: Array<out KClass<*>> = args.kotlinParameterTypes
 * // types: [String::class, Int::class, Any::class]
 * ```
 */
fun NullableArgs.toKParameterTypes(): KParameterTypes {
  return when {
    all { arg -> arg is KClassifier } -> filterIsInstance<KClassifier>().toTypedArray()
    else -> map { arg -> if (arg != null) arg::class else Any::class }.toTypedArray()
  }
}

/**
 * Converts an array of objects (arguments) to an array of corresponding `Class<*>` types.
 *
 * This extension property is used to map the types of the elements in the array to their
 * corresponding Java class representations (`Class<*>`). If an element in the array is `null`,
 * it is mapped to `java.lang.Object` to represent the absence of type information.
 *
 * @receiver An array of arguments, which can include `null` values.
 * @return An array of `Class<*>` objects representing the types of the arguments.
 *         - If an argument is non-null, the type is determined by `arg::class.java`.
 *         - If an argument is `null`, it is mapped to `Any::class.java`.
 */
fun NullableArgs.toJavaParameterTypes(): ParameterTypes = map { arg ->
  when (arg) {
    null -> Any::class.java
    is KClass<*> -> arg.java
    else -> arg::class.javaPrimitiveType ?: arg::class.java
  }
}.toTypedArray()

// -------------------------------------------------------------------------------------------------

private const val ACCESS_MODIFIERS = Modifier.PROTECTED or Modifier.PUBLIC or Modifier.PRIVATE

/**
 * Checks if this member is public.
 *
 * @return `true` if the member is public; `false` otherwise.
 * @see Modifier.isPublic
 */
val Member.isPublic: Boolean
  get() = Modifier.isPublic(modifiers)

/**
 * Checks if this member is neither public, protected, nor private.
 *
 * @return `true` if the member is neither public, protected, nor private.
 */
val Member.isPackage: Boolean
  get() = (modifiers and ACCESS_MODIFIERS) == 0

/**
 * Checks if this member is private.
 *
 * @return `true` if the member is private; `false` otherwise.
 * @see Modifier.isPrivate
 */
val Member.isPrivate: Boolean
  get() = Modifier.isPrivate(modifiers)

/**
 * Checks if this member is protected.
 *
 * @return `true` if the member is protected; `false` otherwise.
 * @see Modifier.isProtected
 */
val Member.isProtected: Boolean
  get() = Modifier.isProtected(modifiers)

/**
 * Checks if this member is static.
 *
 * @return `true` if the member is static; `false` otherwise.
 * @see Modifier.isStatic
 */
val Member.isStatic: Boolean
  get() = Modifier.isStatic(modifiers)

/**
 * Checks if this member is not static.
 *
 * @return `true` if the member is not static; `false` otherwise.
 * @see Modifier.isStatic
 * @see isStatic
 */
val Member.isNotStatic: Boolean
  get() = !Modifier.isStatic(modifiers)

/**
 * Checks if this member is final.
 *
 * @return `true` if the member is final; `false` otherwise.
 * @see Modifier.isFinal
 */
val Member.isFinal: Boolean
  get() = Modifier.isFinal(modifiers)

/**
 * Checks if this member is synchronized.
 *
 * @return `true` if the member is synchronized; `false` otherwise.
 * @see Modifier.isSynchronized
 */
val Member.isSynchronized: Boolean
  get() = Modifier.isSynchronized(modifiers)

/**
 * Checks if this member is volatile.
 *
 * @return `true` if the member is volatile; `false` otherwise.
 * @see Modifier.isVolatile
 */
val Member.isVolatile: Boolean
  get() = Modifier.isVolatile(modifiers)

/**
 * Checks if this member is transient.
 *
 * @return `true` if the member is transient; `false` otherwise.
 * @see Modifier.isTransient
 */
val Member.isTransient: Boolean
  get() = Modifier.isTransient(modifiers)

/**
 * Checks if this member is native.
 *
 * @return `true` if the member is native; `false` otherwise.
 * @see Modifier.isNative
 */
val Member.isNative: Boolean
  get() = Modifier.isNative(modifiers)

/**
 * Checks if this member is an interface.
 *
 * @return `true` if the member is an interface; `false` otherwise.
 * @see Modifier.isInterface
 */
val Member.isInterface: Boolean
  get() = Modifier.isInterface(modifiers)

/**
 * Checks if this member is abstract.
 *
 * @return `true` if the member is abstract; `false` otherwise.
 * @see Modifier.isAbstract
 */
val Member.isAbstract: Boolean
  get() = Modifier.isAbstract(modifiers)

/**
 * Checks if this member is strict.
 *
 * @return `true` if the member is strict; `false` otherwise.
 * @see Modifier.isStrict
 */
val Member.isStrict: Boolean
  get() = Modifier.isStrict(modifiers)

// -------------------------------------------------------------------------------------------------

/**
 * Converts a member ([Field], [Method], or [Constructor]) to its signature representation,
 * including modifiers, annotations, type parameters, return type (if applicable), and parameters.
 *
 * @receiver The member whose signature is to be generated.
 * @return A string representing the full signature of the member.
 */
inline fun <reified T> T.signature(): String where T : AccessibleObject, T : Member {
  val sb = StringBuilder()
  if (annotations.isNotEmpty()) {
    sb.append(annotations.signature()).append('\n')
  }
  if (modifiers != 0) {
    sb.append(modifiersSignature()).append(' ')
  }
  if (this is Executable && typeParameters.isNotEmpty()) {
    sb.append(typeParameters.signature()).append(' ')
  }
  when (this) {
    is Field -> sb.append(genericType.typeName).append(' ')
    is Method -> sb.append(genericReturnType.typeName).append(' ')
  }
  sb.append(declaringClass.typeName)
  if (this !is Constructor<*>) {
    sb.append('.').append(name)
  }
  if (this is Executable) {
    sb.append(parameters.signature(isVarArgs))
    if (exceptionTypes.isNotEmpty()) {
      sb.append(' ').append(exceptionTypes.joinToString { "throws ${it.typeName}" })
    }
  }
  return sb.toString()
}

/**
 * Converts an array of parameter types to a string representation for use in method signatures.
 *
 * @receiver An array of parameter types.
 * @return A string representing the parameter types in method signature format.
 */
fun ParameterTypes.signature(): String = "(${joinToString { p -> p.typeName }})"

/**
 * Converts an array of parameter types to a string representation for use in function signatures.
 *
 * @receiver An array of parameter types.
 * @return A string representing the parameter types in function signature format.
 */
fun KParameterTypes.signature(): String = "(${joinToString { p -> p.name }})"

/**
 * Converts an array of generic parameter types to a string representation for use in method
 * signatures. Handles varargs by replacing the last parameter type's array notation with ellipsis.
 *
 * @receiver An array of generic parameter types.
 * @param isVarArgs Whether the last parameter is a vararg.
 * @return A string representing the generic parameter types in method signature format.
 */
fun GenericParameterTypes.signature(isVarArgs: Boolean): String {
  return joinToString(prefix = "(", postfix = ")") { type ->
    when {
      isVarArgs && last() == type -> type.typeName.replace("[]", "...")
      else -> type.typeName
    }
  }
}

/**
 * Converts an array of parameters to a string representation, including their types and names.
 * Handles varargs by replacing the last parameter's array notation with ellipsis.
 *
 * @receiver An array of parameters.
 * @param isVarArgs Whether the last parameter is a vararg.
 * @return A string representing the parameters in method signature format.
 */
fun Parameters.signature(isVarArgs: Boolean): String {
  return joinToString(prefix = "(", postfix = ")") { param ->
    when {
      isVarArgs && last() == param -> param.type.typeName.replace("[]", "...")
      else -> param.type.typeName
    } + " " + param.name
  }
}

/**
 * Converts an array of annotations to a string representation.
 *
 * @receiver An array of annotations.
 * @return A string representing the annotations in Java annotation format.
 */
fun Annotations.signature(): String =
  joinToString("\n") { '@' + it.annotationClass.java.name }

/**
 * Converts an array of type parameters to a string representation.
 * If a type parameter has bounds, it includes the bounds in the output.
 *
 * @receiver An array of type parameters.
 * @return A string representing the type parameters in generic type format.
 */
fun TypeParameters.signature(): String = when {
  isEmpty() -> ""
  else -> joinToString(", ", "<", ">") { type ->
    when {
      type.bounds.size == 1 && type.bounds[0] == Any::class.java -> type.name
      else -> type.name + " extends " + type.bounds.joinToString(" & ") { it.typeName }
    }
  }
}

/**
 * Converts the modifiers of a member (field, method, or constructor) to a string representation.
 *
 * @receiver The member whose modifiers are to be converted to a string.
 * @return A string representing the modifiers of the member.
 * @see Modifier.toString
 */
fun Member.modifiersSignature(): String = when (this) {
  is Executable -> {
    var mod = modifiers and when (this) {
      is Method -> Modifier.methodModifiers()
      is Constructor<*> -> Modifier.constructorModifiers()
      else -> 0
    }
    val isDefaultMethod = this is Method && isDefault
    when {
      mod != 0 && !isDefaultMethod -> Modifier.toString(mod)
      else -> buildString {
        val accessMod = mod and ACCESS_MODIFIERS
        if (accessMod != 0) append(Modifier.toString(accessMod)).append(' ')
        if (isDefaultMethod) append("default ")
        mod = (mod and ACCESS_MODIFIERS.inv())
        if (mod != 0) append(Modifier.toString(mod))
      }
    }
  }

  else -> Modifier.toString(modifiers)
}

// -------------------------------------------------------------------------------------------------

/**
 * Finds the first member (method, field, or constructor) of the given class that matches
 * the provided [predicate].
 *
 * @param T The accessible member to find.
 * @param predicate The condition to evaluate on each member of the class.
 * @return The first matching member, or `null` if none found.
 * @throws UnsupportedOperationException If the member type is unsupported.
 */
inline fun <reified T> JavaClass.find(predicate: Predicate<T>): T?
  where T : AccessibleObject, T : Member = traverser.traverseFirstOrNullOf {
  when (T::class) {
    Method::class -> declaredMethods.find { method -> predicate(method as T) }
    Field::class -> declaredFields.find { field -> predicate(field as T) }
    Constructor::class -> declaredConstructors.find { constructor -> predicate(constructor as T) }
    Executable::class -> declaredConstructors.find { constructor -> predicate(constructor as T) }
      ?: declaredMethods.find { method -> predicate(method as T) }

    else -> throw UnsupportedOperationException("${T::class.java.name} is not supported.")
  }
} as? T

/**
 * Finds the first member (method, field, or constructor) in the [KClass] that matches
 * the provided [predicate].
 *
 * @param T The accessible member to find.
 * @param predicate The condition to evaluate on each member of the class.
 * @return The first matching member, or `null` if none found.
 */
inline fun <reified T> KotlinClass.find(crossinline predicate: T.() -> Boolean): T?
  where T : AccessibleObject, T : Member = java.find { obj -> predicate.invoke(obj) }

/**
 * Finds the first member (method, field, or constructor) in the [KClass] that matches
 * the provided [predicate].
 *
 * @param T The accessible member to find.
 * @param predicate The condition to evaluate on each member of the class.
 * @return The first matching member, or `null` if none found.
 */
inline fun <reified T> KotlinClass.find(predicate: Predicate<T>): T?
  where T : AccessibleObject, T : Member = java.find(predicate)

/**
 * Finds the first callable (function, or member) of the given class that matches
 * the provided [predicate].
 *
 * @param T The Kotlin callable to find.
 * @param predicate The condition to evaluate on each callable of the class.
 * @return The first matching callable, or `null` if none found.
 * @throws IllegalStateException If the callable type is unsupported.
 */
inline fun <reified T : KCallable<*>> KotlinClass.find(predicate: Predicate<T>): T? {
  return when (T::class) {
    KFunction::class -> functionTraverser
    KProperty::class -> propertyTraverser
    KCallable::class -> callableTraverser
    else -> error("${T::class.name} is not supported.")
  }.firstOrNull { member ->
    member is T && predicate(member)
  } as? T
}

/**
 * Finds the first member (method, field, or constructor) in the [Class] that matches
 * the provided [predicate].
 *
 * @param T The accessible member to find.
 * @param predicate The condition to evaluate on each member of the class.
 * @return The first matching member, or `null` if none found.
 */
inline fun <reified T> JavaClass.find(crossinline predicate: T.() -> Boolean): T?
  where T : AccessibleObject, T : Member = find { obj -> predicate.invoke(obj) }

/**
 * Finds the first field in the [KClass] that matches the provided [FieldPredicates] block.
 *
 * @param block A block of field predicates to test on each field.
 * @return The first matching field, or `null` if none found.
 */
fun KotlinClass.findField(
  block: FieldPredicates.() -> Predicate<Field>
): Field? = java.find(fieldPredicates(block))

/**
 * Finds the first field in the [KClass] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each field.
 * @return The first matching field, or `null` if none found.
 */
fun KotlinClass.findField(predicate: Predicate<Field>): Field? = java.find(predicate)

/**
 * Finds the first field in the [Class] that matches the provided [FieldPredicates] block.
 *
 * @param block A block of field predicates to test on each field.
 * @return The first matching field, or `null` if none found.
 */
fun JavaClass.findField(
  block: FieldPredicates.() -> Predicate<Field>
): Field? = find(fieldPredicates(block))

/**
 * Finds the first field in the [Class] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each field.
 * @return The first matching field, or `null` if none found.
 */
fun JavaClass.findField(predicate: Predicate<Field>): Field? = find(predicate)

/**
 * Finds the first method in the [KClass] that matches the provided [MethodPredicates] block.
 *
 * @param block A block of method predicates to test on each method.
 * @return The first matching method, or `null` if none found.
 */
fun KotlinClass.findMethod(
  block: MethodPredicates.() -> Predicate<Method>
): Method? = java.find(methodPredicates(block))

/**
 * Finds the first method in the [KClass] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each method.
 * @return The first matching method, or `null` if none found.
 */
fun KotlinClass.findMethod(predicate: Predicate<Method>): Method? = java.find(predicate)

/**
 * Finds the first method in the [Class] that matches the provided [MethodPredicates] block.
 *
 * @param block A block of method predicates to test on each method.
 * @return The first matching method, or `null` if none found.
 */
fun JavaClass.findMethod(
  block: MethodPredicates.() -> Predicate<Method>
): Method? = find(methodPredicates(block))

/**
 * Finds the first method in the [Class] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each method.
 * @return The first matching method, or `null` if none found.
 */
fun JavaClass.findMethod(predicate: Predicate<Method>): Method? = find(predicate)

/**
 * Finds the first constructor in the [KClass] that matches the provided [ConstructorPredicates]
 * block.
 *
 * @param block A block of constructor predicates to test on each constructor.
 * @return The first matching constructor, or `null` if none found.
 */
fun KotlinClass.findConstructor(
  block: KFunctionPredicates.() -> Predicate<KFunction<*>>
): KConstructor? = find(KFunctionPredicates.isConstructor() and functionPredicates(block))

/**
 * Finds the first constructor in the [KClass] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each constructor.
 * @return The first matching constructor, or `null` if none found.
 */
fun KotlinClass.findConstructor(
  predicate: Predicate<KConstructor>
): KConstructor? = findConstructor { predicate }

/**
 * Finds the first constructor in the [Class] that matches the provided [ConstructorPredicates]
 * block.
 *
 * @param block A block of constructor predicates to test on each constructor.
 * @return The first matching constructor, or `null` if none found.
 */
fun JavaClass.findConstructor(
  block: ConstructorPredicates.() -> Predicate<Constructor<*>>
): Constructor<*>? = find(constructorPredicates(block))

/**
 * Finds the first constructor in the [Class] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each constructor.
 * @return The first matching constructor, or `null` if none found.
 */
fun JavaClass.findConstructor(
  predicate: Predicate<Constructor<*>>
): Constructor<*>? = find(predicate)

/**
 * Finds the first function in the [KClass] that matches the provided [KFunctionPredicates] block.
 *
 * @param block A block of function predicates to test on each function.
 * @return The first matching function, or `null` if none found.
 */
fun KotlinClass.findFunction(
  block: KFunctionPredicates.() -> Predicate<KFunction<*>>
): KFunction<*>? = find(functionPredicates(block))

/**
 * Finds the first property in the [KClass] that matches the provided [KPropertyPredicates] block.
 *
 * @param block A block of property predicates to test on each property.
 * @return The first matching property, or `null` if none found.
 */
fun KotlinClass.findProperty(
  block: KPropertyPredicates.() -> Predicate<KProperty<*>>
): KProperty<*>? = find(propertyPredicates(block))

/**
 * Finds the first function in the [KClass] that matches the provided [predicate].
 *
 * @param predicate The condition to evaluate on each function.
 * @return The first matching function, or `null` if none found.
 */
fun KotlinClass.findFunction(
  predicate: Predicate<KFunction<*>>
): KFunction<*>? = find(predicate)

// -------------------------------------------------------------------------------------------------

/**
 * Filters members (methods, fields, constructors) of the class based on the provided [predicate].
 *
 * @param T The accessible member to filter.
 * @param predicate The condition to evaluate on each member of the class.
 * @return A list of members that match the provided predicate.
 * @throws UnsupportedOperationException If the member type is unsupported.
 */
inline fun <reified T> JavaClass.filter(predicate: Predicate<T>): List<T>
  where T : AccessibleObject, T : Member = buildList {
  traverse {
    addAll(
      when (T::class) {
        Method::class -> declaredMethods
        Field::class -> declaredFields
        Constructor::class -> declaredConstructors
        Executable::class -> buildList<Executable> {
          addAll(declaredConstructors)
          addAll(declaredMethods)
        }.toTypedArray()

        else -> throw UnsupportedOperationException("${T::class.java.name} is not supported.")
      }.filterIsInstance<T>().filter { member -> predicate(member) }
    )
  }
}

/**
 * Filters members (methods, fields, constructors) of the class based on the provided inline
 * [predicate].
 *
 * @param T The type of the accessible member to filter.
 * @param predicate The condition to evaluate on each member of the class.
 * @return A list of members that match the provided predicate.
 */
inline fun <reified T> JavaClass.filter(crossinline predicate: T.() -> Boolean): List<T>
  where T : AccessibleObject, T : Member = filter { obj -> predicate.invoke(obj) }

/**
 * Filters callables (functions and properties) of the class based on the provided [predicate].
 *
 * @param T The type of the kotlin callable to filter.
 * @param predicate The condition to evaluate on each callable of the class.
 * @return A list of callables that match the provided predicate.
 * @throws UnsupportedOperationException If the callable type is unsupported.
 */
inline fun <reified T : KCallable<*>> KotlinClass.filter(predicate: Predicate<T>): List<T> {
  return when (T::class) {
    KFunction::class -> allFunctions
    KProperty::class -> allProperties
    else -> throw UnsupportedOperationException("${T::class.java.name} is not supported.")
  }.filterIsInstance<T>().filter { callable -> predicate(callable) }
}

/**
 * Filters callables (functions and properties) of the [KClass] based on the provided [predicate].
 *
 * @param T The type of the kotlin callable to filter.
 * @param predicate The condition to evaluate on each callable of the class.
 * @return A list of callables that match the provided predicate.
 */
inline fun <reified T : KCallable<*>> KotlinClass.filter(
  crossinline predicate: T.() -> Boolean
): List<T> = when (T::class) {
  KFunction::class -> allFunctions
  KProperty::class -> allProperties
  else -> throw UnsupportedOperationException("${T::class.java.name} is not supported.")
}.filterIsInstance<T>().filter { callable -> predicate(callable) }

/**
 * Filters fields of the class based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each field.
 * @return A list of fields that match the provided predicate.
 */
fun JavaClass.filterFields(predicate: Predicate<Field>): List<Field> = filter(predicate)

/**
 * Filters fields of the [KClass] based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each field.
 * @return A list of fields that match the provided predicate.
 */
fun KotlinClass.filterFields(predicate: Predicate<Field>): List<Field> = java.filter(predicate)

/**
 * Filters fields of the class based on the provided [FieldPredicates] block.
 *
 * @param block A block of field predicates to test on each field.
 * @return A list of fields that match the provided block of predicates.
 */
fun JavaClass.filterFields(block: FieldPredicates.() -> Predicate<Field>): List<Field> =
  filter(fieldPredicates(block))

/**
 * Filters fields of the [KClass] based on the provided [FieldPredicates] block.
 *
 * @param block A block of field predicates to test on each field.
 * @return A list of fields that match the provided block of predicates.
 */
fun KotlinClass.filterFields(block: FieldPredicates.() -> Predicate<Field>): List<Field> =
  java.filter(fieldPredicates(block))

/**
 * Filters methods of the class based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each method.
 * @return A list of methods that match the provided predicate.
 */
fun JavaClass.filterMethods(predicate: Predicate<Method>): List<Method> = filter(predicate)

/**
 * Filters methods of the [KClass] based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each method.
 * @return A list of methods that match the provided predicate.
 */
fun KotlinClass.filterMethods(predicate: Predicate<Method>): List<Method> = java.filter(predicate)

/**
 * Filters methods of the class based on the provided [MethodPredicates] block.
 *
 * @param block A block of method predicates to test on each method.
 * @return A list of methods that match the provided block of predicates.
 */
fun JavaClass.filterMethods(block: MethodPredicates.() -> Predicate<Method>): List<Method> =
  filter(methodPredicates(block))

/**
 * Filters methods of the [KClass] based on the provided [MethodPredicates] block.
 *
 * @param block A block of method predicates to test on each method.
 * @return A list of methods that match the provided block of predicates.
 */
fun KotlinClass.filterMethods(block: MethodPredicates.() -> Predicate<Method>): List<Method> =
  java.filter(methodPredicates(block))

/**
 * Filters constructors of the class based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each constructor.
 * @return A list of constructors that match the provided predicate.
 */
fun JavaClass.filterConstructors(
  predicate: Predicate<Constructor<*>>
): List<Constructor<*>> = filter(predicate)

/**
 * Filters constructors of the [KClass] based on the provided [predicate].
 *
 * @param predicate The condition to evaluate on each constructor.
 * @return A list of constructors that match the provided predicate.
 */
fun KotlinClass.filterConstructors(
  predicate: Predicate<Constructor<*>>
): List<Constructor<*>> = java.filter(predicate)

/**
 * Filters constructors of the class based on the provided [ConstructorPredicates] block.
 *
 * @param block A block of constructor predicates to test on each constructor.
 * @return A list of constructors that match the provided block of predicates.
 */
fun JavaClass.filterConstructors(
  block: ConstructorPredicates.() -> Predicate<Constructor<*>>
): List<Constructor<*>> = filter(constructorPredicates(block))

/**
 * Filters constructors of the [KClass] based on the provided [ConstructorPredicates] block.
 *
 * @param block A block of constructor predicates to test on each constructor.
 * @return A list of constructors that match the provided block of predicates.
 */
fun KotlinClass.filterConstructors(
  block: ConstructorPredicates.() -> Predicate<Constructor<*>>
): List<Constructor<*>> = java.filter(constructorPredicates(block))

/**
 * Filters functions of the [KClass] based on the provided [KFunctionPredicates] block.
 *
 * @param block A block of function predicates to test on each function.
 * @return A list of functions that match the provided block of predicates.
 */
fun KotlinClass.filterFunctions(
  block: KFunctionPredicates.() -> Predicate<KFunction<*>>
): List<KFunction<*>> = filter(functionPredicates(block))

/**
 * Filters properties of the [KClass] based on the provided [KPropertyPredicates] block.
 *
 * @param block A block of property predicates to test on each [property][KProperty].
 * @return A list of properties that match the provided block of predicates.
 */
fun KotlinClass.filterProperties(
  block: KPropertyPredicates.() -> Predicate<KProperty<*>>
): List<KProperty<*>> = filter(propertyPredicates(block))

// -------------------------------------------------------------------------------------------------

private fun KFunction<*>.companionObjectInstance(): Any? {
  val kotlin = instanceParameter?.type?.classifier as? KClass<*> ?: return null
  return when (kotlin.isCompanion) {
    true -> kotlin.objectInstance
    else -> null
  }
}

private val allTopLevelApplicationClasses: List<KClass<*>> by lazy {
  ClassTraverser()
    .setClassNameFilter { endsWith("Kt") && !startsWith("kotlin.") }
    .collect { isKotlinCompiled() && kotlin.isTopLevelClass() }
    .map { jClass -> jClass.kotlin }
}

private val classNameRegex: Regex by lazy {
  Regex("^(([a-zA-Z_][\\w\$.]*)\\.)?([a-zA-Z_][\\w\$]+)\$")
}

private fun String.isClass() = classNameRegex.matches(this)
