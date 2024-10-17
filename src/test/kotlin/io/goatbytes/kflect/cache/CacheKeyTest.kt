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
 *
 */

package io.goatbytes.kflect.cache

import io.goatbytes.kflect.KotlinReflectionTestClass
import io.goatbytes.kflect.ReflectionTestClass
import io.goatbytes.kflect.ext.name
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheKeyTest {

  companion object {
    // Java reflection method names
    private const val JAVA_METHOD_NAME = "getInstanceField"
    private const val JAVA_STATIC_METHOD_NAME = "getStaticField"

    // Kotlin reflection function names
    private const val KOTLIN_FUNCTION_NAME = "memberFunction"
    private const val KOTLIN_EXTENSION_FUNCTION_NAME = "extensionFunction"
  }

  private val javaClass = ReflectionTestClass::class.java
  private val kotlinClass = KotlinReflectionTestClass::class

  @Test
  fun `test cache key for Java method`() {
    val methodKey = CacheKey<Method>(javaClass, JAVA_METHOD_NAME)
    val expectedKey = "${javaClass.name}.$JAVA_METHOD_NAME()"
    assertEquals(expectedKey, methodKey, "Generated cache key should match the expected format")
  }

  @Test
  fun `test cache key for Java static method`() {
    val staticMethodKey = CacheKey<Method>(javaClass, JAVA_STATIC_METHOD_NAME)
    val expectedKey = "${javaClass.name}.$JAVA_STATIC_METHOD_NAME()"
    assertEquals(
      expectedKey,
      staticMethodKey,
      "Generated cache key for static method should match the expected format"
    )
  }

  @Test
  fun `test cache key for Kotlin member function`() {
    val kotlinFunctionKey = CacheKey<KFunction<*>>(kotlinClass, KOTLIN_FUNCTION_NAME, arrayOf(String::class))
    val expectedKey = "fun ${kotlinClass.name} $KOTLIN_FUNCTION_NAME(kotlin.String)"
    assertEquals(
      expectedKey,
      kotlinFunctionKey,
      "Generated cache key for Kotlin function should match the expected format"
    )
  }

  @Test
  fun `test cache key for Kotlin extension function`() {
    val extensionFunctionKey =
      CacheKey<KFunction<*>>(String::class, KOTLIN_EXTENSION_FUNCTION_NAME)
    val expectedKey = "fun ${String::class.name} $KOTLIN_EXTENSION_FUNCTION_NAME()"
    assertEquals(
      expectedKey,
      extensionFunctionKey,
      "Generated cache key for Kotlin extension function should match the expected format"
    )
  }
}
