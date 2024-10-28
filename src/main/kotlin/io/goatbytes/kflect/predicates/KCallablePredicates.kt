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

import io.goatbytes.kflect.ext.accessible
import io.goatbytes.kflect.ext.declaringClass
import io.goatbytes.kflect.ext.declaringClassEquals
import io.goatbytes.kflect.ext.isTopLevel
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod

/**
 * Predicates for reflection operations on Kotlin callable objects.
 *
 * ```kotlin
 * val publicCallables = MyClass::class.members.filter {
 *     KCallablePredicates.isPublic().test(it)
 * }
 *
 * val suspendFunctions = MyClass::class.members.filter {
 *     KCallablePredicates.isSuspend().test(it)
 * }
 *
 * val specificReturnTypeCallables = MyClass::class.members.filter {
 *     KCallablePredicates.returnType(String::class).test(it)
 * }
 * ```
 *
 * @see KFunctionPredicates
 * @see KPropertyPredicates
 */
sealed class KCallablePredicates<T : KCallable<*>> : ReflectivePredicates<T>() {

  override fun name(name: String) = predicate { callable -> callable.name == name }

  override fun prefix(prefix: String) = predicate { callable -> callable.name.startsWith(prefix) }

  override fun isPublic() = predicate { callable -> callable.visibility == KVisibility.PUBLIC }

  override fun isPrivate() = predicate { callable -> callable.visibility == KVisibility.PRIVATE }

  override fun isProtected() = predicate { callable -> callable.visibility == KVisibility.PROTECTED }

  override fun isFinal() = predicate { callable -> callable.isFinal }

  override fun isAccessible() = predicate { callable -> callable.isAccessible }

  override fun setAccessible() = predicate { callable ->
    callable.accessible = true
    callable.accessible
  }

  override fun isAbstract() = predicate { callable -> callable.isAbstract }

  override fun returnType(kClass: KClass<*>) = predicate { callable ->
    callable.returnType.isSubtypeOf(kClass.starProjectedType)
  }

  override fun returnType(jClass: Class<*>) = predicate { callable ->
    callable.returnType.isSubtypeOf(jClass.kotlin.starProjectedType)
  }

  override fun withAnnotation(annotationClass: Class<Annotation>) = predicate { callable ->
    callable.annotations.any { a -> a.annotationClass == annotationClass }
  }

  override fun withAnnotations(vararg annotations: Class<Annotation>) = predicate { callable ->
    callable.annotations.let { callableAnnotations ->
      annotations.all { a -> callableAnnotations.any { it.annotationClass == a } }
    }
  }

  override fun declaredInPackage(packageName: String) = predicate { callable ->
    callable.declaringClass?.`package`?.name == packageName
  }

  override fun matchesRegex(regex: Regex) = predicate { callable -> regex.matches(callable.name) }

  override fun throwsException(exceptionClass: Class<out Throwable>) = predicate { callable ->
    when (callable) {
      is KFunction<*> -> callable.javaMethod
      is KProperty<*> -> callable.getter.javaMethod
      else -> null
    }?.exceptionTypes?.any { type ->
      type.isAssignableFrom(exceptionClass)
    } == true
  }

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
   * Checks if the callable's return type is a parameterized type that matches the provided raw type
   * and type arguments.
   *
   * @param rawType The raw type to check the callable's return type against.
   * @param typeArguments The type arguments to check for in the callable's return type.
   * @return A [Predicate] that checks if the callable's return type matches the provided raw type
   *         and type arguments.
   */
  fun returnTypeMatchesParameterizedType(rawType: KClassifier, vararg typeArguments: KClassifier) =
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
  fun returnTypeHasBound(boundType: KClassifier) = predicate { callable ->
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
  fun returnTypeMatchesWildcardType(upperBound: KClassifier?, lowerBound: KClassifier?) =
    predicate { callable ->
      val returnType = callable.returnType
      val upperBounds = returnType.arguments.mapNotNull { it.type?.classifier }
      val lowerBounds = returnType.arguments.mapNotNull { it.type?.classifier }
      val isUpperBound = upperBound == null || upperBounds.contains(upperBound)
      val isLowerBound = lowerBound == null || lowerBounds.contains(lowerBound)
      isUpperBound && isLowerBound
    }

  /**
   * Checks if the callable's instance parameter matches the provided classifier.
   *
   * @param classifier The expected declaring class of the function or property.
   * @return A [Predicate] that checks if the declaring class (instance parameter) matches the
   *         provided type.
   */
  fun declaringClass(classifier: KClassifier): Predicate<T> = predicate { callable ->
    callable.declaringClassEquals(classifier)
  }

  /**
   * Checks if the callable is a top-level callable.
   *
   * Returns A [Predicate] that checks if the callable is a top-level callable.
   */
  fun isTopLevel(): Predicate<T> = predicate { callable -> callable.isTopLevel }

  /**
   * Checks if the member has all provided annotations.
   *
   * @param annotations The annotation classes to check for.
   * @return A [Predicate] checking if the member has all the provided annotations.
   */
  fun withAnnotations(vararg annotations: KClass<out Annotation>) = predicate { function ->
    val funcAnnotations = function.annotations
    annotations.all { annotationClass ->
      funcAnnotations.any { annotation ->
        annotation.annotationClass == annotationClass
      }
    }
  }
}
