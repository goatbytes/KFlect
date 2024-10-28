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

package io.goatbytes.kflect.classes

import io.goatbytes.kflect.DEBUG
import io.goatbytes.kflect.lazy.SynchronizedLazyKFlect
import java.util.*

/**
 * Provides an implementation of [ClassNameLoader] for Android. The `AndroidClassNameLoader` object
 * is responsible for loading class names from the DEX files within an Android APK.
 *
 * This implementation uses reflection to retrieve the `sourceDir` of the currently running
 * application and accesses class names through the `dalvik.system.DexFile.entries` method.
 */
internal object AndroidClassNameLoader : ClassNameLoader {

  /*
   * Lazy-loaded property that retrieves the list of class names in the APK's DEX file.
   * Uses synchronized lazy initialization to safely load class names only once per instance.
   *
   * The `SynchronizedLazyKFlect` provides thread-safety for this lazy-loading process.
   * If retrieval fails, an empty list is returned.
   */
  private val classNames by SynchronizedLazyKFlect {
    try {
      "dalvik.system.DexFile".newInstance<Any>(
        "android.app.ActivityThread"("currentApplication")("getApplicationInfo")["sourceDir"]
      ).require<Enumeration<String>>("entries").toList()
    } catch (_: Throwable) {
      if (DEBUG) {
        System.err.println("Error retrieving class names from APK")
      }
      emptyList()
    }
  }

  /**
   * Loads and returns the class names present in the APK's DEX file.
   *
   * @return A list of fully qualified class names from the APK, or an empty list if loading fails.
   */
  override fun loadClassNames(): List<String> = classNames
}
