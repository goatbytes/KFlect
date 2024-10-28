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
import io.goatbytes.kflect.ext.accessible
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Member
import java.lang.reflect.Modifier

/**
 * Base class for creating predicates on reflection members like fields and methods.
 *
 * @param T The type of reflection member (e.g., Field, Method) that
 *          extends [AccessibleObject] and [Member].
 */
sealed class MemberPredicates<T> : ReflectivePredicates<T>()
  where T : AccessibleObject, T : Member {

  override fun name(name: String) = predicate { member -> member.name == name }

  override fun prefix(prefix: String) = predicate { member -> member.name.startsWith(prefix) }

  override fun isPublic() = predicate { member -> Modifier.isPublic(member.modifiers) }

  override fun isPrivate() = predicate { member -> Modifier.isPrivate(member.modifiers) }

  override fun isProtected() = predicate { member -> Modifier.isProtected(member.modifiers) }

  override fun isFinal() = predicate { member -> Modifier.isFinal(member.modifiers) }

  override fun isAbstract() = predicate { member -> Modifier.isAbstract(member.modifiers) }

  override fun setAccessible() = predicate { member ->
    member.accessible = true
    member.accessible
  }

  override fun withAnnotation(annotationClass: Class<Annotation>) = predicate { member ->
    member.isAnnotationPresent(annotationClass)
  }

  override fun withAnnotations(vararg annotations: Class<Annotation>) = predicate { member ->
    member.annotations.contentEquals(annotations)
  }

  override fun matchesRegex(regex: Regex) = predicate { member -> regex.matches(member.name) }

  @Suppress("DEPRECATION")
  override fun isAccessible() = predicate { member -> member.isAccessible }

  override fun declaredInPackage(packageName: String) = predicate { member ->
    member.declaringClass.packageName == packageName
  }

  /**
   * Checks if the member is static.
   *
   * @return A [Predicate] checking if the member is static.
   */
  fun isStatic() = predicate { member -> Modifier.isStatic(member.modifiers) }

  /**
   * Checks if the member is static.
   *
   * @return A [Predicate] checking if the member is static.
   */
  fun isNotStatic() = predicate { member -> !Modifier.isStatic(member.modifiers) }

  /**
   * Checks if the member is synchronized.
   *
   * @return A [Predicate] checking if the member is synchronized.
   */
  fun isSynchronized() = predicate { member -> Modifier.isSynchronized(member.modifiers) }

  /**
   * Checks if the member is volatile.
   *
   * @return A [Predicate] checking if the member is volatile.
   */
  fun isVolatile() = predicate { member -> Modifier.isVolatile(member.modifiers) }

  /**
   * Checks if the member is transient.
   *
   * @return A [Predicate] checking if the member is transient.
   */
  fun isTransient() = predicate { member -> Modifier.isTransient(member.modifiers) }

  /**
   * Checks if the member is native.
   *
   * @return A [Predicate] checking if the member is native.
   */
  fun isNative() = predicate { member -> Modifier.isNative(member.modifiers) }

  /**
   * Checks if the member is an interface.
   *
   * @return A [Predicate] checking if the member is an interface.
   */
  fun isInterface() = predicate { member -> Modifier.isInterface(member.modifiers) }

  /**
   * Checks if the member is strict.
   *
   * @return A [Predicate] checking if the member is strict.
   */
  fun isStrict() = predicate { member -> Modifier.isStrict(member.modifiers) }

  /**
   * Checks if the member is synthetic.
   *
   * @return A [Predicate] checking if the member is synthetic.
   */
  fun isSynthetic() = predicate { member -> member.isSynthetic }

  /**
   * Checks if the member's declaring class matches the provided class.
   *
   * @param clazz The class to compare with the declaring class.
   * @return A [Predicate] checking if the declaring class matches the provided class.
   */
  fun isDeclaringClass(clazz: JavaClass) = predicate { member -> member.declaringClass == clazz }

  /**
   * Checks if the member can access the provided object.
   *
   * @param obj The object to check accessibility for.
   * @return A [Predicate] checking if the member can access the given object.
   */
  fun canAccess(obj: Any?) = predicate { member -> member.canAccess(obj) }

  /**
   * Checks if the member has the given modifier.
   *
   * @param modifier The modifier to check.
   * @return A [Predicate] checking if the member has the specified modifier.
   */
  fun withModifier(modifier: Int) = predicate { member ->
    member.modifiers and modifier != 0
  }

  /**
   * Checks if the member is declared in the provided class.
   *
   * @param clazz The class to check the declaration against.
   * @return A [Predicate] checking if the member is declared in the provided class.
   */
  fun declaredInClass(clazz: JavaClass) = predicate { member ->
    member.declaringClass == clazz
  }

  /**
   * Checks if the member belongs to the given package.
   *
   * @param packageName The package name to check against.
   * @return A [Predicate] checking if the member belongs to the specified package.
   */
  fun inPackage(packageName: String) = predicate { member ->
    member.declaringClass.`package`?.name == packageName
  }

  /**
   * Checks if the member belongs to any of the provided packages.
   *
   * @param packageNames The list of package names to check against.
   * @return A [Predicate] checking if the member belongs to any of the given packages.
   */
  fun inPackages(vararg packageNames: String) = predicate { member ->
    member.declaringClass.`package`?.name in packageNames
  }

  /**
   * Checks if the member is declared in the provided class or any of its superclasses.
   *
   * @param clazz The class to check the declaration against.
   * @return A [Predicate] checking if the member is declared in the given class or its superclasses
   */
  fun declaredIn(clazz: JavaClass) = predicate { member ->
    clazz.isAssignableFrom(member.declaringClass)
  }

  /**
   * Checks if the member has any of the given annotations.
   *
   * @param annotationClasses The list of annotation classes to check for.
   * @return A [Predicate] checking if the member has any of the provided annotations.
   */
  fun hasAnyAnnotations(vararg annotationClasses: Class<out Annotation>) = predicate { member ->
    annotationClasses.any { member.isAnnotationPresent(it) }
  }

  /**
   * Checks if the member has no annotations.
   *
   * @return A [Predicate] checking if the member has no annotations.
   */
  fun hasNoAnnotations() = predicate { member ->
    member.annotations.isEmpty()
  }
}
