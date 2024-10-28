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

package io.goatbytes.kflect.classes

import io.goatbytes.kflect.os.OperatingSystem

/** A lambda function to apply specific patterns or conditions to class names. */
typealias ClassNameFilter = (String.() -> Boolean)

/**
 * A functional interface representing a loader for class names.
 *
 * The [ClassNameLoader] interface defines a single function, [loadClassNames], which retrieves
 * a list of fully qualified class names.
 */
fun interface ClassNameLoader {

  /**
   * Loads class names available in the current runtime environment.
   *
   * @return A list of fully qualified class names.
   */
  fun loadClassNames(): List<String>

  /**
   * Loads and filters class names available in the current runtime environment.
   *
   * @param filter A lambda that takes the string as a receiver and returns the result of
   *               predicate evaluation on the class name.
   * @return A list of fully qualified class names that pass the filter predicate.
   */
  fun loadClassNames(filter: ClassNameFilter): List<String> = loadClassNames().filter(filter)

  /**
   * Companion object to retrieve the default instance of [ClassNameLoader].
   */
  companion object {
    /**
     * The singleton instance of [ClassNameLoader] that is initialized
     * based on the operating system.
     *
     * This will load either [AndroidClassNameLoader] for Android
     * environments or [JvmClassNameLoader] for other environments.
     */
    val INSTANCE: ClassNameLoader by lazy {
      when (OperatingSystem.get()) {
        OperatingSystem.Android -> AndroidClassNameLoader
        else -> JvmClassNameLoader
      }
    }
  }
}
