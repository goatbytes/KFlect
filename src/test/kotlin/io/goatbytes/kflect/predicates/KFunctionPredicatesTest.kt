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
import io.goatbytes.kflect.ext.extensionFunction
import io.goatbytes.kflect.ext.function
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KFunctionPredicatesTest {

  companion object {
    const val MEMBER_FUNCTION = "memberFunction"
    const val SUSPEND_FUNCTION = "suspendFunction"
    const val INFIX_FUNCTION = "infixFunction"
    const val OPERATOR_FUNCTION = "plus"
    const val EXTENSION_FUNCTION = "extensionFunction"
    const val OVERLOADED_FUNCTION = "overloadedFunction"
    const val PRIVATE_FUNCTION = "privateFunction"
  }

  private val kotlinClass = KotlinReflectionTestClass::class
  private val memberFunction = kotlinClass.function(MEMBER_FUNCTION, String::class)
  private val suspendFunction = kotlinClass.function(SUSPEND_FUNCTION)
  private val infixFunction = kotlinClass.function(INFIX_FUNCTION, String::class)
  private val operatorFunction = kotlinClass.function(OPERATOR_FUNCTION, String::class)
  private val overloadedFunctionNoArgs = kotlinClass.function(OVERLOADED_FUNCTION)
  private val overloadedFunctionWithArgs = kotlinClass.function(OVERLOADED_FUNCTION, String::class)
  private val extensionFunction = kotlinClass.extensionFunction(String::class, EXTENSION_FUNCTION)

  @Test
  fun `test function is public`() {
    val publicPredicate = KFunctionPredicates.isPublic()
    assertTrue("Member function should be public") {
      publicPredicate.test(memberFunction)
    }
  }

  @Test
  fun `test function is private`() {
    val privateFunction = kotlinClass.function(PRIVATE_FUNCTION, String::class)
    val privatePredicate = KFunctionPredicates.isPrivate()
    assertTrue("Private function should be private") {
      privatePredicate.test(privateFunction)
    }
  }

  @Test
  fun `test function is suspend`() {
    val suspendPredicate = KFunctionPredicates.isSuspend()
    assertTrue("Suspend function should pass suspend predicate") {
      suspendPredicate.test(suspendFunction)
    }
  }

  @Test
  fun `test function is infix`() {
    val infixPredicate = KFunctionPredicates.isInfix()
    assertTrue("Infix function should pass infix predicate") {
      infixPredicate.test(infixFunction)
    }
  }

  @Test
  fun `test function is operator`() {
    val operatorPredicate = KFunctionPredicates.isOperator()
    assertTrue("Operator function should pass operator predicate") {
      operatorPredicate.test(operatorFunction)
    }
  }

  @Test
  fun `test function is extension`() {
    val extensionPredicate = KFunctionPredicates.isExtensionFunction()
    assertTrue("Extension function should pass extension predicate") {
      extensionPredicate.test(extensionFunction)
    }
  }

  @Test
  fun `test function name predicate`() {
    val namePredicate = KFunctionPredicates.name(MEMBER_FUNCTION)
    assertTrue("Function name should match 'memberFunction'") {
      namePredicate.test(memberFunction)
    }
    assertFalse("Function name should not match 'suspendFunction'") {
      namePredicate.test(suspendFunction)
    }
  }

  @Test
  fun `test function return type predicate`() {
    val returnTypePredicate = KFunctionPredicates.returnType(String::class)
    assertTrue("Function with return type String should pass") {
      returnTypePredicate.test(memberFunction)
    }
  }

  @Test
  fun `test function parameters predicate`() {
    val parametersPredicate = KFunctionPredicates.parameters(String::class)
    assertTrue("Function with a single String parameter should pass") {
      parametersPredicate.test(overloadedFunctionWithArgs)
    }
    assertFalse("Function with no parameters should not pass") {
      parametersPredicate.test(overloadedFunctionNoArgs)
    }
  }

  @Test
  fun `test function extension of predicate`() {
    val extensionOfPredicate = KFunctionPredicates.extensionOf(String::class)
    assertTrue("String extension function should pass extensionOf predicate") {
      extensionOfPredicate.test(extensionFunction)
    }
  }

  @Test
  fun `test combined predicates`() {
    val combinedPredicate = KFunctionPredicates.name(INFIX_FUNCTION)
      .and(KFunctionPredicates.isInfix())

    assertTrue("Combined predicate should pass for infix function") {
      combinedPredicate.test(infixFunction)
    }

    assertFalse("Combined predicate should not pass for member function") {
      combinedPredicate.test(memberFunction)
    }
  }
}
