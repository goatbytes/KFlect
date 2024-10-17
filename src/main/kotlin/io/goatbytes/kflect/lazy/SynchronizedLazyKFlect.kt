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

package io.goatbytes.kflect.lazy

import io.goatbytes.kflect.KFlect

/**
 * A thread-safe lazy initialization class using Kotlin reflection with synchronization.
 *
 * This class ensures that the initialization of the value is done lazily and safely in a
 * multithreaded environment by synchronizing access to the value. The initializer is a Kotlin
 * reflection operation performed in the context of the [Kflect] class.
 *
 * @param T The type of the value being lazily initialized.
 * @param initializer A block that performs reflection to initialize the value.
 *
 * Example usage:
 * ```
 * val lazyValue = SynchronizedLazyReflect {
 *     "java.lang.Boolean"["TRUE"]
 * }
 * println(lazyValue.value) // Lazy evaluation happens here
 * ```
 */
class SynchronizedLazyKFlect<T>(private val initializer: KFlect.() -> T) : Lazy<T> {
  private var cached: T? = null
  private var initialized = false

  @Suppress("UNCHECKED_CAST")
  override val value: T
    get() = synchronized(this) {
      if (initialized) cached as T else initializer(KFlect()).also { value ->
        cached = value
        initialized = true
      }
    }

  override fun isInitialized(): Boolean = initialized
}
