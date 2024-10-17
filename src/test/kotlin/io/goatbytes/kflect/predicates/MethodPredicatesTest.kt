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
import io.goatbytes.kflect.dsl.methodPredicates
import io.goatbytes.kflect.ext.method
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MethodPredicatesTest {

  companion object {
    private const val STATIC_METHOD = "getStaticField"
    private const val INSTANCE_METHOD = "getInstanceField"
    private const val OVERLOAD_METHOD = "overloadedMethod"
  }

  private val javaClass = ReflectionTestClass::class.java
  private val staticJavaMethod by lazy { javaClass.method(STATIC_METHOD) }
  private val instanceJavaMethod by lazy { javaClass.method(INSTANCE_METHOD) }
  private val overloadedMethodNoArgs by lazy { javaClass.method(OVERLOAD_METHOD) }
  private val overloadedMethodWithArgs by lazy {
    javaClass.method(OVERLOAD_METHOD, String::class.java)
  }

  @Test
  fun `test method name predicate`() {
    val methodPredicate = MethodPredicates.name(INSTANCE_METHOD)
    assertTrue("Method name should match '$INSTANCE_METHOD'") {
      methodPredicate.test(instanceJavaMethod)
    }
    assertFalse("Method name should not match '$INSTANCE_METHOD'") {
      methodPredicate.test(staticJavaMethod)
    }
  }

  @Test
  fun `test method parameters predicate`() {
    // Predicate for method with no arguments
    val predicateNoArgs = MethodPredicates.parameterTypesEquals()
    assertTrue("Method with no args should pass") {
      predicateNoArgs.test(overloadedMethodNoArgs)
    }
    assertFalse("Method with args should not pass") {
      predicateNoArgs.test(overloadedMethodWithArgs)
    }

    // Predicate for method with specific arguments
    val predicateWithArgs = MethodPredicates.parameterTypesEquals(String::class.java)
    assertTrue("Method with String argument should pass") {
      predicateWithArgs.test(overloadedMethodWithArgs)
    }
    assertFalse("Method with no arguments should not pass") {
      predicateWithArgs.test(overloadedMethodNoArgs)
    }
  }

  @Test
  fun `test method modifier predicate`() {
    val publicModifierPredicate = MethodPredicates.isPublic()
    assertTrue("Static public method should pass the public modifier predicate") {
      publicModifierPredicate.test(staticJavaMethod)
    }
    assertTrue("Instance public method should pass the public modifier predicate") {
      publicModifierPredicate.test(instanceJavaMethod)
    }
  }

  @Test
  fun `test method return type predicate`() {
    val returnTypePredicate = MethodPredicates.returnType(String::class.java)
    assertTrue("Method with String return type should pass") {
      returnTypePredicate.test(overloadedMethodWithArgs)
    }
    assertFalse("Method with non-String return type should not pass") {
      returnTypePredicate.test(overloadedMethodNoArgs)
    }
  }

  @Test
  fun `test method is static predicate`() {
    val isStaticPredicate = MethodPredicates.isStatic()
    assertTrue("Static method should pass") {
      isStaticPredicate.test(staticJavaMethod)
    }
    assertFalse("Instance method should not pass the static predicate") {
      isStaticPredicate.test(instanceJavaMethod)
    }
  }

  @Test
  fun `test method is not static predicate`() {
    val isNotStaticPredicate = MethodPredicates.isNotStatic()
    assertFalse("Static method should not pass the isNotStatic predicate") {
      isNotStaticPredicate.test(staticJavaMethod)
    }
    assertTrue("Instance method should pass the isNotStatic predicate") {
      isNotStaticPredicate.test(instanceJavaMethod)
    }
  }

  @Test
  fun `test combined method predicates`() {
    // Combining name and static predicates
    val combinedPredicate = methodPredicates {
      name(STATIC_METHOD) and isStatic()
    }
    assertTrue("Static method with name '$STATIC_METHOD' should pass") {
      combinedPredicate.test(staticJavaMethod)
    }
    assertFalse("Non-static method should not pass the combined predicate") {
      combinedPredicate.test(instanceJavaMethod)
    }
  }
}
