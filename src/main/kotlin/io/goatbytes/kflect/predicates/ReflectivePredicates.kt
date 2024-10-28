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
 *
 */

package io.goatbytes.kflect.predicates

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * A base class that provides common reflection-based predicates for various callable and member
 * types, including methods, constructors, properties, and fields. Each predicate facilitates checks
 * on attributes such as visibility, accessibility, name, and type, streamlining reflection
 * operations across both Kotlin and Java elements.
 *
 * This class can be extended to provide more specific predicates for individual callable types.
 *
 * @param T The type of member or callable that the predicates apply to.
 */
sealed class ReflectivePredicates<T> {

  /**
   * Helper to create a predicate for reflection callables.
   *
   * @param test The test condition to apply on the reflection callable.
   * @return A [Predicate] that checks the provided test condition.
   */
  inline fun predicate(crossinline test: (obj: T) -> Boolean) = Predicate<T> { test(it) }

  /**
   * Checks if the callable's name equals the given name.
   *
   * @param name The name to check for.
   * @return A [Predicate] checking if the callable's name matches the given name.
   */
  abstract fun name(name: String): Predicate<T>

  /**
   * Checks if the callable's name starts with the given prefix.
   *
   * @param prefix The prefix to check for.
   * @return A [Predicate] checking if the callable's name starts with the given prefix.
   */
  abstract fun prefix(prefix: String): Predicate<T>

  /**
   * Checks if the callable is public.
   *
   * @return A [Predicate] checking if the callable is public.
   */
  abstract fun isPublic(): Predicate<T>

  /**
   * Checks if the callable is private.
   *
   * @return A [Predicate] checking if the callable is private.
   */
  abstract fun isPrivate(): Predicate<T>

  /**
   * Checks if the callable is protected.
   *
   * @return A [Predicate] checking if the callable is protected.
   */
  abstract fun isProtected(): Predicate<T>

  /**
   * Checks if the callable is final.
   *
   * @return A [Predicate] checking if the callable is final.
   */
  abstract fun isFinal(): Predicate<T>

  /**
   * Checks if the callable is abstract.
   *
   * @return A [Predicate] checking if the callable is abstract.
   */
  abstract fun isAbstract(): Predicate<T>

  /**
   * Checks if the callable is accessible.
   *
   * @return A [Predicate] checking if the callable is accessible.
   */
  abstract fun isAccessible(): Predicate<T>

  /**
   * Sets the callable to be accessible and returns true if successful.
   *
   * @return A [Predicate] checking if the callable is accessible after setting.
   */
  abstract fun setAccessible(): Predicate<T>

  /**
   * Checks if the callable's return type matches the given type.
   *
   * @param kClass The return type to check.
   * @return A [Predicate] that checks if the callable's return type matches the given type.
   */
  abstract fun returnType(kClass: KClass<*>): Predicate<T>

  /**
   * Checks if the callable's return type matches the given type.
   *
   * @param jClass The return type to check.
   * @return A [Predicate] that checks if the callable's return type matches the given type.
   */
  abstract fun returnType(jClass: Class<*>): Predicate<T>

  /**
   * Checks if the member has the given annotation.
   *
   * @param annotationClass The annotation class to check for.
   * @return A [Predicate] checking if the member has the specified annotation.
   */
  abstract fun withAnnotation(annotationClass: Class<Annotation>): Predicate<T>

  /**
   * Creates a predicate to check if a member has a specified annotation type.
   *
   * @param A The annotation class to check for, specified as a reified type parameter.
   * @return A [Predicate] that evaluates to `true` if the member contains the specified annotation,
   *         otherwise `false`.
   */
  inline fun <reified A : Annotation> hasAnnotation(): Predicate<T> = predicate { member ->
    when (member) {
      is AccessibleObject -> member.annotations.any { a -> a.annotationClass == A::class }
      is KCallable<*> -> member.annotations.any { a -> a.annotationClass == A::class }
      else -> false
    }
  }

  /**
   * Checks if the member has all provided annotations.
   *
   * @param annotations The annotation classes to check for.
   * @return A [Predicate] checking if the member has all the provided annotations.
   */
  abstract fun withAnnotations(vararg annotations: Class<Annotation>): Predicate<T>

  /**
   * Checks if the member is declared in the given package.
   *
   * @param packageName The package name to check against.
   * @return A [Predicate] checking if the member is declared in the given package.
   */
  abstract fun declaredInPackage(packageName: String): Predicate<T>

  /**
   * Checks if the member's name matches the given regex.
   *
   * @param regex The regex to match the member's name against.
   * @return A [Predicate] checking if the member's name matches the provided regex.
   */
  abstract fun matchesRegex(regex: Regex): Predicate<T>

  /**
   * Checks if the executable throws the given exception.
   *
   * @param exceptionClass The exception type to check.
   * @return A [Predicate] that checks if the executable declares the given exception type.
   */
  abstract fun throwsException(exceptionClass: Class<out Throwable>): Predicate<T>

  /**
   * Companion object to create predicates for java reflection.
   */
  companion object {
    /**
     * Convenience function to create a [Predicate] using the pre-defined [KFunctionPredicates].
     *
     * @param block Generates a predicate for reflection operations on functions.
     * @return The generated [Predicate].
     */
    fun functions(
      block: KFunctionPredicates.() -> Predicate<KFunction<*>>
    ): Predicate<KFunction<*>> = KFunctionPredicates.block()

    /**
     * Convenience function to create a [Predicate] using the pre-defined [KPropertyPredicates].
     *
     * @param block Generates a predicate for reflection operations on properties.
     * @return The generated [Predicate].
     */
    fun properties(
      block: KPropertyPredicates.() -> Predicate<KProperty<*>>
    ): Predicate<KProperty<*>> = KPropertyPredicates.block()

    /**
     * Convenience function to create a [Predicate] using the pre-defined [MethodPredicates].
     *
     * @param block Generates a predicate for reflection operations on methods.
     * @return The generated [Predicate].
     */
    fun methods(
      block: MethodPredicates.() -> Predicate<Method>
    ): Predicate<Method> = MethodPredicates.block()

    /**
     * Convenience function to create a [Predicate] using the pre-defined [FieldPredicates].
     *
     * @param block Generates a predicate for reflection operations on fields.
     * @return The generated [Predicate].
     */
    fun fields(
      block: FieldPredicates.() -> Predicate<Field>
    ): Predicate<Field> = FieldPredicates.block()

    /**
     * Convenience function to create a [Predicate] using the pre-defined [ConstructorPredicates].
     *
     * @param block Generates a predicate for reflection operations on constructors.
     * @return The generated [Predicate].
     */
    fun constructors(
      block: ConstructorPredicates.() -> Predicate<Constructor<*>>
    ): Predicate<Constructor<*>> = ConstructorPredicates.block()
  }
}
