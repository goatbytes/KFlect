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

package io.goatbytes.kflect.predicates

import io.goatbytes.kflect.ReflectionTestClass
import io.goatbytes.kflect.ext.field
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FieldPredicatesTest {

  companion object {
    private const val STATIC_FINAL_FIELD = "STATIC_FINAL_FIELD"
    private const val STATIC_FIELD = "staticField"
    private const val FINAL_FIELD = "finalField"
    private const val INSTANCE_FIELD = "instanceField"
  }

  private val javaClass = ReflectionTestClass::class.java
  private val staticFinalField by lazy { javaClass.field(STATIC_FINAL_FIELD) }
  private val staticField by lazy { javaClass.field(STATIC_FIELD) }
  private val finalField by lazy { javaClass.field(FINAL_FIELD) }
  private val instanceField by lazy { javaClass.field(INSTANCE_FIELD) }

  @Test
  fun `test field type predicate`() {
    val fieldTypePredicate = FieldPredicates.withType(String::class.java)
    assertTrue("Field with type String should pass the predicate") {
      fieldTypePredicate.test(instanceField)
    }
    assertTrue("Static final field with type String should pass the predicate") {
      fieldTypePredicate.test(staticFinalField)
    }
    assertTrue("Final field with type String should pass the predicate") {
      fieldTypePredicate.test(finalField)
    }
    assertFalse("Field with different type should not pass") {
      FieldPredicates.withType(Int::class.java).test(staticField)
    }
  }

  @Test
  fun `test field type assignable predicate`() {
    val assignablePredicate = FieldPredicates.withTypeAssignableFrom(Any::class.java)
    assertTrue("Field assignable from Any should pass the predicate") {
      assignablePredicate.test(instanceField)
    }
    assertTrue("Static final field assignable from Any should pass the predicate") {
      assignablePredicate.test(staticFinalField)
    }
    assertTrue("Final field assignable from Any should pass the predicate") {
      assignablePredicate.test(finalField)
    }
  }

  @Test
  fun `test field generic type predicate`() {
    val genericTypePredicate = FieldPredicates.withGenericType(String::class.java)
    assertTrue("Field with generic type String should pass") {
      genericTypePredicate.test(instanceField)
    }
    assertTrue("Static final field with generic type String should pass") {
      genericTypePredicate.test(staticFinalField)
    }
    assertTrue("Final field with generic type String should pass") {
      genericTypePredicate.test(finalField)
    }
  }

  @Test
  fun `test field name and type predicate`() {
    val nameAndTypePredicate = FieldPredicates.withNameAndType(INSTANCE_FIELD, String::class.java)
    assertTrue("Field with matching name and type should pass") {
      nameAndTypePredicate.test(instanceField)
    }
    assertFalse("Field with non-matching name or type should not pass") {
      nameAndTypePredicate.test(staticFinalField)
    }
  }

  @Test
  fun `test static field has default value predicate`() {
    val hasDefaultValuePredicate = FieldPredicates.hasDefaultValue()
    assertFalse("Static field should not pass as it doesn't have a default value of null") {
      hasDefaultValuePredicate.test(staticField)
    }
    assertFalse("Final static field should not pass as it does not have a default value of null") {
      hasDefaultValuePredicate.test(staticFinalField)
    }
  }

  @Test
  fun `test field is static final predicate`() {
    val staticFinalPredicate = FieldPredicates.isStaticFinal()
    assertTrue("Static final field should pass the predicate") {
      staticFinalPredicate.test(staticFinalField)
    }
    assertFalse("Static non-final field should not pass") {
      staticFinalPredicate.test(staticField)
    }
    assertFalse("Instance field should not pass the static final predicate") {
      staticFinalPredicate.test(instanceField)
    }
  }
}
