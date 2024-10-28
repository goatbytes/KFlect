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

package io.goatbytes.kflect.dsl

import io.goatbytes.kflect.DEBUG
import io.goatbytes.kflect.EMPTY
import io.goatbytes.kflect.Invokable
import io.goatbytes.kflect.JavaClass
import io.goatbytes.kflect.KFlect
import io.goatbytes.kflect.KotlinClass
import io.goatbytes.kflect.Reflective
import io.goatbytes.kflect._INIT_
import io.goatbytes.kflect.cache.CacheKey
import io.goatbytes.kflect.cache.ReflectiveCache
import io.goatbytes.kflect.exceptions.NoSuchFunctionException
import io.goatbytes.kflect.exceptions.NoSuchPropertyException
import io.goatbytes.kflect.ext.constructorOrNull
import io.goatbytes.kflect.ext.extensionClassEquals
import io.goatbytes.kflect.ext.findFunction
import io.goatbytes.kflect.ext.function
import io.goatbytes.kflect.ext.isExtension
import io.goatbytes.kflect.ext.isKotlinCompiledClass
import io.goatbytes.kflect.ext.kClass
import io.goatbytes.kflect.ext.name
import io.goatbytes.kflect.ext.property
import io.goatbytes.kflect.ext.propertyOrNull
import io.goatbytes.kflect.ext.signature
import io.goatbytes.kflect.ext.toJavaParameterTypes
import io.goatbytes.kflect.ext.toKParameterTypes
import io.goatbytes.kflect.ext.traverseFirstNonNullOf
import io.goatbytes.kflect.ext.traverseFirstNonNullOrNullOf
import io.goatbytes.kflect.ext.valueParametersEqual
import io.goatbytes.kflect.predicates.ConstructorPredicates
import io.goatbytes.kflect.predicates.FieldPredicates
import io.goatbytes.kflect.predicates.KFunctionPredicates
import io.goatbytes.kflect.predicates.KPropertyPredicates
import io.goatbytes.kflect.predicates.MethodPredicates
import io.goatbytes.kflect.predicates.Predicate
import io.goatbytes.kflect.restrictedReflectionClasses
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 * Executes a Kotlin block of reflection code using the [KFlect] class.
 *
 * @param T The result type of the [block]
 * @param block A block of code that operates on the [KFlect] class.
 * @return The result of executing the block.
 */
inline fun <T> kflect(block: KFlect.() -> T): T = KFlect().block()

/**
 * Executes a Kotlin block of reflection code using the [KFlect] class.
 *
 * @param T The result type of the [block]
 * @param block A block of code that operates on the [KFlect] class.
 * @return The result of executing the block or `null` if an exception was thrown.
 */
inline fun <T> kflectOrNull(block: KFlect.() -> T): T? = try {
  KFlect().block()
} catch (_: Throwable) {
  null
}

// -------------------------------------------------------------------------------------------------

/**
 * Executes a block of predicates on field reflection and returns a [Predicate].
 *
 * @param block A block that defines the field reflection predicates.
 * @return A [Predicate] that can be used to test fields based on the provided predicates.
 */
fun fieldPredicates(
  block: FieldPredicates.() -> Predicate<Field>
) = FieldPredicates.block()

/**
 * Executes a block of predicates on method reflection and returns a [Predicate].
 *
 * Example:
 * ```kotlin
 * val methodPredicate = methodPredicates {
 *     isPublic() and returnType(String::class.java)
 * }
 * ```
 *
 * @param block A block that defines the method reflection predicates.
 * @return A [Predicate] that can be used to test methods based on the provided predicates.
 */
fun methodPredicates(
  block: MethodPredicates.() -> Predicate<Method>
) = MethodPredicates.block()

/**
 * Executes a block of predicates on functions reflection and returns a [Predicate].
 *
 * @param block A block that defines the method reflection predicates.
 * @return A [Predicate] that can be used to test methods based on the provided predicates.
 */
fun functionPredicates(
  block: KFunctionPredicates.() -> Predicate<KFunction<*>>
) = KFunctionPredicates.block()

/**
 * Executes a block of predicates on properties and returns a [Predicate].
 *
 * @param block A block that defines the method reflection predicates.
 * @return A [Predicate] that can be used to test methods based on the provided predicates.
 */
fun propertyPredicates(
  block: KPropertyPredicates.() -> Predicate<KProperty<*>>
) = KPropertyPredicates.block()

/**
 * Executes a block of predicates on constructor reflection and returns a [Predicate].
 *
 * @param block A block that defines the constructor reflection predicates.
 * @return A [Predicate] that can be used to test constructors based on the provided predicates.
 */
fun constructorPredicates(
  block: ConstructorPredicates.() -> Predicate<Constructor<*>>
) = ConstructorPredicates.block()

// -------------------------------------------------------------------------------------------------

/**
 * Retrieves an accessible field, method, or constructor of type [T].
 * Throws an [Error] if the member is not found.
 *
 * @param T The accessible member type.
 * @param jClass The java class to retrieve the accessible member from.
 * @param name The name of the member to retrieve.
 * @param types The parameter types for methods or constructors.
 * @return The accessible member.
 * @throws IllegalArgumentException if the type [T] is not supported.
 * @throws NoSuchFieldException if the field was not found in the class hierarchy
 * @throws NoSuchMethodException if the method or constructor was not found in the class hierarchy
 */
@Suppress("CyclomaticComplexMethod")
@Throws(IllegalStateException::class, NoSuchElementException::class)
inline fun <reified T> accessible(jClass: JavaClass, name: String, vararg types: JavaClass): T
  where T : AccessibleObject, T : Member {
  require(jClass !in restrictedReflectionClasses) {
    "${jClass.name} is a restricted class"
  }
  return ReflectiveCache.java.getOrPut(CacheKey<T>(jClass, name, types)) {
    jClass.traverseFirstNonNullOrNullOf {
      try {
        when (T::class) {
          Field::class -> getDeclaredField(name)
          Method::class -> getDeclaredMethod(name, *types)
          Constructor::class -> getDeclaredConstructor(*types)
          Executable::class -> when (name) {
            _INIT_, EMPTY -> getDeclaredConstructor(*types)
            else -> getDeclaredMethod(name, *types)
          }

          else -> error("Unsupported type ${T::class.java.name}")
        }.apply { isAccessible = true } as T
      } catch (_: NoSuchFieldException) {
        if (DEBUG) {
          println("No field named $name was found in ${this.name}")
        }
        null
      } catch (_: NoSuchMethodException) {
        if (DEBUG) {
          println("$name${types.signature()} was not found in ${this.name}")
        }
        null
      } catch (_: SecurityException) {
        if (DEBUG) {
          println("Access denied to '$name' due to security restrictions")
        }
        null
      }
    } ?: when (T::class) {
      Field::class -> throw NoSuchFieldException("${jClass.name}.$name")
      Constructor::class, Executable::class, Method::class -> {
        throw NoSuchMethodException("${jClass.name}.$name${types.signature()}")
      }

      else -> error("Unsupported type ${T::class.java.name}")
    }
  } as T
}

/**
 * Retrieves an accessible field, method, or constructor of type [T], if it exists.
 * Returns null if the member is not found.
 *
 * @param T The accessible member type.
 * @param jClass The java class to retrieve the accessible member from.
 * @param name The name of the member to retrieve.
 * @param types The parameter types for methods or constructors.
 * @return The accessible member or null if not found.
 */
inline fun <reified T> accessibleOrNull(
  jClass: JavaClass,
  name: String,
  vararg types: JavaClass
): T? where T : AccessibleObject, T : Member = tryOrNull {
  accessible(jClass, name, *types)
}

// -------------------------------------------------------------------------------------------------

/**
 * Finds and retrieves an executable (such as a [Method], [Constructor], or [Executable]) from a
 * class using reflection. Throws [IllegalStateException] or [NoSuchElementException] if the
 * reflection attempt fails.
 *
 * The function checks if the given class is restricted before traversing through the declared
 * methods or constructors of the class. If a matching executable is found (based on the name and
 * argument types), it is returned.
 *
 * @param T       The type of executable to retrieve (e.g., [Method] or [Constructor]).
 * @param jClass  The class from which to retrieve the executable.
 * @param name    The name of the executable (method or constructor) to find.
 * @param args    The arguments to match the parameters of the executable.
 *                If an argument is `null`, it is treated as `Any`.
 * @return The found executable of type [T].
 * @throws IllegalStateException If the reflection on the class is restricted.
 * @throws NoSuchElementException If no matching executable is found.
 */
@Suppress("Indentation", "SwallowedException", "CyclomaticComplexMethod")
@Throws(IllegalStateException::class, NoSuchElementException::class)
inline fun <reified T : Executable> executable(
  jClass: JavaClass,
  name: String,
  vararg args: Any?
): T {
  if (jClass in restrictedReflectionClasses) error("Reflection on ${jClass.name} is not supported")
  val types = args.toJavaParameterTypes()
  return ReflectiveCache.java.getOrPut(CacheKey<T>(jClass, name, types)) {
    jClass.traverseFirstNonNullOf {
      try {
        when (T::class) {
          Method::class, Constructor::class, Executable::class -> when (T::class) {
            Method::class -> declaredMethods
            Constructor::class -> declaredConstructors
            Executable::class -> buildList<Executable> {
              addAll(declaredMethods)
              addAll(declaredConstructors)
            }.toTypedArray()

            else -> error("Unsupported type: ${T::class.name}")
          }.first { executable ->
            var i = 0
            executable.name == name &&
              executable.parameterCount == args.size &&
              executable.parameterTypes.all { clazz ->
                clazz.isAssignableFrom(types[i]).also { i++ }
              }
          }

          else -> {
            error("Unsupported type ${T::class.java.name}".also { System.err.println(it) })
          }
        }.apply { isAccessible = true }
      } catch (e: NoSuchMethodException) {
        null
      } catch (e: NoSuchElementException) {
        null
      }
    }
  } as T
}

/**
 * Attempts to retrieve an executable (such as a [Method], [Constructor], or [Executable]) from a
 * class using reflection, returning `null` if the attempt fails.
 *
 * This function behaves similarly to [executable], but instead of throwing an exception, it
 * returns `null` if the executable cannot be found.
 *
 * @param T The type of executable to retrieve (e.g., [Method], [Constructor], or [Executable]).
 * @param jClass The class from which to retrieve the executable.
 * @param name The name of the executable (method or constructor) to find.
 * @param args The arguments to match the parameters of the executable.
 * @return The found executable of type [T], or `null` if not found.
 */
inline fun <reified T : Executable> executableOrNull(
  jClass: JavaClass,
  name: String,
  vararg args: Any?
): T? = tryOrNull {
  executable(jClass, name, *args)
}

// -------------------------------------------------------------------------------------------------

/**
 * Finds and retrieves a [callable][KCallable] (such as a [KFunction] or [KProperty]) from a
 * Kotlin class using reflection. Throws [IllegalStateException], [ReflectiveOperationException]
 * if the reflection attempt fails.
 *
 * @param T       The type of callable to retrieve (e.g., [KFunction] or [KProperty]).
 * @param kClass  The class from which to retrieve the executable.
 * @param name    The name of the executable (method or constructor) to find.
 * @param args    The arguments to match the parameters of the function.
 *                If an argument is `null`, it is treated as `Any`.
 * @return The found callable of type [T].
 * @throws IllegalStateException If the reflection on the class isn't supported.
 * @throws NoSuchPropertyException If no matching executable is found.
 * @throws NoSuchFunctionException If no matching executable is found.
 */
@Throws(
  NoSuchFunctionException::class,
  NoSuchPropertyException::class,
  IllegalStateException::class
)
inline fun <reified T : KCallable<*>> callable(
  kClass: KotlinClass,
  name: String,
  vararg args: Any?
): T {
  val parameterTypes = args.toKParameterTypes()
  return ReflectiveCache.kotlin.getOrPut(CacheKey<T>(kClass, name, parameterTypes)) {
    when (T::class) {
      KProperty::class -> kClass.property(name)
      KFunction::class, KCallable::class -> {
        if (T::class == KCallable::class && args.isEmpty()) {
          (kClass.propertyOrNull(name) as? T)?.let { property -> return property }
        }
        try {
          kClass.function(name, *parameterTypes)
        } catch (e: NoSuchFunctionException) {
          if (args.isNotEmpty()) {
            // The arguments may contain the declaring class and the extension receiver.
            val result = kClass.findFunction {
              name(name) and declaringClass(parameterTypes[0]) and predicate { function ->
                when (parameterTypes.size >= 2 && function.isExtension) {
                  true -> function.extensionClassEquals(parameterTypes[1]) &&
                    function.valueParametersEqual(parameterTypes.drop(2))

                  else -> function.valueParametersEqual(parameterTypes.drop(1))
                }
              }
            } ?: kClass.constructorOrNull(*parameterTypes)
            (result as? T)?.let { return it }
          }
          throw e
        }
      }

      else -> error("Unsupported type: ${T::class.java.name}")
    }
  } as T
}

/**
 * Finds and retrieves a [callable][KCallable] (such as a [KFunction] or [KProperty]) from a
 * Kotlin class using reflection, returning `null` if the attempt fails.
 *
 * @param T       The type of callable to retrieve (e.g., [KFunction] or [KProperty]).
 * @param kClass  The class from which to retrieve the executable.
 * @param name    The name of the executable (method or constructor) to find.
 * @param args    The arguments to match the parameters of the function.
 *                If an argument is `null`, it is treated as `Any`.
 * @return The found callable of type [T], or `null` if not found.
 */
inline fun <reified T : KCallable<*>> callableOrNull(
  kClass: KotlinClass,
  name: String,
  vararg args: Any?
): T? = tryOrNull {
  callable<T>(kClass, name, *args)
}

/**
 * Finds an invokable member (function, method, or field) by its name and arguments.
 *
 * If the member is a function or method with arguments, it searches for an executable
 * (either a Java method or a Kotlin function) based on the provided name and argument types.
 * If no arguments are provided, it searches for the field, property, method or function.
 *
 * @param receiver The [Invokable] object that contains the member to invoke.
 * @param name The name of the member (function, method, or field) to invoke.
 * @param args The arguments to pass to the function or method.
 * @return An [Invokable] wrapper for the member (Java or Kotlin).
 * @throws NoSuchFunctionException if the specified function cannot be found.
 * @throws NoSuchPropertyException if the specified property cannot be found.
 */
@Suppress("CyclomaticComplexMethod")
@Throws(NoSuchFunctionException::class, NoSuchPropertyException::class)
fun invokable(receiver: Reflective, name: String, vararg args: Any?): Invokable {
  val kClass = receiver.kClass
  val isKotlinCompiled = kClass.isKotlinCompiledClass()

  return when {
    name == _INIT_ -> callableOrNull<KFunction<*>>(kClass, name, *args)
      ?: executableOrNull<Constructor<*>>(kClass.java, name, *args)
      ?: NoSuchFunctionException(
        "Constructor with parameters '${args.toJavaParameterTypes().signature()}' " +
          "not found in '${kClass.name}'"
      )

    args.isNotEmpty() -> if (isKotlinCompiled) {
      callableOrNull<KFunction<*>>(kClass, name, *args)
        ?: executableOrNull<Executable>(kClass.java, name, *args)
    } else {
      executableOrNull<Executable>(kClass.java, name, *args)
        ?: callableOrNull<KFunction<*>>(kClass, name, *args)
    } ?: throw NoSuchFunctionException(
      "$name with arguments ${args.toKParameterTypes().signature()} not found in ${kClass.name}"
    )

    else -> if (isKotlinCompiled) {
      callableOrNull<KProperty<*>>(kClass, name)
        ?: callableOrNull<KFunction<*>>(kClass, name)
        ?: accessibleOrNull<Field>(kClass.java, name)
        ?: executableOrNull<Executable>(kClass.java, name)
    } else {
      accessibleOrNull<Field>(kClass.java, name)
        ?: executableOrNull<Executable>(kClass.java, name)
        ?: callableOrNull<KProperty<*>>(kClass, name)
        ?: callableOrNull<KFunction<*>>(kClass, name)
    } ?: throw NoSuchPropertyException("$name not found in ${kClass.name}")
  }.run {
    when (this) {
      is Member -> Invokable.create(this)
      is KCallable<*> -> Invokable.create(this)
      else -> error("Unexpected type: ${this::class.name}")
    }
  }
}

/**
 * Finds an invokable member (function, method, or field) by its name and arguments.
 *
 * If the member is a function or method with arguments, it searches for an executable
 * (either a Java method or a Kotlin function) based on the provided name and argument types.
 * If no arguments are provided, it searches for the field, property, method or function.
 *
 * @param receiver The [Reflective] object that contains the member to invoke.
 * @param name The name of the member (function, method, or field) to invoke.
 * @param args The arguments to pass to the function or method.
 * @return An [Invokable] wrapper for the member (Java or Kotlin), or null if not found.
 */
fun invokableOrNull(receiver: Reflective, name: String, vararg args: Any?): Invokable? =
  tryOrNull { invokable(receiver, name, *args) }

// -------------------------------------------------------------------------------------------------

/**
 * Executes the given block of code and returns the result, or `null` if an exception occurs.
 *
 * Example:
 * ```kotlin
 * val result = tryOrNull { someReflectionOperation() }
 * ```
 *
 * @param R The return type of the block.
 * @param block The block of code to execute.
 * @return The result of the block, or `null` if an exception is thrown.
 */
inline fun <reified R> tryOrNull(block: () -> R): R? = try {
  block()
} catch (_: Throwable) {
  null
}

/**
 * Attempts to execute the given block of code and returns `true` if successful, or `false` if an
 * exception occurs.
 *
 * This function provides a way to handle code that might throw an exception without halting
 * execution.
 *
 * @param block The block of code to execute.
 * @return `true` if the block was successfully executed, `false` if an exception was thrown.
 */
inline fun attempt(block: () -> Unit): Boolean = try {
  block()
  true
} catch (_: Throwable) {
  false
}
