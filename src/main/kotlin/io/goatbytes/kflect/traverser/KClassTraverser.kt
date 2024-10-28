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

import io.goatbytes.kflect.classes.ClassNameLoader
import kotlin.reflect.KClass

/**
 * A class responsible for traversing through all available Kotlin classes ([KClass])
 * using a [ClassNameLoader] instance.
 *
 * The `KClassTraverser` iterates over each class name provided by the `loader`, attempts
 * to load each class, and converts it to a [KClass]. The specified [block] function is
 * applied to each [KClass], stopping the traversal if [block] returns `true`.
 *
 * This implementation handles exceptions commonly encountered during class loading
 * (e.g., `ClassNotFoundException`, `LinkageError`, `ExceptionInInitializerError`)
 * to ensure uninterrupted traversal.
 *
 * Sample usage:
 * ```kotlin
 * val traverser = KClassTraverser()
 *
 * // Find the first class that implements a specific Kotlin interface
 * traverser.traverseUntil { kClass ->
 *   if (MyInterface::class in kClass.supertypes.map { it.classifier }) {
 *     println("Found class implementing MyInterface: ${kClass.simpleName}")
 *     true // Stops traversal
 *   } else {
 *     false
 *   }
 * }
 *
 * // Iterate through all classes and print names
 * KClassTraverser(ClassNameLoader.INSTANCE).traverse { kClass ->
 *   println(kClass.qualifiedName)
 * }
 * ```
 *
 * @param loader The [ClassNameLoader] instance used to retrieve class names.
 * Defaults to the singleton [ClassNameLoader.INSTANCE].
 * @see Traverser
 * @see ClassTraverser
 */
data class KClassTraverser(
  private val loader: ClassNameLoader = ClassNameLoader.INSTANCE
) : Traverser<KClass<*>> {

  /**
   * Traverses through classes provided by the [loader] until [block] returns `true` for a [KClass].
   * This method safely handles potential exceptions that can occur during class loading.
   *
   * @param block A lambda with a receiver of [KClass] that returns a Boolean.
   * If `true`, traversal stops.
   */
  override fun traverseUntil(block: KClass<*>.() -> Boolean) {
    loader.loadClassNames().forEach { className ->
      try {
        if (Class.forName(className).kotlin.block()) return
      } catch (_: ClassNotFoundException) {
        // Ignore missing classes to continue traversal
      } catch (_: LinkageError) {
        // Ignore linkage issues to continue traversal
      } catch (_: ExceptionInInitializerError) {
        // Ignore initialization errors to continue traversal
      }
    }
  }
}
