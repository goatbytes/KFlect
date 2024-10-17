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
 */

package io.goatbytes.kflect.cache

import java.lang.reflect.Member
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KCallable

/**
 * Object `ReflectiveCache` acts as a thread-safe cache for storing and retrieving reflection
 * members (e.g., methods, constructors, fields, functions, properties) based on their [CacheKey].
 *
 * The cache is designed to improve performance by avoiding repeated reflection operations,
 * which can be expensive.
 */
object ReflectiveCache {
  /** Thread-safe cache for Java reflection members (e.g. methods, fields, constructors) */
  val java: ConcurrentMap<CacheKey, Member> by lazy { Cache() }

  /** Thread-safe cache for Kotlin reflection callables (e.g. functions and properties) */
  val kotlin: ConcurrentMap<CacheKey, KCallable<*>> by lazy { Cache() }
}
