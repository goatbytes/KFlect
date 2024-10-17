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

import io.goatbytes.kflect.DEBUG
import io.goatbytes.kflect.JavaClass
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.Type

/**
 * Predicates for reflection operations on fields.
 */
data object FieldPredicates : MemberPredicates<Field>() {

  /**
   * Checks if the field's type matches the given type.
   *
   * @param type The type to check against the field's type.
   * @return A [Predicate] that checks if the field's type matches the given type.
   */
  fun withType(type: Type) = predicate { field -> field.type == type }

  /**
   * Checks if the field's type is assignable from the given type.
   *
   * @param type The class to check if it's assignable from the field's type.
   * @return A [Predicate] that checks if the field's type is assignable from the provided type.
   */
  fun withTypeAssignableFrom(type: JavaClass) = predicate { field ->
    type.isAssignableFrom(field.type)
  }

  /**
   * Checks if the field's generic type matches the given generic type.
   *
   * @param genericType The generic type to check against the field's generic type.
   * @return A [Predicate] that checks if the field's generic type matches the given type.
   */
  fun withGenericType(genericType: Type) = predicate { field ->
    field.genericType == genericType
  }

  /**
   * Checks if the field's name and type match the given name and type.
   *
   * @param name The name of the field to check.
   * @param type The type of the field to check.
   * @return A [Predicate] that checks if the field's name and type match the given name and type.
   */
  fun withNameAndType(name: String, type: JavaClass) = predicate { field ->
    field.name == name && field.type == type
  }

  /**
   * Checks if the static field has a default value (null).
   *
   * @return A [Predicate] that checks if the static field has a default value of null.
   * @throws IllegalAccessException If the field cannot be accessed.
   */
  fun hasDefaultValue() = predicate { field ->
    Modifier.isStatic(field.modifiers) && try {
      @Suppress("DEPRECATION")
      if (!field.isAccessible) field.isAccessible = true
      field.get(null) == null
    } catch (e: IllegalAccessException) {
      if (DEBUG) {
        System.err.println("Error setting ${field.name} accessibility")
        e.printStackTrace()
      }
      false
    }
  }

  /**
   * Checks if the field is both static and final.
   *
   * @return A [Predicate] that checks if the field is both static and final.
   */
  fun isStaticFinal() = predicate { field ->
    Modifier.isStatic(field.modifiers) && Modifier.isFinal(field.modifiers)
  }
}
