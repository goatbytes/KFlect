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

import io.goatbytes.kflect.JavaClass
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass

/**
 * Predicates for reflection operations on methods.
 *
 * Example usage:
 * ```
 * val predicate = MethodPredicates.returnType(String::class.java)
 * val methods = MyClass::class.java.declaredMethods.filter { predicate.test(it) }
 * ```
 *
 * @see ExecutablePredicates
 * @see ConstructorPredicates
 */
data object MethodPredicates : ExecutablePredicates<Method>() {

  override fun returnType(kClass: KClass<*>): Predicate<Method> = predicate { method ->
    method.returnType.isAssignableFrom(kClass.java)
  }

  override fun returnType(jClass: JavaClass) = predicate { method ->
    method.returnType.isAssignableFrom(jClass)
  }

  /**
   * Checks if the method's return type has a bound that is assignable to the given class.
   *
   * @param boundType The class to check against the return type bound.
   * @return A [Predicate] that verifies if the return type has an assignable bound.
   */
  fun returnTypeBoundAssignableTo(boundType: JavaClass) = predicate { method ->
    val genericType = method.genericReturnType
    if (genericType is TypeVariable<*>) {
      genericType.bounds.any { type -> type is Class<*> && boundType.isAssignableFrom(type) }
    } else {
      false
    }
  }

  /**
   * Checks if the method's return type matches the given generic type.
   *
   * @param genericType The generic type to check against the method's return type.
   * @return A [Predicate] that checks if the method's return type matches the given generic type.
   */
  fun returnTypeMatchesGeneric(genericType: JavaClass) = predicate { method ->
    method.genericReturnType == genericType
  }

  /**
   * Checks if the method's return type matches a parameterized type with the given raw type and
   * type arguments.
   *
   * @param rawType The raw type of the parameterized return type.
   * @param typeArguments The type arguments of the parameterized return type.
   * @return A [Predicate] that checks if the method's return type matches the parameterized type.
   */
  fun returnTypeMatchesParameterizedType(
    rawType: JavaClass,
    vararg typeArguments: Type
  ) = predicate { method ->
    val genericType = method.genericReturnType
    if (genericType is ParameterizedType) {
      val actualTypeArguments = genericType.actualTypeArguments
      genericType.rawType == rawType && actualTypeArguments.contentEquals(typeArguments)
    } else {
      false
    }
  }

  /**
   * Checks if the method's return type has a generic bound that matches the given type.
   *
   * @param boundType The bound type to check for.
   * @return A Predicate that checks if the method's return type has a generic bound
   *         matching the given type.
   */
  fun returnTypeHasBound(boundType: JavaClass) = predicate { method ->
    val genericType = method.genericReturnType
    if (genericType is TypeVariable<*>) {
      genericType.bounds.any { it == boundType }
    } else {
      false
    }
  }

  /**
   * Checks if the method's return type matches a wildcard type with the given upper and lower
   * bounds.
   *
   * @param upperBound The upper bound of the wildcard type.
   * @param lowerBound The lower bound of the wildcard type.
   * @return A [Predicate] that checks if the method's return type matches the given wildcard type.
   */
  fun returnTypeMatchesWildcardType(upperBound: Type?, lowerBound: Type?) = predicate { method ->
    when (val genericType = method.genericReturnType) {
      is WildcardType -> {
        val upperBounds = genericType.upperBounds
        val lowerBounds = genericType.lowerBounds
        val isUpperBound = upperBound == null || upperBounds.contentEquals(arrayOf(upperBound))
        val isLowerBound = lowerBound == null || lowerBounds.contentEquals(arrayOf(lowerBound))
        isUpperBound && isLowerBound
      }

      else -> false
    }
  }
}
