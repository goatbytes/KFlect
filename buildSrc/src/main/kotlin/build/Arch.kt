/*
 * Copyright 2024 GoatBytes.IO
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package build

/**
 * Represents the architecture types that can be identified.
 */
enum class Arch {
  X64, Arm32, Arm64;

  /**
   * Companion object to get the current architecture.
   */
  companion object {
    /** The current architecture. */
    val current: Arch by lazy {
      val arch = System.getProperty("os.arch").lowercase()
      when {
        "amd64" in arch || "x86_64" in arch -> X64
        "arm" in arch || "arm32" in arch -> Arm32
        "aarch64" in arch -> Arm64
        else -> error("Unsupported build environment: $arch")
      }
    }
    val isX64 get() = current == X64
    val isArm32 get() = current == Arm32
    val isArm64 get() = current == Arm64
  }
}
