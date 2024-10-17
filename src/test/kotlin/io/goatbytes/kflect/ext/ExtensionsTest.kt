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

package io.goatbytes.kflect.ext

import io.goatbytes.kflect.KotlinReflectionTestClass
import io.goatbytes.kflect.ReflectionTestClass
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExtensionsTest {

  companion object {
    private const val STATIC_METHOD = "getStaticField"
    private const val MEMBER_FUNCTION = "memberFunction"
    private const val KOTLIN_PROPERTY = "immutableProperty"
  }

  private val kotlinClass = KotlinReflectionTestClass::class
  private val javaClass = ReflectionTestClass::class.java

  @Test
  fun `test traverse function`() {
    val collectedClasses = mutableListOf<KClass<*>>()
    kotlinClass.traverse { collectedClasses.add(this) }
    assertTrue(collectedClasses.contains(KotlinReflectionTestClass::class))
    assertTrue(collectedClasses.contains(Any::class))
  }

  @Test
  fun `test traverseFirstNonNullOrNullOf function`() {
    val result = javaClass.traverseFirstNonNullOrNullOf {
      declaredMethods.firstOrNull { method -> method.name == STATIC_METHOD }
    }

    assertNotNull(result)
    assertEquals(STATIC_METHOD, result.name)
  }

  @Test
  fun `test traverseFirstNonNullOf function`() {
    val result = javaClass.traverseFirstNonNullOf {
      declaredMethods.firstOrNull { method -> method.name == STATIC_METHOD }
    }

    assertNotNull(result)
    assertEquals(STATIC_METHOD, result.name)
  }

  @Test
  fun `test isTopLevelClass`() {
    assertFalse(kotlinClass.isTopLevelClass(), "ReflectionTestClass is not a top-level class")
  }

  @Test
  fun `test findMethod`() {
    val method = javaClass.findMethod {
      name(STATIC_METHOD)
    }

    assertNotNull(method)
    assertEquals(STATIC_METHOD, method.name)
  }

  @Test
  fun `test findFunction`() {
    val function = kotlinClass.findFunction {
      name(MEMBER_FUNCTION)
    }

    assertNotNull(function)
    assertEquals(MEMBER_FUNCTION, function.name)
  }

  @Test
  fun `test findField`() {
    val field = javaClass.findField {
      name("finalField")
    }

    assertNotNull(field)
    assertEquals("finalField", field.name)
  }

  @Test
  fun `test filterMethods`() {
    val methods = javaClass.filterMethods {
      isPublic() and returnType(String::class.java)
    }

    assertTrue(methods.isNotEmpty(), "Should find public methods with String return type")
    assertTrue(methods.all { method -> method.returnType == String::class.java })
    assertTrue(methods.all { method -> method.isPublic })
  }

  @Test
  fun `test filterFunctions`() {
    val functions = kotlinClass.filterFunctions {
      isPublic() and returnType(String::class)
    }

    assertTrue(functions.isNotEmpty(), "Should find public Kotlin functions with String return type")
    assertTrue(functions.all { func -> func.returnType.classifier == String::class })
    assertTrue(functions.all { func -> func.visibility == KVisibility.PUBLIC })
  }

  @Test
  fun `test property retrieval`() {
    val property = kotlinClass.propertyOrNull(KOTLIN_PROPERTY)
    assertNotNull(property)
  }

  @Test
  fun `test method signature`() {
    val method = javaClass.declaredMethods.first { it.name == STATIC_METHOD }
    val signature = method.signature()

    assertTrue(signature.contains(STATIC_METHOD), "Signature should contain method name")
  }

  @Test
  fun `test field signature`() {
    val field = javaClass.declaredFields.first { it.name == "finalField" }
    val signature = field.signature()

    assertTrue(signature.contains("finalField"), "Signature should contain field name")
  }

  @Test
  fun `test extension function retrieval`() {
    val extensionFunction = kotlinClass
      .extensionFunctionOrNull(String::class, "extensionFunction")
    assertNotNull(extensionFunction)
  }

  @Test
  fun `test top-level extension function retrieval`() {
    val topLevelExtensionFunction = kotlinClass
      .topLevelExtensionFunctionOrNull(String::class, "extensionFunction")
    assertNotNull(topLevelExtensionFunction)
  }
}
