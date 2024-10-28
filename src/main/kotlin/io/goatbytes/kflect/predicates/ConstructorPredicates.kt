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

import java.lang.reflect.Constructor
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Predicates for reflection operations on constructors.
 *
 * Sample usage:
 * ```kotlin
 * val predicate = ConstructorPredicates.genericParameterTypesEquals(String::class.java)
 * val constructors = MyClass::class.java.declaredConstructors.filter { predicate.test(it) }
 *```
 *
 * @see ExecutablePredicates
 * @see MethodPredicates
 */
data object ConstructorPredicates : ExecutablePredicates<Constructor<*>>() {

  override fun returnType(kClass: KClass<*>) = predicate { constructor ->
    constructor.declaringClass.isAssignableFrom(kClass.java)
  }

  override fun returnType(jClass: Class<*>) = predicate { constructor ->
    constructor.declaringClass.isAssignableFrom(jClass)
  }

  /**
   * Checks if the constructor's generic parameter types match the given generic types.
   *
   * @param genericTypes The generic types to check against.
   * @return A [Predicate] that checks if the constructor's generic parameter types match the given
   *         generic types.
   */
  fun genericParameterTypesEquals(vararg genericTypes: Type) = predicate { constructor ->
    constructor.genericParameterTypes.contentEquals(genericTypes)
  }

  /**
   * Checks if the constructor's generic parameter types are assignable from the given types.
   *
   * @param genericTypes The types to check against.
   * @return A [Predicate] that checks if each generic parameter type is assignable from
   *         the provided type in the same position.
   */
  fun genericParameterTypesAssignableFrom(vararg genericTypes: Type) = predicate { constructor ->
    val parameterTypes = constructor.genericParameterTypes
    parameterTypes.size == genericTypes.size && parameterTypes.withIndex().all { (index, type) ->
      type is Class<*> && genericTypes[index] is Class<*> &&
        type.isAssignableFrom(genericTypes[index] as Class<*>)
    }
  }
}
