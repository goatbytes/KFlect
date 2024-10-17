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

import io.goatbytes.kflect.KotlinReflectionTestClass
import io.goatbytes.kflect.ext.property
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KPropertyPredicatesTest {

  companion object {
    const val COMPANION_CONST = "COMPANION_CONST"
    const val MUTABLE_PROPERTY = "mutableProperty"
    const val IMMUTABLE_PROPERTY = "immutableProperty"
    const val LATEINIT_PROPERTY = "lateInitProperty"
    const val NULLABLE_PROPERTY = "nullableProperty"
    const val NON_NULLABLE_PROPERTY = "nonNullableProperty"
  }

  private val kotlinClass = KotlinReflectionTestClass::class
  private val mutableProperty = kotlinClass.property(MUTABLE_PROPERTY)
  private val immutableProperty = kotlinClass.property(IMMUTABLE_PROPERTY)
  private val lateInitProperty = kotlinClass.property(LATEINIT_PROPERTY)
  private val nullableProperty = kotlinClass.property(NULLABLE_PROPERTY)
  private val nonNullableProperty = kotlinClass.property(NON_NULLABLE_PROPERTY)
  private val constProperty = KotlinReflectionTestClass.Companion::class.property(COMPANION_CONST)

  @Test
  fun `test is const`() {
    val constPredicate = KPropertyPredicates.isConst()
    assertTrue("Const property should pass the const predicate") {
      constPredicate.test(constProperty)
    }
    assertFalse("Non-const property should not pass the const predicate") {
      constPredicate.test(mutableProperty)
    }
  }

  @Test
  fun `test is lateinit`() {
    val lateInitPredicate = KPropertyPredicates.isLateInit()
    assertTrue("Lateinit property should pass the lateinit predicate") {
      lateInitPredicate.test(lateInitProperty)
    }
    assertFalse("Non-lateinit property should not pass the lateinit predicate") {
      lateInitPredicate.test(mutableProperty)
    }
  }

  @Test
  fun `test is val`() {
    val valPredicate = KPropertyPredicates.isVal()
    assertTrue("Immutable property should pass the val predicate") {
      valPredicate.test(immutableProperty)
    }
    assertFalse("Mutable property should not pass the val predicate") {
      valPredicate.test(mutableProperty)
    }
  }

  @Test
  fun `test is var`() {
    val varPredicate = KPropertyPredicates.isVar()
    assertTrue("Mutable property should pass the var predicate") {
      varPredicate.test(mutableProperty)
    }
    assertFalse("Immutable property should not pass the var predicate") {
      varPredicate.test(immutableProperty)
    }
  }

  @Test
  fun `test is nullable`() {
    val nullablePredicate = KPropertyPredicates.isNullable()
    assertTrue("Nullable property should pass the nullable predicate") {
      nullablePredicate.test(nullableProperty)
    }
    assertFalse("Non-nullable property should not pass the nullable predicate") {
      nullablePredicate.test(nonNullableProperty)
    }
  }

  @Test
  fun `test is non-nullable`() {
    val nonNullablePredicate = KPropertyPredicates.isNotNullable()
    assertTrue("Non-nullable property should pass the non-nullable predicate") {
      nonNullablePredicate.test(nonNullableProperty)
    }
    assertFalse("Nullable property should not pass the non-nullable predicate") {
      nonNullablePredicate.test(nullableProperty)
    }
  }

  @Test
  fun `test has type`() {
    val hasTypeStringPredicate = KPropertyPredicates.hasType<String>()
    assertTrue("Property with String type should pass") {
      hasTypeStringPredicate.test(mutableProperty)
    }
    assertFalse("Property with non-String type should not pass") {
      hasTypeStringPredicate.test(lateInitProperty)
    }
  }

  @Test
  fun `test has generic type`() {
    val genericTypePredicate = KPropertyPredicates.hasGenericType<String>()
    assertTrue("Generic type predicate should pass for String property") {
      genericTypePredicate.test(mutableProperty)
    }
    assertFalse("Generic type predicate should not pass for non-String property") {
      genericTypePredicate.test(lateInitProperty)
    }
  }

  @Test
  fun `test combined predicates`() {
    val combinedPredicate = KPropertyPredicates.isVar()
      .and(KPropertyPredicates.isNullable())

    assertTrue("Combined predicate should pass for nullable mutable property") {
      combinedPredicate.test(nullableProperty)
    }
    assertFalse("Combined predicate should not pass for non-nullable mutable property") {
      combinedPredicate.test(mutableProperty)
    }
  }
}
