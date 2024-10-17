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

@file:Suppress("FunctionName")

package io.goatbytes.kflect.cache

import io.goatbytes.kflect.CacheKey
import io.goatbytes.kflect.JavaClass
import io.goatbytes.kflect.ext.name
import io.goatbytes.kflect.ext.signature
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Member
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Generates a cache key for storing Java reflection members (methods, constructors, fields)
 * based on the class, member name, and parameter types.
 *
 * The cache key is constructed by concatenating the class name, member name (e.g., method or
 * field name), and if applicable, the signature of the parameter types. This key can be used
 * to cache reflection-based lookups efficiently.
 *
 * @param T The accessible member type.
 * @param klass The class containing the member (method, constructor, field, etc.)
 * @param name The name of the member (e.g., method name, field name, etc.)
 * @param types The parameter types of the member (if applicable, e.g., method/constructor params)
 * @return A string that uniquely identifies a reflection member for caching purposes.
 *
 * Example:
 * ```
 * val methodKey: CacheKey = CacheKey<Method>(MyClass::class.java, "myMethod", String::class.java)
 * ```
 */
inline fun <reified T> CacheKey(
  klass: JavaClass,
  name: String,
  types: Array<out JavaClass> = emptyArray(),
): CacheKey where T : AccessibleObject, T : Member =
  "${klass.name}.$name${if (T::class != Field::class) types.signature() else ""}"

/**
 * Generates a cache key for storing Kotlin reflection callable (functions, properties)
 * based on the class, name, and parameter types.
 *
 * The cache key is constructed by concatenating the class name, callable name (e.g., function or
 * property name), and if applicable, the signature of the parameter types. This key can be used
 * to cache reflection-based lookups efficiently.
 *
 * Example:
 * ```
 * val funcKey: CacheKey = CacheKey<KFunction<*>>(MyClass::class, "function", String::class)
 * ```
 *
 * @param T The Kotlin callable type.
 * @param kClass The class containing the callable
 * @param name The name of the callable
 * @param types The parameter types of the callable (if applicable, e.g., function parameters)
 * @return A string that uniquely identifies a reflection callable for caching purposes.
 */
inline fun <reified T : KCallable<*>> CacheKey(
  kClass: KClass<*>,
  name: String,
  types: Array<out KClass<*>> = emptyArray()
) = when (T::class) {
  KFunction::class -> "fun ${kClass.name} $name${types.signature()}"
  else -> "${kClass.name}.$name"
}
