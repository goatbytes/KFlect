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

package io.goatbytes.kflect.traverser

import io.goatbytes.kflect.classes.ClassNameFilter
import io.goatbytes.kflect.classes.ClassNameLoader

/**
 * A class responsible for traversing through all available classes
 * using a [ClassNameLoader] instance.
 *
 * The `ClassTraverser` iterates over each class name provided by the `loader` and attempts to
 * load each class. If loading a class succeeds, the specified [block] function is applied to it.
 * The traversal continues until [block] returns `true` for a class, ending further traversal.
 *
 * This implementation handles exceptions commonly encountered during class loading (e.g.,
 * `ClassNotFoundException`, `LinkageError`, `ExceptionInInitializerError`) to ensure that the
 * traversal is not interrupted.
 *
 * Sample usage:
 * ```kotlin
 * val traverser = ClassTraverser()
 *   .setClassNameFilter { name -> name.startsWith("com.example") }
 *
 * // Find the first class that implements a specific interface
 * traverser.traverseUntil { clazz ->
 *   if (MyInterface::class.java.isAssignableFrom(clazz)) {
 *     println("Found class implementing MyInterface: ${clazz.name}")
 *     true // Stops traversal
 *   } else {
 *     false
 *   }
 * }
 *
 * // Iterate through all classes and print names
 * ClassTraverser.INSTANCE.traverse { clazz ->
 *   println(clazz.name)
 * }
 * ```
 *
 * @param loader The [ClassNameLoader] instance used to retrieve class names.
 * Defaults to the singleton [ClassNameLoader.INSTANCE].
 *
 * @see Traverser
 */
class ClassTraverser(
  private val loader: ClassNameLoader = ClassNameLoader.INSTANCE
) : Traverser<Class<*>> {

  private var filter: ClassNameFilter? = null

  /**
   * Sets the filter to be used when loading class names.
   *
   * @param filter The predicate to filter class names.
   * @return This [ClassTraverser] instance for the chaining of function calls.
   * @see ClassNameLoader.loadClassNames(filter: ClassNameFilter)
   */
  fun setClassNameFilter(filter: ClassNameFilter) = apply {
    this.filter = filter
  }

  /**
   * Removed the [ClassNameFilter] when traversing classes.
   *
   * @return This [ClassTraverser] instance for the chaining of function calls.
   */
  fun removeClassNameFilter() = apply {
    this.filter = null
  }

  /**
   * Traverses through classes provided by the [loader] until [block] returns `true` for a class.
   * This method safely handles potential exceptions that can occur during class loading.
   *
   * @param block A lambda with a receiver of [Class] that returns a Boolean.
   * If `true`, traversal stops.
   */
  override fun traverseUntil(block: Class<*>.() -> Boolean) {
    when (val filter = filter) {
      null -> loader.loadClassNames()
      else -> loader.loadClassNames(filter)
    }.forEach { className ->
      try {
        if (Class.forName(className).block()) return
      } catch (_: ClassNotFoundException) {
        // Ignore missing classes to continue traversal
      } catch (_: LinkageError) {
        // Ignore linkage issues to continue traversal
      } catch (_: ExceptionInInitializerError) {
        // Ignore initialization errors to continue traversal
      }
    }
  }

  /**
   * The companion object to retrieve the singleton instance of [ClassTraverser].
   */
  companion object {
    /**
     * The singleton instance of [ClassTraverser] that is initialized
     * based on the operating system.
     */
    val INSTANCE: ClassTraverser by lazy { ClassTraverser(ClassNameLoader.INSTANCE) }
  }
}
