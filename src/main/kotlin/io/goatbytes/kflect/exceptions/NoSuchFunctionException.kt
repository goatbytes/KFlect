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

package io.goatbytes.kflect.exceptions

import io.goatbytes.kflect.KParameterTypes
import io.goatbytes.kflect.KotlinClass
import io.goatbytes.kflect.ext.name
import io.goatbytes.kflect.ext.signature
import kotlin.reflect.KClass

/**
 * An exception that is thrown when a specified function cannot be found in the target class.
 *
 * This exception is intended for use when a reflective operation fails to find a function or method
 * by its name and parameters in the specified class.
 *
 * @constructor Creates a [NoSuchFunctionException] with a custom message and optional underlying
 * cause.
 *
 * @param message The message that provides more details about the exception.
 * @param exception An optional underlying cause of the exception.
 */
class NoSuchFunctionException(message: String? = null, exception: Exception? = null) :
  ReflectiveOperationException(message, exception) {

  /**
   * Secondary constructor that builds the exception message based on the class, function name, and
   * parameter types.
   *
   * @param kClass        The [KClass] in which the function was expected.
   * @param name          The name of the function that was not found.
   * @param extensionType The extension function type or null if not an extension function.
   * @param parameters    The parameter types of the function that was not found.
   * @param exception     An optional underlying cause of the exception.
   */
  internal constructor(
    kClass: KClass<*>,
    name: String,
    extensionType: KotlinClass? = null,
    parameters: Array<out KotlinClass> = emptyArray(),
    exception: Exception? = null
  ) : this(
    "'${functionName(name, extensionType, parameters)}' does not exist in '${kClass.name}'",
    exception
  )

  private companion object {
    private fun functionName(name: String, extType: KotlinClass?, paramTypes: KParameterTypes) =
      "fun ${if (extType != null) "${extType.name}." else ""}$name${paramTypes.signature()}"
  }
}
