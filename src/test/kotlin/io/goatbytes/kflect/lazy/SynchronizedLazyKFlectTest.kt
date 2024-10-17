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

import kotlin.concurrent.thread
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("detekt:all")
class SynchronizedLazyKFlectTest {

  private lateinit var synchronizedLazy: SynchronizedLazyKFlect<String>
  private var initializerInvokedCount = 0
  private val `this` = this

  @Suppress("unused")
  fun functionToCall(): String = "Synchronized Lazy Initialized Value"

  @BeforeTest
  fun setup() {
    initializerInvokedCount = 0
    synchronizedLazy = SynchronizedLazyKFlect {
      initializerInvokedCount++
      `this`("functionToCall") as String
    }
  }

  @Test
  fun `test synchronized lazy initialization`() {
    // Initially, it should not be initialized
    assertFalse("Value should not be initialized initially") {
      synchronizedLazy.isInitialized()
    }

    // Accessing the value should initialize it
    val value = synchronizedLazy.value
    assertTrue("Value should be initialized after access") {
      synchronizedLazy.isInitialized()
    }
    assertTrue("Lazy value should match the expected value") {
      "Synchronized Lazy Initialized Value" == value
    }

    // The initializer should only be invoked once
    assertTrue("Initializer should be invoked only once") {
      initializerInvokedCount == 1
    }

    // Accessing the value again should not trigger re-initialization
    val secondAccess = synchronizedLazy.value
    assertTrue("Value should remain the same on second access") {
      "Synchronized Lazy Initialized Value" == secondAccess
    }
    assertTrue("Initializer should still be invoked only once") {
      initializerInvokedCount == 1
    }
  }

  @Test
  fun `test value not initialized until accessed`() {
    // The initializer should not be called until we access the value
    assertTrue("Initializer should not be invoked before accessing the value") {
      initializerInvokedCount == 0
    }

    // Access the value
    synchronizedLazy.value

    // Now the initializer should be called once
    assertTrue("Initializer should be invoked when value is accessed") {
      initializerInvokedCount == 1
    }
  }

  @Test
  fun `test thread safety with concurrent access`() {
    val threads = mutableListOf<Thread>()
    var result: String? = null

    repeat(10) {
      threads.add(thread {
        result = synchronizedLazy.value
      })
    }

    // Wait for all threads to complete
    threads.forEach { it.join() }

    // Ensure that the initializer is called exactly once despite concurrent access
    assertEquals(
      "Synchronized Lazy Initialized Value",
      result,
      "All threads should get the same value"
    )
    assertEquals(
      1,
      initializerInvokedCount,
      "Initializer should be invoked only once, even in concurrent access"
    )
  }
}
