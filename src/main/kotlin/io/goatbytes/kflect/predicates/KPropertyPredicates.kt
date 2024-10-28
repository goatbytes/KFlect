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

package io.goatbytes.kflect.predicates

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Object `KPropertyPredicates` provides predicate functions for filtering and matching properties
 * (instances of [KProperty]) based on various characteristics, such as being `const` or `lateinit`.
 * These predicates can be used to simplify property reflection and filtering operations.
 *
 * Sample usage:
 * ```kotlin
 * // Filtering all constant properties
 * val constantProperties = MyClass::class.memberProperties.filter {
 *     KPropertyPredicates.isConst().test(it)
 * }
 *
 * // Filtering properties of a specific type (e.g., String)
 * val stringProperties = MyClass::class.memberProperties.filter {
 *     KPropertyPredicates.hasType<String>().test(it)
 * }
 *
 * // Filtering nullable properties
 * val nullableProperties = MyClass::class.memberProperties.filter {
 *     KPropertyPredicates.isNullable().test(it)
 * }
 * ```
 *
 * @see KCallablePredicates
 * @see KFunctionPredicates
 */
data object KPropertyPredicates : KCallablePredicates<KProperty<*>>() {

  /**
   * Returns a predicate that checks whether a property is marked as `const`.
   *
   * A `const` property is a compile-time constant that can only be a `val`
   * with a primitive type or string.
   *
   * @return A [Predicate] that checks if the property is `const`.
   */
  fun isConst() = predicate { property -> property.isConst }

  /**
   * Returns a predicate that checks whether a property is marked as `lateinit`.
   *
   * A `lateinit` property is one that is initialized after object creation.
   * It must be a `var` and non-primitive.
   *
   * @return A [Predicate] that checks if the property is `lateinit`.
   */
  fun isLateInit() = predicate { property -> property.isLateinit }

  /**
   * Returns a predicate that checks whether a property is a `val` (immutable).
   *
   * @return A [Predicate] that checks if the property is a `val`.
   */
  fun isVal() = predicate { property ->
    (property !is KMutableProperty<*> && !property.isLateinit) ||
      property.toString().startsWith("val ")
  }

  /**
   * Returns a predicate that checks whether a property is a `var` (mutable).
   *
   * @return A [Predicate] that checks if the property is a `var`.
   */
  fun isVar() =
    predicate { property -> property is KMutableProperty<*> }

  /**
   * Returns a predicate that checks whether a property is nullable.
   *
   * @return A [Predicate] that checks if the property's return type is marked as nullable.
   */
  fun isNullable() = predicate { property -> property.returnType.isMarkedNullable }

  /**
   * Returns a predicate that checks whether a property is non-null (not nullable).
   *
   * @return A [Predicate] that checks if the property's return type is not marked as nullable.
   */
  fun isNotNullable() = predicate { property -> !property.returnType.isMarkedNullable }

  /**
   * Returns a predicate that checks whether a property is of the specified type.
   *
   * @param T The type to check.
   * @return A [Predicate] that checks if the property is of type `T`.
   */
  inline fun <reified T> hasType() =
    predicate { property -> property.returnType.classifier == T::class }

  /**
   * Checks if the property's return type matches the reified generic type [T] with the provided
   * type arguments.
   *
   * @param T The classifier return type.
   * @param typeArguments The type arguments to check for in the property's return type.
   * @return A [Predicate] that checks if the property's return type matches the reified generic
   *         type [T] and its type arguments.
   */
  inline fun <reified T> hasGenericType(vararg typeArguments: KClass<*>) = predicate { property ->
    val classifier = property.returnType.classifier
    val arguments = property.returnType.arguments.mapNotNull { it.type?.classifier }
    classifier == T::class && arguments.size == typeArguments.size && arguments.zip(typeArguments)
      .all { (actual, expected) ->
        actual == expected
      }
  }

  /**
   * Checks if the property's return type has a type bound that matches the provided bound type.
   *
   * @param boundType The bound type to check for in the property's return type.
   * @return A [Predicate] that checks if the property's return type has a type bound that matches
   *         the provided bound type.
   */
  fun hasTypeBound(boundType: KClass<*>) = predicate { property ->
    property.returnType.arguments.any { arg -> arg.type?.classifier == boundType }
  }
}
