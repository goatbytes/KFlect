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

import io.goatbytes.kflect.KotlinClass

/**
 * Exception thrown when a specified property is not found in the class hierarchy of the given
 * [KotlinClass]. This exception extends [ReflectiveOperationException] and is used for
 * reflection-based property access.
 *
 * @constructor Creates an instance of [NoSuchPropertyException] with a custom message and a cause.
 * @param message The detail message for the exception, which can be `null`.
 * @param cause The underlying cause of the exception, which can be `null`.
 */
class NoSuchPropertyException(message: String? = null, cause: Exception? = null) :
  ReflectiveOperationException(message, cause)
