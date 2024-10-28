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

package io.goatbytes.kflect

import java.lang.reflect.Field
import java.lang.reflect.Parameter
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction

/**
 * A constant used to enable or disable debug mode throughout the project.
 * When set to `true`, debug logs and stack traces will be printed to aid in
 * troubleshooting and development. When set to `false`, debug logs will be suppressed.
 */
var DEBUG = false

// -------------------------------------------------------------------------------------------------

/** Alias for Any. */
typealias Reflective = Any

/** Alias for Class<*>. */
typealias JavaClass = Class<*>

/** Alias for KClass<*>. */
typealias KotlinClass = KClass<*>

/** Alias for a string that represents a callable or member name. */
typealias MemberName = String

/** Alias for an array of Java reflection parameters. */
typealias Parameters = Array<out Parameter>

/** Alias for an array of Java reflection annotations. */
typealias Annotations = Array<out Annotation>

/** Alias for an array of Java reflection parameter types. */
typealias ParameterTypes = Array<out JavaClass>

/** Alias for an array of Java reflection generic parameter types. */
typealias GenericParameterTypes = Array<out Type>

/** Alias for an array of Java reflection type parameters. */
typealias TypeParameters = Array<TypeVariable<*>>

/** Alias for an array of Kotlin reflection parameter types. */
typealias KParameterTypes = Array<out KClassifier>

/** Alias for an array of any nullable. */
typealias NullableArgs = Array<out Any?>

/** Alias for a Kotlin constructor. */
typealias KConstructor = KFunction<*>

/**
 * A typealias for `String` used to represent cache keys for storing Java reflection members
 * (e.g., methods, constructors, fields). These keys are generated to uniquely identify members
 * using their class, name, and parameter types.
 *
 * Cache keys are used for efficient lookups of reflection-based members to avoid repeated
 * reflection operations, which can be expensive.
 *
 * Example usage:
 * ```
 * val cacheKey: CacheKey = CacheKey<Method>(MyClass::class.java, "myMethod", String::class.java)
 * ```
 */
typealias CacheKey = String

// -------------------------------------------------------------------------------------------------

/** Constant for identifying the constructor method `<init>`. */
@Suppress("ObjectPropertyName", "TopLevelPropertyNaming")
const val _INIT_ = "<init>"

/** An empty string. */
const val EMPTY = ""

// -------------------------------------------------------------------------------------------------

/**
 * A boolean value that indicates whether reflection on certain fields (like the `modifiers` field)
 * is blocked. This is determined by attempting to access the `modifiers` of the [Field] class.
 * If access is blocked due to the field not existing, security restrictions, or other access
 * control mechanisms, this value will be `true`.
 *
 * @return `true` if reflection is restricted, `false` otherwise.
 */
@get:Suppress("SwallowedException")
val isReflectionBlocked: Boolean by lazy {
  try {
    val field = Field::class.java.getDeclaredField("modifiers")
    !field.canAccess(null)
  } catch (e: NoSuchFieldException) {
    true // The field doesn't exist, so access is restricted
  } catch (e: SecurityException) {
    true // A security manager is preventing access
  } catch (e: IllegalAccessException) {
    true // Access to the field is restricted due to access control
  }
}

/**
 * An array of classes that are restricted from performing reflection operations.
 * This is used to prevent reflection on sensitive or internal classes, such as [KFlect],
 * which should not perform reflection on themselves to avoid unintended behavior.
 *
 * If a class is present in this array, any attempt to retrieve a [java.lang.reflect.Member]
 * for that class will be considered unacceptable.
 */
val restrictedReflectionClasses = arrayOf(KFlect::class.java)
