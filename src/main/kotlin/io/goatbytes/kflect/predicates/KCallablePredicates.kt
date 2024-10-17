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

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.isAccessible

/**
 * Predicates for reflection operations on Kotlin callable objects.
 */
sealed class KCallablePredicates<T : KCallable<*>> {

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
  fun name(name: String) = predicate { callable -> callable.name == name }

  /**
   * Checks if the callable's name starts with the given prefix.
   *
   * @param prefix The prefix to check for.
   * @return A [Predicate] checking if the callable's name starts with the given prefix.
   */
  fun prefix(prefix: String) = predicate { callable -> callable.name.startsWith(prefix) }

  /**
   * Checks if the callable is public.
   *
   * @return A [Predicate] checking if the callable is public.
   */
  fun isPublic() = predicate { callable -> callable.visibility == KVisibility.PUBLIC }

  /**
   * Checks if the callable is private.
   *
   * @return A [Predicate] checking if the callable is private.
   */
  fun isPrivate() = predicate { callable -> callable.visibility == KVisibility.PRIVATE }

  /**
   * Checks if the callable is protected.
   *
   * @return A [Predicate] checking if the callable is protected.
   */
  fun isProtected() = predicate { callable -> callable.visibility == KVisibility.PROTECTED }

  /**
   * Checks if the callable is internal.
   *
   * @return A [Predicate] checking if the callable is internal.
   */
  fun isInternal() = predicate { callable -> callable.visibility == KVisibility.INTERNAL }

  /**
   * Checks if the callable is final.
   *
   * @return A [Predicate] checking if the callable is final.
   */
  fun isFinal() = predicate { callable -> callable.isFinal }

  /**
   * Checks if the callable is marked as suspend.
   *
   * @return A [Predicate] checking if the callable is marked as suspend.
   */
  fun isSuspend() = predicate { callable -> callable.isSuspend }

  /**
   * Checks if the callable is open.
   *
   * @return A [Predicate] checking if the callable is open.
   */
  fun isOpen() = predicate { callable -> callable.isOpen }

  /**
   * Checks if the callable is accessible.
   *
   * @return A [Predicate] checking if the callable is accessible.
   */
  fun isAccessible() = predicate { callable -> callable.isAccessible }

  /**
   * Checks if the callable is abstract.
   *
   * @return A [Predicate] checking if the callable is abstract.
   */
  fun isAbstract() = predicate { callable -> callable.isAbstract }

  /**
   * Checks if the callable's return type matches the given type.
   *
   * @param type The return type to check.
   * @return A [Predicate] that checks if the callable's return type matches the given type.
   */
  fun returnType(type: KClass<*>) = predicate { callable ->
    callable.returnType.classifier == type
  }

  /**
   * Checks if the callable's return type is a parameterized type that matches the provided raw type
   * and type arguments.
   *
   * @param rawType The raw type to check the callable's return type against.
   * @param typeArguments The type arguments to check for in the callable's return type.
   * @return A [Predicate] that checks if the callable's return type matches the provided raw type
   *         and type arguments.
   */
  fun returnTypeMatchesParameterizedType(rawType: KClass<*>, vararg typeArguments: KClass<*>) =
    predicate { callable ->
      val returnType = callable.returnType
      if (returnType.classifier == rawType) {
        val actualTypeArguments = returnType.arguments.mapNotNull { it.type?.classifier }
        actualTypeArguments.size == typeArguments.size && actualTypeArguments.zip(typeArguments)
          .all { (actual, expected) ->
            actual == expected
          }
      } else false
    }

  /**
   * Checks if the callable's return type has a bound that matches the provided bound type.
   *
   * @param boundType The bound type to check for in the callable's return type.
   * @return A [Predicate] that checks if the callable's return type has a bound that matches
   *         the provided bound type.
   */
  fun returnTypeHasBound(boundType: KClass<*>) = predicate { callable ->
    val returnType = callable.returnType
    returnType.arguments.any { arg -> arg.type?.classifier == boundType }
  }

  /**
   * Checks if the callable's return type matches a wildcard type with the provided upper and lower
   * bounds.
   *
   * @param upperBound The upper bound to check for in the callable's return type.
   *                   If null, the upper bound is not considered.
   * @param lowerBound The lower bound to check for in the callable's return type.
   *                   If null, the lower bound is not considered.
   * @return A [Predicate] that checks if the callable's return type matches a wildcard type with
   *         the provided upper and lower bounds.
   */
  fun returnTypeMatchesWildcardType(upperBound: KClass<*>?, lowerBound: KClass<*>?) =
    predicate { callable ->
      val returnType = callable.returnType
      val upperBounds = returnType.arguments.mapNotNull { it.type?.classifier }
      val lowerBounds = returnType.arguments.mapNotNull { it.type?.classifier }
      val isUpperBound = upperBound == null || upperBounds.contains(upperBound)
      val isLowerBound = lowerBound == null || lowerBounds.contains(lowerBound)
      isUpperBound && isLowerBound
    }
}
