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
import java.lang.reflect.Executable

/**
 * Predicates for reflection operations on executables.
 */
open class ExecutablePredicates<T : Executable> : MemberPredicates<T>() {
  /**
   * Checks if the executable's parameter types match the given types.
   *
   * @param parameterTypes The parameter types to check.
   * @return A [Predicate] that checks if the executable's parameter types match the given types.
   */
  fun parameterTypesEquals(vararg parameterTypes: JavaClass) = predicate { executable ->
    executable.parameterTypes.contentEquals(parameterTypes)
  }

  /**
   * Checks if the executable has a parameter of the given type.
   *
   * @param clazz The parameter type to check for.
   * @return A [Predicate] that checks if the executable contains the given parameter type.
   */
  fun hasParameterType(clazz: JavaClass) = predicate { executable ->
    executable.parameterTypes.contains(clazz)
  }

  /**
   * Checks if the executable's parameter types are assignable from the given types.
   *
   * @param parameterTypes The parameter types to check against.
   * @return A [Predicate] that checks if the executable's parameter types are
   *                        assignable from the given types.
   */
  fun parameterTypesAssignableFrom(vararg parameterTypes: JavaClass) = predicate { exec ->
    var index = 0
    exec.parameterTypes.size == parameterTypes.size && exec.parameterTypes.all { type ->
      type.isAssignableFrom(parameterTypes[index++])
    }
  }

  /**
   * Checks if the executable throws the given exception.
   *
   * @param exceptionClass The exception type to check.
   * @return A [Predicate] that checks if the executable declares the given exception type.
   */
  fun throwsException(exceptionClass: Class<out Throwable>) = predicate { executable ->
    executable.exceptionTypes.contains(exceptionClass)
  }

  /**
   * Checks if the executable has a vararg parameter.
   *
   * @return A [Predicate] that checks if the executable has a vararg parameter.
   */
  fun hasVarArgs() = predicate { executable -> executable.isVarArgs }

  /**
   * Checks if the executable has the given number of parameters.
   *
   * @param count The number of parameters to check for.
   * @return A [Predicate] that checks if the executable has the specified number of parameters.
   */
  fun hasParameterCount(count: Int) = predicate { executable -> executable.parameterCount == count }

  /**
   * Checks if any of the executable's parameters have the specified annotation.
   *
   * @param annotationClass The annotation class to check for.
   * @return A [Predicate] that checks if any of the executable's parameters have the specified
   *         annotation.
   */
  fun hasParameterAnnotation(annotationClass: Class<Annotation>) = predicate { executable ->
    executable.parameters.any { it.isAnnotationPresent(annotationClass) }
  }
}
