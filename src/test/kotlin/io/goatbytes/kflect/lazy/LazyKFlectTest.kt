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

package io.goatbytes.kflect.lazy

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LazyKFlectTest {

  private lateinit var lazyInitialized: LazyKFlect<String>
  private var initializerInvokedCount = 0

  @BeforeTest
  fun setup() {
    initializerInvokedCount = 0
    lazyInitialized = LazyKFlect {
      initializerInvokedCount++
      "Lazy Initialized Value"
    }
  }

  @Test
  fun `test lazy initialization`() {
    // Initially, it should not be initialized
    assertFalse(lazyInitialized.isInitialized(), "Value should not be initialized initially")

    // Accessing the value should initialize it
    val value = lazyInitialized.value
    assertTrue(lazyInitialized.isInitialized(), "Value should be initialized after access")
    assertEquals("Lazy Initialized Value", value, "Lazy value should match the expected value")

    // The initializer should only be invoked once
    assertEquals(1, initializerInvokedCount, "Initializer should be invoked only once")

    // Accessing the value again should not trigger re-initialization
    val secondAccess = lazyInitialized.value
    assertEquals("Lazy Initialized Value", secondAccess, "Value should remain the same on second access")
    assertEquals(1, initializerInvokedCount, "Initializer should still be invoked only once")
  }

  @Test
  fun `test value not initialized until accessed`() {
    // The initializer should not be called until we access the value
    assertEquals(0, initializerInvokedCount, "Initializer should not be invoked before accessing the value")

    // Access the value
    lazyInitialized.value

    // Now the initializer should be called once
    assertEquals(1, initializerInvokedCount, "Initializer should be invoked when value is accessed")
  }
}
