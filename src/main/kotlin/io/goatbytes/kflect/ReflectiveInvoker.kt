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

import io.goatbytes.kflect.Invokable
import io.goatbytes.kflect.MemberName
import io.goatbytes.kflect.Reflective
import io.goatbytes.kflect._INIT_
import io.goatbytes.kflect.dsl.invokableOrNull
import io.goatbytes.kflect.ext.invoke
import io.goatbytes.kflect.ext.isStatic
import io.goatbytes.kflect.ext.set
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * A utility class that provides a unified interface for invoking different reflective members,
 * such as methods, fields, constructors, and Kotlin functions or properties. It supports Java
 * reflection, Kotlin reflection, and custom `Invokable` instances.
 *
 * @param reflective the reflective member to invoke or the object to find an invokable.
 */
internal class ReflectiveInvoker(private val reflective: Reflective) {

  /**
   * Invokes the reflective member with the provided arguments. This function handles various types
   * of reflective members (e.g., methods, fields, constructors) and appropriately applies the
   * arguments.
   *
   * @param args the arguments to apply to the reflective member.
   * @return the result of the invocation, or `null` if there is no return value.
   */
  operator fun invoke(vararg args: Any?): Any? {
    return when (reflective) {
      is Method -> reflective.invokeWith(args)
      is Field -> reflective.get(args.first)
      is Constructor<*> -> reflective.newInstance(*args)
      is KFunction<*> -> reflective.invoke(*args)
      is Invokable -> call(reflective, args)
      is KProperty<*> -> reflective invokeWith args
      else -> dynamicallyInvoke(args)
    }
  }

  @Throws(IllegalArgumentException::class)
  private infix fun KProperty<*>.invokeWith(args: Array<out Any?>): Any? {
    return when (args.size) {
      2 -> this[args[0]] = args[1] // Set the property when supplied with the receiver and value.
      1 -> getter.call(args[0]) // Get the property when supplied with the receiver and no value.
      else -> error("Invalid arguments")
    }
  }

  private infix fun Method.invokeWith(args: Array<out Any?>): Any? {
    return if (isStatic) {
      invoke(null, *args)
    } else {
      invoke(args.first, *args.shifted)
    }
  }

  private fun dynamicallyInvoke(args: Array<out Any?>): Any? {
    return when (val invokable = findInvokable(args)) {
      null -> null
      else -> when (invokable.isConstructor() && args[0] != _INIT_) {
        true -> call(invokable, args)
        else -> call(invokable, args.shifted)
      }
    }
  }

  private fun findInvokable(args: Array<out Any?>): Invokable? {
    when (val name = args.first) {
      is MemberName -> invokableOrNull(reflective, name, *args.shifted)?.let { return it }
    }
    return invokableOrNull(reflective, _INIT_, *args)
  }

  private fun call(invokable: Invokable, args: Array<out Any?>): Any? {
    return when (invokable) {
      is Invokable.JavaConstructor -> invokable.invoke(*args)
      is Invokable.JavaField -> invokable(reflective)
      is Invokable.JavaMethod -> if (invokable.isStatic) {
        invokable(*args)
      } else {
        invokable(reflective, *args)
      }
      is Invokable.KotlinProperty -> invokable(reflective)
      is Invokable.KotlinFunction -> when (invokable.name) {
        _INIT_ -> invokable(*args)
        else -> invokable(reflective, *args)
      }
    }.getOrNull()
  }

  private fun Invokable.isConstructor(): Boolean {
    return when (this) {
      is Invokable.JavaConstructor -> true
      is Invokable.KotlinFunction -> name == _INIT_
      else -> false
    }
  }

  private val Array<out Any?>.first: Any? get() = firstOrNull()

  private val Array<out Any?>.shifted: Array<out Any?> get() = drop(1).toTypedArray()
}
