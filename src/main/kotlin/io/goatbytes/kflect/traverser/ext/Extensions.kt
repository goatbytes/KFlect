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

package io.goatbytes.kflect.traverser.ext

import io.goatbytes.kflect.ext.companionFunctions
import io.goatbytes.kflect.ext.companionProperties
import io.goatbytes.kflect.ext.topLevelFunctions
import io.goatbytes.kflect.ext.topLevelMembers
import io.goatbytes.kflect.ext.topLevelProperties
import io.goatbytes.kflect.traverser.Traverser
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.full.superclasses

/**
 * An extension property for the [Class] type that returns a [Traverser] for traversing
 * class hierarchies.
 */
val Class<*>.traverser: Traverser<Class<*>>
  get() = Traverser { block ->
    var javaClass: Class<*>? = this@traverser
    while (javaClass != null && !javaClass.block()) {
      javaClass = javaClass.superclass
    }
  }

/**
 * An extension property for [KClass] that provides a traverser for [properties][KProperty]
 * within the class.
 */
val KClass<*>.propertyTraverser: Traverser<KProperty<*>>
  get() = Traverser { block ->
    listOf(
      { memberProperties },
      { staticProperties },
      { companionProperties },
      { topLevelProperties }
    ).forEach { lazy ->
      val properties = lazy()
      properties.forEach { property ->
        if (property.block()) return@Traverser
      }
    }
  }

/**
 * An extension property for [KClass] that provides a traverser for [callables][KCallable]
 * within the class.
 */
val KClass<*>.callableTraverser: Traverser<KCallable<*>>
  get() = Traverser { block ->
    listOf(
      { members },
      { companionObject?.members ?: emptyList() },
      { topLevelMembers }
    ).forEach { lazy ->
      val callables = lazy()
      callables.forEach { callable ->
        if (callable.block()) return@Traverser
      }
    }
  }

/**
 * An extension property for [KClass] that provides a traverser for [functions][KFunction]
 * within the class.
 */
val KClass<*>.functionTraverser: Traverser<KFunction<*>>
  get() = Traverser { block ->
    listOf(
      { functions },
      { constructors },
      { companionFunctions },
      { topLevelFunctions }
    ).forEach { lazy ->
      val functions = lazy()
      functions.forEach { function ->
        if (function.block()) return@Traverser
      }
    }
  }

/**
 * An extension property for [KClass] that provides a traverser for traversing the class hierarchy.
 */
val KClass<*>.traverser: Traverser<KClass<*>>
  get() = Traverser { block ->
    if (!block()) {
      for (superclass in superclasses) {
        if (superclass.block()) {
          break
        }
      }
    }
  }
