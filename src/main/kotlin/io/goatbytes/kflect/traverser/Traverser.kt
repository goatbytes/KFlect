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
 */

package io.goatbytes.kflect.traverser

import java.util.*

/**
 * A generic interface representing a class traverser.
 *
 * Provides methods to traverse items of type [T] and perform operations on them.
 *
 * @param T The type of items that the traverser operates on.
 * @see ClassTraverser
 * @see KClassTraverser
 */
fun interface Traverser<T> {

  /**
   * Traverses through the items and stops once the provided block returns `true`.
   * @param block A lambda with receiver of type [T] that returns a Boolean.
   */
  fun traverseUntil(block: T.() -> Boolean)

  /**
   * Traverses through the items and applies the provided block on each item.
   * The traversal is stopped when the block is applied to all items.
   * @param block A lambda with receiver of type [T] that returns Unit.
   */
  fun traverse(block: T.() -> Unit) = traverseUntil {
    block()
    false
  }

  /**
   * Collects all items that satisfy the given predicate into a collection.
   *
   * @param predicate A lambda that checks if an item matches a condition.
   * @return A collection of items that match the predicate.
   */
  fun collect(predicate: T.() -> Boolean): Collection<T> = buildList {
    traverse {
      if (predicate(this)) {
        add(this)
      }
    }
  }

  /**
   * Traverses through the items and returns the result of the first block execution
   * that is not null, or null if no such result is found.
   *
   * @param R The result type.
   * @param block A lambda with receiver of type [T] that returns nullable [R].
   * @return The result of type [R] or null if none is found.
   */
  fun <R> traverseFirstOrNullOf(block: T.() -> R?): R? {
    var result: R? = null
    traverseUntil {
      result = block()
      result != null
    }
    return result
  }

  /**
   * Traverses through the items and returns the result of the first block execution
   * that is not null. Throws [NoSuchElementException] if no result is found.
   *
   * @param R The result type.
   * @param block A lambda with receiver of type [T] that returns nullable [R].
   * @return The result of type [R].
   * @throws NoSuchElementException If no result is found.
   */
  @Throws(NoSuchElementException::class)
  fun <R> traverseFirstOf(block: T.() -> R?): R =
    traverseFirstOrNullOf(block) ?: throw NoSuchElementException(
      "No matching element found during traversal"
    )

  /**
   * Traverses through the items and returns the first item that satisfies the given block.
   * Throws [NoSuchElementException] if no such item is found.
   *
   * @param block The predicate that checks whether an item matches.
   * @return The first item that matches the predicate.
   */
  @Throws(NoSuchElementException::class)
  fun first(block: (T) -> Boolean): T =
    firstOrNull(block) ?: throw NoSuchElementException("No matching element found during traversal")

  /**
   * Traverses through the items and returns the first item that satisfies the given block,
   * or null if no such item is found.
   *
   * @param block The predicate that checks whether an item matches.
   * @return The first item that matches the predicate, or null if none is found.
   */
  fun firstOrNull(block: (T) -> Boolean): T? {
    var result: T? = null
    traverseUntil {
      if (block(this)) {
        result = this
      }
      result != null
    }
    return result
  }

  /**
   * Finds the first item that satisfies the given block, or null if no such item is found.
   *
   * @param block The predicate that checks whether an item matches.
   * @return The first matching item, or null if none is found.
   */
  fun find(block: (T) -> Boolean): T? = firstOrNull(block)
}
