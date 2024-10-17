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

import io.goatbytes.kflect.Invokable.Type
import io.goatbytes.kflect.ext.isConstructor
import io.goatbytes.kflect.ext.isNotStatic
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * A class representing an invokable member, such as a method, constructor, field, function, or
 * property.
 *
 * This class abstracts over Java reflection members (e.g., [Method], [Field], [Constructor]) and
 * Kotlin reflection callables (e.g., [KFunction], [KProperty]), providing a unified interface for
 * invoking them. It simplifies the invocation of these members and handles both Java and Kotlin
 * reflection seamlessly.
 *
 * @param T The type of the member, represented as either [Type.Java] or [Type.Kotlin].
 * @param member The member (method, constructor, field, function, or property) to be invoked.
 */
class Invokable<T : Type> constructor(private val member: T) {

  /**
   * The companion object containing helper functions to instantiate an [Invokable].
   */
  companion object {
    /**
     * Creates an [Invokable] for a Java reflection [Member] (e.g., a [Method], [Field], or
     * [Constructor]).
     *
     * Usage:
     * ```kotlin
     * val method: Method = SomeClass::class.method("someMethod")
     * val invokable = Invokable of method
     * ```
     *
     * @param member The Java reflection [Member] to wrap.
     * @return An [Invokable] instance that can invoke the member.
     */
    infix fun java(member: Member) = Invokable(Type.Java(member))

    /**
     * Creates an [Invokable] for a Kotlin reflection [KCallable] (e.g., a [KFunction] or
     * [KProperty]).
     *
     * Usage:
     * ```kotlin
     * val function: KFunction<*> = SomeClass::someFunction
     * val invokable = Invokable of function
     * ```
     *
     * @param callable The Kotlin reflection [KCallable] to wrap.
     * @return An [Invokable] instance that can invoke the callable.
     */
    infix fun kotlin(callable: KCallable<*>) = Invokable(Type.Kotlin(callable))
  }

  /**
   * Checks if the member is a [Method].
   *
   * @return `true` if the member is a Java [Method], `false` otherwise.
   */
  val isMethod: Boolean get() = member.value is Method

  /**
   * Checks if the member is a [Field].
   *
   * @return `true` if the member is a Java [Field], `false` otherwise.
   */
  val isField: Boolean get() = member.value is Field

  /**
   * Checks if the member is a Kotlin [KFunction].
   *
   * @return `true` if the member is a Kotlin function, `false` otherwise.
   */
  val isFunction: Boolean get() = member.value is KFunction<*>

  /**
   * Checks if the member is a Kotlin [KProperty].
   *
   * @return `true` if the member is a Kotlin property, `false` otherwise.
   */
  val isProperty: Boolean get() = member.value is KProperty<*>

  /**
   * Checks if the member is a constructor.
   *
   * This property checks whether the member is a constructor, either as a Java [Constructor]
   * or as a Kotlin [KFunction] that represents a constructor.
   *
   * @return `true` if the member is a constructor, `false` otherwise.
   */
  val isConstructor: Boolean
    get() = when (val func = member.value) {
      is Constructor<*> -> true
      is KFunction<*> -> func.isConstructor
      else -> false
    }

  /**
   * Invokes the member with the specified arguments.
   *
   * The behavior of this method depends on the type of member:
   * - **Constructor**: Creates a new instance using the provided arguments.
   * - **Method**:      Invokes the method on the receiver (if applicable) and passes the arguments.
   * - **Field**:       Returns the field's value for the specified receiver. If the field is
   *                    static, no receiver is needed.
   * - **Property**:   Invokes the getter of the Kotlin property with the provided arguments.
   * - **Function**:    Invoked the Kotlin function with the provided arguments
   *
   * Example usage:
   * ```kotlin
   * val method: Method = SomeClass::class.java.getMethod("someMethod")
   * val invokable = Invokable.of(method)
   * invokable(someInstance, arg1, arg2)
   * ```
   *
   * @param args The arguments to pass to the member (receiver first for methods/fields).
   * @return The result of invoking the member, or `null` if the invocation fails.
   * @throws Exception if an error occurs during invocation. Errors are logged in `DEBUG` mode.
   */
  @Suppress("TooGenericExceptionCaught", "SpreadOperator")
  operator fun invoke(vararg args: Any?): Any? = try {
    when (val invokable = member.value) {
      is Constructor<*> -> invokable.newInstance(*args)
      is Method -> invokable.invoke(args.firstOrNull(), *(args.drop(1).toTypedArray()))
      is Field -> invokable.get(args.firstOrNull()?.takeIf { invokable.isNotStatic })
      is KProperty<*> -> invokable.getter.call(*args)
      is KFunction<*> -> invokable.call(*args)
      else -> null
    }
  } catch (e: Exception) {
    if (DEBUG) {
      println("Failed to invoke member: ${member.value} with args: ${args.joinToString()}")
    }
    null
  }

  /**
   * A sealed class representing the type of the member, which can be either Java or Kotlin
   * reflection.
   */
  sealed class Type {
    /**
     * The value of the type.
     */
    abstract val value: Any

    /**
     * Represents a Java reflection member (e.g., [Method], [Field], or [Constructor]).
     *
     * @property value The Java reflection member.
     */
    class Java(override val value: Member) : Type()

    /**
     * Represents a Kotlin callable (e.g., [KFunction] or [KProperty]).
     *
     * @property value The Kotlin reflection callable.
     */
    class Kotlin(override val value: KCallable<*>) : Type()
  }
}
