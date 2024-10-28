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

import ReflectiveInvoker
import io.goatbytes.kflect.dsl.accessible
import io.goatbytes.kflect.dsl.accessibleOrNull
import io.goatbytes.kflect.dsl.callable
import io.goatbytes.kflect.dsl.callableOrNull
import io.goatbytes.kflect.dsl.invokable
import io.goatbytes.kflect.dsl.invokableOrNull
import io.goatbytes.kflect.dsl.kflect
import io.goatbytes.kflect.dsl.kflectOrNull
import io.goatbytes.kflect.exceptions.NoSuchFunctionException
import io.goatbytes.kflect.exceptions.NoSuchPropertyException
import io.goatbytes.kflect.ext.assign
import io.goatbytes.kflect.ext.isStatic
import io.goatbytes.kflect.ext.jClass
import io.goatbytes.kflect.ext.kClass
import io.goatbytes.kflect.ext.name
import io.goatbytes.kflect.ext.set
import io.goatbytes.kflect.ext.staticValue
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import io.goatbytes.kflect.ext.topLevelExtensionFunction as _topLevelExtensionFunction
import io.goatbytes.kflect.ext.topLevelExtensionFunctionOrNull as _topLevelExtensionFunctionOrNull
import io.goatbytes.kflect.ext.topLevelFunction as _topLevelFunction
import io.goatbytes.kflect.ext.topLevelFunctionOrNull as _topLevelFunctionOrNull

/**
 * The `Reflect` class provides a set of utilities for working with reflection in Java and Kotlin,
 * specifically aimed at handling common reflection operations such as accessing fields, methods,
 * constructors, functions, and properties.
 *
 * The `Reflective` object represents a target object or class on which reflection operations are
 * performed.
 *
 * This class provides various utility methods such as:
 * - Retrieving accessible members like fields, methods, and constructors.
 * - Retrieving callable members like functions and properties.
 * - Invoking methods, functions and constructors dynamically.
 * - Setting and getting field and property values.
 * - Handling static and non-static fields and methods.
 *
 * These operations use reflection, which can be expensive. Therefore, this class is designed to
 * reduce boilerplate and simplify reflective code by abstracting much of the reflection logic.
 *
 * @see kflect
 * @see kflectOrNull
 */
class KFlect {

  /**
   * Retrieves an accessible field, method, or constructor of type [T].
   * Throws an [Error] if the member is not found.
   *
   * @receiver The object or class to retrieve the accessible member from.
   * @param T The accessible member type
   * @param name The name of the member to retrieve.
   * @param types The parameter types for methods or constructors.
   * @return The accessible member.
   * @throws IllegalArgumentException if the type [T] is not supported.
   * @throws NoSuchFieldException if the field was not found in the class hierarchy
   * @throws NoSuchMethodException if the method or constructor was not found in the class hierarchy
   */
  @Throws(NoSuchFieldException::class, NoSuchMethodException::class, IllegalAccessException::class)
  inline fun <reified T> Reflective.accessible(name: String, vararg types: JavaClass): T
    where T : AccessibleObject, T : Member = accessible(jClass, name, *types)

  /**
   * Retrieves an accessible field, method, or constructor of type [T], if it exists.
   * Returns null if the member is not found.
   *
   * @receiver The object or class to retrieve the accessible member from.
   * @param T The accessible member type
   * @param name The name of the member to retrieve.
   * @param types The parameter types for methods or constructors.
   * @return The accessible member or null if not found.
   */
  inline fun <reified T> Reflective.accessibleOrNull(
    name: String,
    vararg types: JavaClass
  ): T? where T : AccessibleObject, T : Member =
    accessibleOrNull(jClass, name, *types)

  /**
   * Retrieves a method by name and parameter types.
   * Throws an [Error] if the method is not found.
   *
   * @receiver The object to retrieve the method for.
   * @param name The method name.
   * @param types The parameter types.
   * @return The method.
   * @throws NoSuchElementException if the accessible object was not found in the class hierarchy
   */
  @Throws(NoSuchElementException::class)
  fun Reflective.method(name: String, vararg types: JavaClass): Method =
    accessible(jClass, name, *types)

  /**
   * Retrieves a method by name and parameter types, or null if not found.
   *
   * @receiver The object to retrieve the method for.
   * @param name The method name.
   * @param types The parameter types.
   * @return The method or null if not found.
   */
  fun Reflective.methodOrNull(name: String, vararg types: JavaClass): Method? =
    accessibleOrNull(jClass, name, *types)

  /**
   * Finds a field by name, and returns it.
   *
   * @receiver The object to retrieve the field for.
   * @param name The name of the field.
   * @throws NoSuchElementException if the accessible object was not found in the class hierarchy
   */
  @Throws(IllegalStateException::class, NoSuchFieldException::class)
  infix fun Reflective.field(name: String): Field = accessible(jClass, name)

  /**
   * Finds a field by name, returning it or `null` if not found.
   *
   * @receiver The object to retrieve the field for.
   * @param name The name of the field.
   * @return The found [Field], or `null` if not found.
   */
  fun Reflective.fieldOrNull(name: String): Field? =
    accessibleOrNull(jClass, name)

  /**
   * Finds a constructor by its parameter types and returns it.
   *
   * @receiver The object to retrieve the constructor for.
   * @param types The parameter types of the constructor.
   * @return The found [Constructor].
   * @throws NoSuchElementException if the accessible object was not found in the class hierarchy
   */
  fun Reflective.constructor(vararg types: JavaClass): Constructor<*> =
    accessible(jClass, _INIT_, *types)

  /**
   * Finds a constructor by its parameter types and returns it.
   *
   * @receiver The object to retrieve the constructor for.
   * @param types The parameter types of the constructor.
   * @return The found [Constructor].
   * @throws NoSuchElementException if the accessible object was not found in the class hierarchy
   */
  fun Reflective.constructor(vararg types: KotlinClass): Constructor<*> =
    accessible(jClass, _INIT_, *types.map { it.java }.toTypedArray())

  /**
   * Finds a constructor by its parameter types, returning it or `null` if not found.
   *
   * @receiver The object to retrieve the constructor for.
   * @param types The parameter types of the constructor.
   * @return The found [Constructor], or `null` if not found.
   */
  fun Reflective.constructorOrNull(vararg types: JavaClass): Constructor<*>? =
    accessibleOrNull(jClass, _INIT_, *types)

  /**
   * Retrieves a callable function or property of type [T].
   *
   * @receiver The object or class to retrieve the callable from.
   * @param T The Kotlin callable type
   * @param name The name of the callable to retrieve.
   * @param types The parameter types for callable.
   * @return The accessible callable.
   * @throws IllegalArgumentException if the type [T] is not supported.
   * @throws NoSuchFunctionException if the field was not found in the class hierarchy
   * @throws NoSuchPropertyException if the method or constructor was not found in the class hierarchy
   */
  inline fun <reified T : KCallable<*>> Reflective.callable(
    name: String,
    vararg types: KotlinClass
  ) = callable<T>(kClass, name, *types)

  /**
   * Retrieves an accessible callable (function or property) of type [T], if it exists.
   * Returns null if the callable is not found.
   *
   * @receiver The object or class to retrieve the callable from.
   * @param T The Kotlin callable type.
   * @param name The name of the callable to retrieve.
   * @param types The parameter types for function.
   * @return The accessible callable or null if not found.
   */
  inline fun <reified T : KCallable<*>> Reflective.callableOrNull(
    name: String,
    vararg types: KotlinClass
  ) = callableOrNull<T>(kClass, name, *types)

  /**
   * Retrieves a function by name and parameter types.
   *
   * @receiver The object to retrieve the method for.
   * @param name The function name.
   * @param types The parameter types.
   * @return The function.
   * @throws NoSuchElementException if the function was not found in the class hierarchy
   */
  @Throws(
    NoSuchFunctionException::class,
    NoSuchPropertyException::class,
    IllegalStateException::class
  )
  fun Reflective.function(name: String, vararg types: KotlinClass): KFunction<*> =
    callable<KFunction<*>>(kClass, name, *types)

  /**
   * Retrieves a top-level function with the specified [name] and parameter types [types] from the
   * associated `kClass` in this `Reflective` instance.
   *
   * @param name The name of the function to retrieve.
   * @param types The expected parameter types of the function.
   * @return The matching `KFunction` if found.
   * @throws NoSuchFunctionException if the function is not found.
   */
  fun Reflective.topLevelFunction(name: String, vararg types: KClassifier): KFunction<*> =
    kClass._topLevelFunction(name, *types)

  /**
   * Attempts to retrieve a top-level function with the specified [name] and parameter types [types]
   * from the associated `kClass` in this `Reflective` instance, returning `null` if not found.
   *
   * @param name The name of the function to search for.
   * @param types The expected parameter types of the function.
   * @return The matching `KFunction` if found, or `null` if no matching function is found.
   */
  fun Reflective.topLevelFunctionOrNull(name: String, vararg types: KClassifier): KFunction<*>? =
    kClass._topLevelFunctionOrNull(name, *types)

  /**
   * Retrieves a top-level extension function with the specified [name], [receiver] type, and
   * parameter types [types] from the associated `kClass` in this `Reflective` instance.
   *
   * @param receiver The type of the extension function's receiver.
   * @param name The name of the extension function to retrieve.
   * @param types The expected parameter types of the extension function.
   * @return The matching `KFunction` if found.
   * @throws NoSuchFunctionException if the extension function is not found.
   */
  fun Reflective.topLevelExtensionFunction(
    receiver: KClassifier,
    name: String,
    vararg types: KClassifier
  ): KFunction<*> =
    kClass._topLevelExtensionFunction(receiver, name, *types)

  /**
   * Attempts to retrieve a top-level extension function with the specified [name], [receiver]
   * type, and parameter types [types] from the associated `kClass` in this `Reflective` instance,
   * returning `null` if not found.
   *
   * @param receiver The type of the extension function's receiver.
   * @param name The name of the extension function to search for.
   * @param types The expected parameter types of the extension function.
   * @return The matching `KFunction` if found, or `null` if no matching extension function is found.
   */
  fun Reflective.topLevelExtensionFunctionOrNull(
    receiver: KClassifier,
    name: String,
    vararg types: KClassifier
  ): KFunction<*>? =
    kClass._topLevelExtensionFunctionOrNull(receiver, name, *types)

  /**
   * Retrieves a method by name and parameter types, or null if not found.
   *
   * @receiver The object to retrieve the method for.
   * @param name The method name.
   * @param types The parameter types.
   * @return The method or null if not found.
   */
  fun Reflective.functionOrNull(name: String, vararg types: JavaClass): KFunction<*>? =
    callableOrNull(kClass, name, *types)

  /**
   * Finds a property by name, and returns it.
   *
   * @receiver The object to retrieve the property for.
   * @param name The name of the property.
   * @throws NoSuchPropertyException if the property was not found in the class hierarchy
   */
  @Throws(NoSuchPropertyException::class)
  infix fun Reflective.property(name: String): KProperty<*> =
    callable<KProperty<*>>(kClass, name)

  /**
   * Finds a field by name, returning it or `null` if not found.
   *
   * @receiver The object to retrieve the field for.
   * @param name The name of the field.
   * @return The found [Field], or `null` if not found.
   */
  fun Reflective.propertyOrNull(name: String): KProperty<*>? =
    callableOrNull(kClass, name)

  /**
   * Finds an invokable member (function, method, or field) by its name and arguments.
   *
   * If the member is a function or method with arguments, it searches for an executable
   * (either a Java method or a Kotlin function) based on the provided name and argument types.
   * If no arguments are provided, it searches for the field, property, method or function.
   *
   * @receiverThe [Reflective] object that contains the member to invoke.
   * @param name The name of the member (function, method, or field) to invoke.
   * @param args The arguments to pass to the function or method.
   * @return An [Invokable] wrapper for the member (Java or Kotlin).
   * @throws ReflectiveOperationException if the specified member cannot be found.
   */
  @Throws(ReflectiveOperationException::class)
  fun Reflective.invokable(name: String, vararg args: Any?): Invokable {
    return invokable(this, name, *args)
  }

  /**
   * Finds an invokable member (function, method, or field) by its name and arguments.
   *
   * If the member is a function or method with arguments, it searches for an executable
   * (either a Java method or a Kotlin function) based on the provided name and argument types.
   * If no arguments are provided, it searches for the field, property, method or function.
   *
   * @receiver The [Reflective] object that contains the member to invoke.
   * @param name The name of the member (function, method, or field) to invoke.
   * @param args The arguments to pass to the function or method.
   * @return An [Invokable] wrapper for the member (Java or Kotlin), or null if not found.
   */
  fun Reflective.invokableOrNull(name: String, vararg args: Any?): Invokable? =
    invokableOrNull(this, name, *args)

  /**
   * Creates a new instance of the class represented by this [Reflective] object using the
   * specified arguments. The constructor is found using reflection based on the provided
   * argument types.
   *
   * @param T     The type of object to instantiate.
   * @param args  The arguments to pass to the constructor. The types of the arguments will be used
   *              to find the matching constructor.
   * @return A new instance of type [T].
   * @throws NoSuchMethodException If a matching constructor is not found.
   * @throws IllegalAccessException If the constructor is not accessible.
   * @throws InvocationTargetException If the constructor throws an exception.
   * @throws InstantiationException If the class that declares the constructor is abstract.
   */
  @Throws(
    InvocationTargetException::class,
    NoSuchMethodException::class,
    IllegalAccessException::class,
    InstantiationException::class
  )
  inline fun <reified T> Reflective.newInstance(vararg args: Any?): T {
    return invokable(_INIT_, *args)(*args).getOrNull() as T
  }

  /**
   * Sets the value of a field in this [Reflective] object.
   * The field is found by its name, and the value is assigned to it.
   *
   * If the field is static, it sets the static value directly.
   * If the field is non-static, it assigns the value to the instance of [Reflective].
   *
   * @param fieldName The name of the field to set.
   * @param value The value to assign to the field.
   * @throws NoSuchFieldException If the field with the specified name is not found.
   * @throws IllegalAccessException If the field is not accessible.
   */
  @Throws(NoSuchFieldException::class, IllegalAccessException::class)
  operator fun Reflective.set(fieldName: String, value: Any?) {
    val member = jClass.fieldOrNull(fieldName) ?: kClass.propertyOrNull(fieldName)
    when (member) {
      is Field -> {
        if (member.isStatic) {
          member.staticValue = value
        } else {
          member.assign(this, value)
        }
      }

      is KProperty<*> -> {
        member[this] = value
      }
    }
  }

  /**
   * Sets the value of the specified [Field] in this [Reflective] object.
   * This method assigns the given value to the field, handling both static and non-static fields.
   *
   * @param field The [Field] object representing the field to set.
   * @param value The value to assign to the field.
   * @throws IllegalAccessException If the field is not accessible.
   */
  @Throws(IllegalAccessException::class)
  operator fun Reflective.set(field: Field, value: Any?) = field.assign(this, value)

  /**
   * Sets the value of the specified [KProperty] in this [Reflective] object.
   * This method assigns the given value to the property.
   *
   * @param property The [KProperty] object representing the field to set.
   * @param value The value to assign to the property.
   * @throws IllegalAccessException If the property is not accessible.
   */
  @Throws(IllegalAccessException::class)
  operator fun Reflective.set(property: KProperty<*>, value: Any?) = property.set(this, value)

  /**
   * Invokes the appropriate behavior for this `Reflective` instance based on its type.
   *
   * This function dynamically dispatches an invocation depending on whether the instance is
   * a [Constructor], [Method], [Field], [KFunction], or [KProperty]. If the instance type
   * is `null` or not supported, it attempts to invoke a method with the provided arguments
   * using reflection on a name, if the first argument is a [String].
   *
   * @param args The arguments to use in the invocation. These are spread into the invoked member
   *             as needed, depending on its type.
   * @return The result of invoking the member, or `null` if the invocation could not be performed.
   */
  operator fun Reflective?.invoke(vararg args: Any?): Any? {
    return when (this) {
      null -> null
      else -> ReflectiveInvoker(this)(*args)
    }
  }

  /**
   * Retrieves the value of a field or method result by name from this [Reflective] object,
   * passing any arguments as needed.
   *
   * If this [Reflective] object is `null`, the method returns `null`.
   *
   * @param name The name of the member or callable to retrieve.
   * @param args The arguments to pass to the executable or function, if applicable.
   * @return The value of the field/property or the result of the method/function invocation.
   * @throws NoSuchFieldException If a field with the specified name is not found.
   * @throws NoSuchMethodException If a method with the specified name is not found.
   * @throws IllegalAccessException If the field or method is not accessible.
   * @throws InvocationTargetException If the method throws an exception.
   */
  operator fun Reflective?.get(name: String, vararg args: Any?): Any? = this(name, *args)

  /**
   * Retrieves the value of a field or invokes a method with the given name and arguments,
   * casting the result to [T].
   *
   * @receiver The object from which the represented member's value is to be extracted.
   * @param T The expected result type.
   * @param name The name of the field or method.
   * @param args The arguments for the method, if applicable.
   * @return The value of the field or the result of the method invocation, cast to [T].
   * @throws ReflectiveOperationException If the field or method is not found.
   */
  @Suppress("TooGenericExceptionCaught")
  @Throws(ReflectiveOperationException::class)
  inline fun <reified T> Reflective?.require(name: String, vararg args: Any?): T {
    return try {
      when (val result = this(name, *args)) {
        is T -> result
        else -> error("Expected type ${T::class.name}, but got ${result?.javaClass?.name}")
      }
    } catch (e: Exception) {
      throw ReflectiveOperationException(e)
    }
  }
}
