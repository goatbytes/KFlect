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
  ReflectiveOperationException(message, exception)
