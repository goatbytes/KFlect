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

import io.goatbytes.kflect.ext.isStatic
import io.goatbytes.kflect.ext.name
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Represents an invokable entity that can be a Java or Kotlin method, field, constructor,
 * function, or property. `Invokable` provides a unified interface for invoking each type,
 * standardizing how arguments are handled and results are returned.
 */
sealed interface Invokable {

  val name: String

  /**
   * Invokes the underlying member or callable with the specified arguments.
   *
   * @param args The arguments to pass to the member. For Java methods and fields, the first
   *             argument is treated as the instance (`this`), followed by method parameters.
   * @return A [Result] containing the result of the invocation, or an exception (if failed).
   */
  operator fun invoke(vararg args: Any?): Result<Any?>

  /**
   * A wrapper for a Java [Method] that enables standardized invocation and provides
   * a unified `toString` representation for logging and debugging.
   *
   * @param method The underlying Java method.
   */
  class JavaMethod(private val method: Method) : Invokable {

    override val name: String get() = this.method.name

    override fun invoke(vararg args: Any?): Result<Any?> = runCatching {
      if (method.isStatic) {
        method.invoke(null, *args)
      } else {
        method.invoke(args.obj, *args.arguments)
      }
    }

    override fun toString(): String = method.toString()
  }

  /**
   * A wrapper for a Java [Field] that enables standardized access to field values
   * and provides a unified `toString` representation.
   *
   * @param field The underlying Java field.
   */
  class JavaField(private val field: Field) : Invokable {

    override val name: String get() = this.field.name

    override fun invoke(vararg args: Any?): Result<Any?> = runCatching {
      field.get(args.obj)
    }

    override fun toString(): String = field.toString()
  }

  /**
   * A wrapper for a Java [Constructor] that enables invocation of constructors with specified
   * arguments and provides a unified `toString` representation.
   *
   * @param constructor The underlying Java constructor.
   */
  class JavaConstructor(private val constructor: Constructor<*>) : Invokable {

    override val name: String get() = constructor.name

    override fun invoke(vararg args: Any?): Result<Any?> = runCatching {
      constructor.newInstance(*args)
    }

    override fun toString(): String = constructor.toString()
  }

  /**
   * A wrapper for a Kotlin [KFunction] that enables invocation with specified arguments
   * and provides a unified `toString` representation.
   *
   * @param function The underlying Kotlin function.
   */
  class KotlinFunction(private val function: KFunction<*>) : Invokable {

    override val name: String get() = this.function.name

    override fun invoke(vararg args: Any?): Result<Any?> = runCatching {
      function.call(*args)
    }

    override fun toString(): String = function.toString()
  }

  /**
   * A wrapper for a Kotlin [KProperty] that enables standardized access to property values
   * and provides a unified `toString` representation.
   *
   * @param property The underlying Kotlin property.
   */
  class KotlinProperty(private val property: KProperty<*>) : Invokable {

    override val name: String get() = this.property.name

    override fun invoke(vararg args: Any?): Result<Any?> = runCatching {
      property.getter.call(args.obj)
    }

    override fun toString(): String = property.toString()
  }

  /**
   * The companion object for creating instances of [Invokable].
   */
  companion object {

    /*
     * Extracts the receiver object (i.e., the target instance) from the argument array.
     */
    private val Array<out Any?>.obj: Any? get() = firstOrNull()

    /*
     * Extracts the arguments for the member invocation, excluding the first element, which
     * is treated as the receiver object.
     */
    private val Array<out Any?>.arguments get() = drop(1).toTypedArray()

    /**
     * Creates an [Invokable] instance from a Kotlin [KCallable] ([KFunction], [KProperty]).
     *
     * @param callable The Kotlin callable to wrap.
     * @return An [Invokable] instance for the specified callable.
     * @throws IllegalArgumentException If the callable type is not supported.
     */
    fun create(callable: KCallable<*>): Invokable = when (callable) {
      is KFunction<*> -> KotlinFunction(callable)
      is KProperty<*> -> KotlinProperty(callable)
      else -> error("Unsupported type: ${callable::class.name}")
    }

    /**
     * Creates an [Invokable] instance from a Java [Member] ([Method], [Field], [Constructor]).
     *
     * @param member The Java member to wrap.
     * @return An [Invokable] instance for the specified member.
     * @throws IllegalArgumentException If the member type is not supported.
     */
    fun create(member: Member): Invokable = when (member) {
      is Method -> JavaMethod(member)
      is Field -> JavaField(member)
      is Constructor<*> -> JavaConstructor(member)
      else -> error("Unsupported type: ${member::class.name}")
    }
  }
}
