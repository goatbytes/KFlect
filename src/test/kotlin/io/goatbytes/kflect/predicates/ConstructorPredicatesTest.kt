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
import io.goatbytes.kflect.ext.constructor
import java.lang.reflect.Constructor
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConstructorPredicatesTest {

  private val javaClass = ReflectionTestClass::class.java

  private val constructorWithParams: Constructor<*> by lazy {
    javaClass.constructor(String::class.java, String::class.java)
  }

  private val defaultConstructor: Constructor<*> by lazy {
    javaClass.constructor()
  }

  @Test
  fun `test constructor generic parameter types predicate`() {
    // Predicate for constructor with two String parameters
    val predicateWithGenericParams =
      ConstructorPredicates.genericParameterTypesEquals(String::class.java, String::class.java)
    assertTrue("Constructor with two String parameters should pass") {
      predicateWithGenericParams.test(constructorWithParams)
    }
    assertFalse("Default constructor should not pass generic parameter type predicate") {
      predicateWithGenericParams.test(defaultConstructor)
    }

    // Predicate for constructor with no parameters (default constructor)
    val predicateNoParams = ConstructorPredicates.genericParameterTypesEquals()
    assertTrue("Default constructor should pass no parameters predicate") {
      predicateNoParams.test(defaultConstructor)
    }
    assertFalse("Constructor with parameters should not pass no parameters predicate") {
      predicateNoParams.test(constructorWithParams)
    }
  }
}
