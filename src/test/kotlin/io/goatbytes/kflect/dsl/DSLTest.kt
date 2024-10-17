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

package io.goatbytes.kflect.dsl

import io.goatbytes.kflect.KotlinReflectionTestClass
import io.goatbytes.kflect.ReflectionTestClass
import io.goatbytes.kflect.ext.field
import io.goatbytes.kflect.ext.function
import io.goatbytes.kflect.ext.kClass
import io.goatbytes.kflect.ext.property
import java.lang.reflect.Method
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DSLTest {

  private val testClass = ReflectionTestClass::class.java
  private val kotlinTestClass = KotlinReflectionTestClass::class.kClass

  companion object {
    private const val STATIC_METHOD_NAME = "getStaticField"
    private const val STATIC_FINAL_FIELD_NAME = "STATIC_FINAL_FIELD"
    private const val OVERLOADED_METHOD_NAME = "overloadedFunction"
    private const val INSTANCE_PROPERTY = "mutableProperty"
    private const val STATIC_PROPERTY = "companionProperty"
  }

  @Test
  fun `test accessible method retrieval`() {
    val method = accessible<Method>(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(method)
    assertEquals(STATIC_METHOD_NAME, method.name)
  }

  @Test
  fun `test accessibleOrNull retrieval`() {
    val method =
      accessibleOrNull<Method>(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(method)

    val nonExistentMethod =
      accessibleOrNull<Method>(ReflectionTestClass::class.java, "nonExistentMethod")
    assertNull(nonExistentMethod)
  }

  @Test
  fun `test invokable function retrieval`() {
    val invokableMethod = invokable(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(invokableMethod)
  }

  @Test
  fun `test invokableOrNull function retrieval`() {
    val invokableMethod =
      invokableOrNull(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(invokableMethod)

    val nonExistentInvokable =
      invokableOrNull(ReflectionTestClass::class.java, "nonExistentMethod")
    assertNull(nonExistentInvokable)
  }

  @Test
  fun `test method predicates`() {
    val methodPredicate = methodPredicates {
      name(STATIC_METHOD_NAME) and isStatic()
    }
    val method = testClass.getMethod(STATIC_METHOD_NAME)
    assertTrue(methodPredicate.test(method))
  }

  @Test
  fun `test field predicates`() {
    val fieldPredicate = fieldPredicates {
      withNameAndType(STATIC_FINAL_FIELD_NAME, String::class.java)
    }
    val field = testClass.field(STATIC_FINAL_FIELD_NAME)
    assertTrue(fieldPredicate.test(field))
  }

  @Test
  fun `test function predicates`() {
    val functionPredicate = functionPredicates {
      isPublic() and hasParameterCount(1)
    }
    val function = kotlinTestClass.function(OVERLOADED_METHOD_NAME, String::class)
    assertTrue(functionPredicate.test(function))
  }

  @Test
  fun `test property predicates`() {
    val propertyPredicate = propertyPredicates {
      name(INSTANCE_PROPERTY)
    }
    val property = kotlinTestClass.property(INSTANCE_PROPERTY)
    assertTrue(propertyPredicate.test(property))
  }

  @Test
  fun `test constructor predicates`() {
    val constructorPredicate = constructorPredicates {
      genericParameterTypesEquals(String::class.java, String::class.java)
    }
    val constructor = ReflectionTestClass::class.java
      .getConstructor(String::class.java, String::class.java)
    assertTrue(constructorPredicate.test(constructor))
  }

  @Test
  fun `test executable retrieval`() {
    val method = executable<Method>(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(method)
    assertEquals(STATIC_METHOD_NAME, method.name)
  }

  @Test
  fun `test executableOrNull retrieval`() {
    val method =
      executableOrNull<Method>(ReflectionTestClass::class.java, STATIC_METHOD_NAME)
    assertNotNull(method)
    val nonExistentMethod =
      executableOrNull<Method>(ReflectionTestClass::class.java, "nonExistentMethod")
    assertNull(nonExistentMethod)
  }

  @Test
  fun `test callable retrieval`() {
    val property =
      callable<KProperty<*>>(kotlinTestClass.companionObject!!, STATIC_PROPERTY)
    assertNotNull(property)
    assertEquals(STATIC_PROPERTY, property.name)
  }

  @Test
  fun `test callableOrNull retrieval`() {
    val property =
      callableOrNull<KProperty<*>>(kotlinTestClass.companionObject!!, STATIC_PROPERTY)
    assertNotNull(property)

    val nonExistentProperty =
      callableOrNull<KProperty<*>>(kotlinTestClass, "nonExistentProperty")
    assertNull(nonExistentProperty)
  }
}
