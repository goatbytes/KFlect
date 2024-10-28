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

package io.goatbytes.kflect.predicates

/**
 * A functional interface that represents a predicate (boolean-valued function) of one argument.
 * It provides default methods for common logical operations (AND, OR, NOT) and operator overloads.
 *
 * @param T the type of input to the predicate
 */
fun interface Predicate<T> {

  /**
   * Evaluates the predicate on the given value.
   *
   * @param value the input value to evaluate
   * @return `true` if the input matches the predicate, `false` otherwise
   */
  fun test(value: T): Boolean

  /**
   * Combines this predicate with another predicate using a logical AND.
   *
   * @param other the other predicate to combine with this one
   * @return a new predicate that represents the logical AND of this predicate and the other.
   */
  infix fun and(other: Predicate<T>): Predicate<T> = Predicate { test(it) && other.test(it) }

  /**
   * Combines this predicate with another predicate using a logical OR.
   *
   * @param other the other predicate to combine with this one
   * @return a new predicate that represents the logical OR of this predicate and the other.
   */
  infix fun or(other: Predicate<T>): Predicate<T> = Predicate { test(it) || other.test(it) }

  /**
   * Negates this predicate.
   *
   * @return a new predicate that represents the negation of this predicate
   */
  operator fun not(): Predicate<T> = Predicate { !test(it) }

  /**
   * Evaluates the predicate on the given value using the `invoke` operator.
   * This allows calling the predicate as a function, like `predicate(value)`.
   *
   * @param value the input value to evaluate
   * @return `true` if the input matches the predicate, `false` otherwise
   */
  operator fun invoke(value: T): Boolean = test(value)

  /**
   * Combines this predicate with another predicate using a logical AND (`+` operator).
   * This is an overload of the `and` function for operator use.
   *
   * @param other the other predicate to combine with this one
   * @return a new predicate that represents the logical AND of this predicate and the other.
   */
  operator fun plus(other: Predicate<T>): Predicate<T> = this and other

  /**
   * The companion object for functions to chain predicates.
   */
  companion object {
    /**
     * Combines multiple predicates using logical AND.
     *
     * @param T the type of input to the predicate
     * @param predicates A list of predicates to combine.
     * @return A combined predicate that returns true only if all predicates are true.
     */
    fun <T> allOf(vararg predicates: Predicate<T>): Predicate<T> = Predicate { value ->
      predicates.all { predicate -> predicate(value) }
    }

    /**
     * Combines multiple predicates using logical OR.
     *
     * @param T the type of input to the predicate
     * @param predicates A list of predicates to combine.
     * @return A combined predicate that returns true if any predicate is true.
     */
    fun <T> anyOf(vararg predicates: Predicate<T>): Predicate<T> = Predicate { value ->
      predicates.any { predicate -> predicate(value) }
    }
  }
}
