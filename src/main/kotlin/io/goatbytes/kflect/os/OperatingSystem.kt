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

package io.goatbytes.kflect.os

import java.io.File

/**
 * Enum class representing various operating systems.
 *
 * This enum provides a method to detect the operating system in use and is primarily
 * based on the "os.name" system property. The OS types identified include:
 * - Windows
 * - MacOSX
 * - Linux
 * - Android
 * - IOS
 * - Solaris
 * - BSD
 * - Unix
 * - Nintendo
 * - Playstation
 * - XBox
 * - Unknown (for unsupported or unidentified systems)
 */
enum class OperatingSystem {
  Windows,
  MacOS,
  Linux,
  Android,
  IOS,
  Solaris,
  BSD,
  Unix,
  Nintendo,
  Playstation,
  XBox,
  Unknown;

  /**
   * Companion object to retrieve the current operating system.
   */
  companion object {
    /**
     * Determines the current operating system by evaluating the "os.name" system property.
     *
     * The method applies OS-specific conditions to classify the detected system as one of the
     * known [OperatingSystem] values, based on keywords in the OS name string or filesystem
     * properties. If no match is found, it defaults to [Unknown].
     *
     * @return The identified [OperatingSystem] enum value.
     */
    fun get(): OperatingSystem {
      val osName = System.getProperty("os.name", "unknown").lowercase()
      return when {
        File.separatorChar == '\\' || osName.contains("win") -> Windows
        osName.contains("android") -> Android
        osName.contains("ios") || osName.contains("iphone") -> IOS
        osName.contains("mac") || osName.contains("darwin") -> MacOS
        osName.contains("nux") -> Linux
        osName.contains("sunos") || osName.contains("solaris") -> Solaris
        osName.contains("bsd") -> BSD
        osName.contains("nix") || osName.contains("aix") -> Unix
        osName.contains("nintendo") -> Nintendo
        osName.contains("playstation") -> Playstation
        osName.contains("xbox") -> XBox
        else -> Unknown
      }
    }
  }
}
