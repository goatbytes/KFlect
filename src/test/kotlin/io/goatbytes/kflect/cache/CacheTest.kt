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

import io.goatbytes.kflect.ReflectionTestClass
import io.goatbytes.kflect.ext.method
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheTest {

  private lateinit var cache: Cache<Any>
  private val testClass = ReflectionTestClass::class.java

  companion object {
    private const val TEST_KEY = "testKey"
    private const val STATIC_METHOD_NAME = "getStaticField"
    private const val INSTANCE_METHOD_NAME = "getInstanceField"
  }

  @BeforeTest
  fun setUp() {
    cache = Cache()
  }

  @AfterTest
  fun tearDown() {
    cache.shutdown()
  }

  @Test
  fun `test put and get method from cache`() {
    val staticMethod = testClass.method(STATIC_METHOD_NAME)

    // Add to cache
    cache.put(TEST_KEY, staticMethod)

    // Retrieve from cache
    val cachedMethod = cache[TEST_KEY]
    assertEquals(staticMethod, cachedMethod, "Cached method should match the original method")
  }

  @Test
  fun `test putIfAbsent method`() {
    val instanceMethod = testClass.method(INSTANCE_METHOD_NAME)

    // Add to cache
    cache.putIfAbsent(TEST_KEY, instanceMethod)

    // Attempt to add a different method under the same key
    val staticMethod = testClass.method(STATIC_METHOD_NAME)
    cache.putIfAbsent(TEST_KEY, staticMethod)

    // Retrieve from cache and ensure the first method was kept
    val cachedMethod = cache[TEST_KEY]
    assertEquals(instanceMethod, cachedMethod, "The first inserted method should be returned")
  }

  @Test
  fun `test replace method`() {
    val initialMethod = testClass.method(STATIC_METHOD_NAME)
    val newMethod = testClass.method(INSTANCE_METHOD_NAME)

    // Add to cache
    cache[TEST_KEY] = initialMethod

    // Replace the value
    cache.replace(TEST_KEY, newMethod)

    // Verify replacement
    val cachedMethod = cache[TEST_KEY]
    assertEquals(newMethod, cachedMethod, "Cached method should be the replaced method")
  }

  @Test
  fun `test remove method`() {
    val method = testClass.method(STATIC_METHOD_NAME)

    // Add to cache
    cache.put(TEST_KEY, method)

    // Remove from cache
    val removedMethod = cache.remove(TEST_KEY)

    // Verify removal
    assertEquals(method, removedMethod, "Removed method should match the cached method")
    assertNull(cache[TEST_KEY], "Cache should return null after removal")
  }

  @Test
  fun `test TTL eviction`() {
    // Set short TTL and cache an item
    val cache = Cache<Any>(100)

    val method = testClass.method(STATIC_METHOD_NAME)
    cache[TEST_KEY] = method

    // Immediately retrieve
    val cachedMethod = cache[TEST_KEY]
    assertEquals(method, cachedMethod, "Method should be in cache before TTL expiration")

    // Wait for TTL to expire
    TimeUnit.MILLISECONDS.sleep(150)
    cache.cleanup()

    // Cache should be empty after TTL expiration
    assertNull(cache[TEST_KEY], "Method should be evicted after TTL expiration")
  }

  @Test
  fun `test cache size limit and eviction`() {
    // Set max size of 1 to trigger eviction on second insert
    val cache = Cache<Any>(maxSize = 1)

    val method1 = testClass.method(STATIC_METHOD_NAME)
    val method2 = testClass.method(INSTANCE_METHOD_NAME)

    // Add first method
    cache["method1Key"] = method1

    // Add second method to trigger eviction of the first one
    cache["method2Key"] = method2

    // Verify first method was evicted
    assertNull(cache["method1Key"], "First method should be evicted due to size limit")
    assertEquals(method2, cache["method2Key"], "Second method should be in cache")
  }

  @Test
  fun `test containsKey and containsValue methods`() {
    val method = testClass.method(STATIC_METHOD_NAME)

    // Add method to cache
    cache[TEST_KEY] = method

    // Verify cache contains the key and value
    assertTrue(cache.containsKey(TEST_KEY), "Cache should contain the key")
    assertTrue(cache.containsValue(method), "Cache should contain the method value")
  }
}
