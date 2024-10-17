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

@file:Suppress("detekt.all")

package io.goatbytes.kflect

// Top-level function
fun topLevelFunction(input: String): String = "TopLevel: $input"

// Extension function
fun String.extensionFunction(): String = "Extension: $this"

class KotlinReflectionTestClass {

  // Companion object with static-like behavior
  companion object {
    const val COMPANION_CONST = "companionConst"
    var companionProperty = "companionProperty"

    fun companionFunction(): String = "Companion Function"
  }

  fun String.extensionFunction() {
    // no-op
  }

  // Properties and fields
  var mutableProperty: String = "Mutable Property"
  val immutableProperty: String = "Immutable Property"
  private var privateProperty: String = "Private Property"
  lateinit var lateInitProperty: StringBuilder
  var nullableProperty: String? = null
  val nonNullableProperty: String = "Non Nullable Property"

  // Member functions
  fun memberFunction(input: String): String = "Member: $input"
  suspend fun suspendFunction(): String = "Suspend Function"
  infix fun infixFunction(input: String): String = "Infix: $input"
  operator fun plus(input: String): String = "Operator + $input"

  // Private member function
  private fun privateFunction(input: String): String = "Private: $input"

  // Overloaded functions
  fun overloadedFunction(): String = "No arguments"
  fun overloadedFunction(input: String): String = "Argument: $input"
}
