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

import io.goatbytes.kflect.DEBUG
import io.goatbytes.kflect.KotlinClass
import io.goatbytes.kflect.ext.isConstructor
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

/**
 * Predicates for reflection operations on Kotlin functions.
 *
 * Sample usage:
 * ```kotlin
 * // Filtering all extension functions
 * val extensionFunctions = MyClass::class.members.filterIsInstance<KFunction<*>>().filter {
 *     KFunctionPredicates.isExtensionFunction().test(it)
 * }
 *
 * // Finding functions that accept a String parameter and are marked inline
 * val stringInlineFunctions = MyClass::class.members.filterIsInstance<KFunction<*>>().filter {
 *     KFunctionPredicates.parameters(String::class).and(KFunctionPredicates.isInline()).test(it)
 * }
 *
 * // Filtering functions with a wildcard upper bound
 * val wildcardFunctions = MyClass::class.members.filterIsInstance<KFunction<*>>().filter {
 *     KFunctionPredicates.parametersMatchWildcardType(Any::class, null).test(it)
 * }
 * ```
 *
 * @see KCallablePredicates
 * @see KPropertyPredicates
 */
data object KFunctionPredicates : KCallablePredicates<KFunction<*>>() {

  /**
   * Checks if the function is a constructor.
   *
   * @return A [Predicate] that checks if the function is a constructor.
   */
  fun isConstructor() = predicate { func -> func.isConstructor }

  /**
   * Checks if the functions is an extension function.
   *
   * @return A [Predicate] that checks if the function is an extension function.
   */
  fun isExtensionFunction() = predicate { func -> func.extensionReceiverParameter != null }

  /**
   * Checks if the executable's parameter types match the given types.
   *
   * @param parameters The parameter types to check.
   * @return A [Predicate] that checks if the functions' parameter types match the given types.
   */
  fun parameters(vararg parameters: KClassifier) = predicate { func ->
    func.parameters.filter { param ->
      param.kind == KParameter.Kind.VALUE
    }.run {
      size == parameters.size && zip(parameters)
        .all { (param, type) ->
          param.type.classifier == type || param.isSubclassOf(type)
        }
    }
  }

  /**
   * Checks if the function has a parameter of the given type.
   *
   * @param kClass The parameter type to check for.
   * @return A [Predicate] that checks if the function contains the given parameter type.
   */
  fun hasParameterType(kClass: KotlinClass) = predicate { func ->
    func.parameters.any { param -> param.type.classifier == kClass }
  }

  override fun throwsException(exceptionClass: Class<out Throwable>) = predicate { func ->
    func.javaMethod?.exceptionTypes?.contains(exceptionClass) ?: run {
      if (DEBUG) {
        println("No Java method available for ${func.name}")
      }
      false
    }
  }

  /**
   * Checks if the function has a vararg parameter.
   *
   * @return A [Predicate] that checks if the function has a vararg parameter.
   */
  fun hasVarArgs() = predicate { func -> func.parameters.any { param -> param.isVararg } }

  /**
   * Checks if the function has the given number of parameters.
   *
   * @param count The number of parameters to check for.
   * @return A [Predicate] that checks if the function has the specified number of parameters.
   */
  fun hasParameterCount(count: Int) = predicate { func -> func.valueParameters.size == count }

  /**
   * Checks if the function's extension type matches the given type.
   *
   * @param type The extension type to check.
   * @return A [Predicate] that checks if the functions' extension type matches the given type.
   */
  infix fun extensionOf(type: KClassifier) = predicate { func ->
    val receiver = func.extensionReceiverParameter
    if (receiver != null && receiver is KClass<*> && type is KClass<*>) {
      receiver.isInstance(type) || receiver == type
    }
    func.extensionReceiverParameter?.type?.classifier == type
  }

  /**
   * Checks if the function is infix.
   *
   * @return A [Predicate] checking if the function is an infix function.
   */
  fun isInfix() = predicate { func -> func.isInfix }

  /**
   * Checks if the function is external.
   *
   * @return A [Predicate] checking if the function is an external function.
   */
  fun isExternal() = predicate { func -> func.isExternal }

  /**
   * Checks if the function is operator.
   *
   * @return A [Predicate] checking if the function is an operator function.
   */
  fun isOperator() = predicate { func -> func.isOperator }

  /**
   * Checks if the function is inline.
   *
   * @return A [Predicate] checking if the function is an inline function.
   */
  fun isInline() = predicate { func -> func.isInline }

  /**
   * Checks if the function's parameter types match the provided parameterized types.
   *
   * @param parameterTypes The parameterized types to check for in the function's parameters.
   * @return A [Predicate] that checks if the function's parameter types match the provided
   *          parameterized types.
   */
  fun parametersMatchParameterizedType(vararg parameterTypes: KClassifier) = predicate { func ->
    val paramTypes = func.parameters.mapNotNull { p -> p.type.classifier }
    paramTypes.size == parameterTypes.size && paramTypes.zip(parameterTypes)
      .all { (actual, expected) ->
        actual == expected
      }
  }

  /**
   * Checks if any of the function's parameters have a bound that matches the provided bound type.
   *
   * @param boundType The bound type to check for in the function's parameters.
   * @return A [Predicate] that checks if any of the function's parameters have a bound type
   *         that matches the provided bound type.
   */
  fun parameterTypesHasBound(boundType: KotlinClass) = predicate { func ->
    func.parameters.any { param ->
      param.type.arguments.any { arg -> arg.type?.classifier == boundType }
    }
  }

  /**
   * Checks if the function's parameters match a wildcard type with the provided upper and lower
   * bounds.
   *
   * @param upperBound The upper bound to check for in the function's parameters.
   *                   If null, the upper bound is not considered.
   * @param lowerBound The lower bound to check for in the function's parameters.
   *                   If null, the lower bound is not considered.
   * @return A [Predicate] that checks if the function's parameters match a wildcard type with
   *         the provided upper and lower bounds.
   */
  fun parametersMatchWildcardType(upperBound: KClassifier?, lowerBound: KClassifier?) =
    predicate { func ->
      func.parameters.any { param ->
        val upperBounds = param.type.arguments.mapNotNull { it.type?.classifier }
        val lowerBounds = param.type.arguments.mapNotNull { it.type?.classifier }
        val isUpperBound = upperBound == null || upperBounds.contains(upperBound)
        val isLowerBound = lowerBound == null || lowerBounds.contains(lowerBound)
        isUpperBound && isLowerBound
      }
    }

  private fun KParameter.isSubclassOf(classifier: KClassifier): Boolean {
    val parameterClass = type.classifier as? KClass<*> ?: return false
    val expectedClass = classifier as? KClass<*> ?: return false
    return parameterClass.isSubclassOf(expectedClass)
  }
}
