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

package publication

import BuildConfig.Publishing
import org.gradle.api.Project
import java.io.File

/**
 * Represents the configuration for signing artifacts, encapsulating necessary details.
 */
sealed class SigningConfig {

  /**
   * The credentials for the signing configuration.
   */
  abstract val credentials: Credentials

  /**
   * Specific signing configuration for the release publication.
   *
   * @property credentials The credentials for the signing configuration.
   */
  class Release(override val credentials: Credentials) : SigningConfig() {

    /**
     * Companion object to get the signing config for the project.
     */
    companion object {
      /**
       * Get the [Release] [SigningConfig].
       *
       * @param project The root project
       * @return The release signing config or null if the environment is not setup to sign releases
       */
      @Suppress("TooGenericExceptionCaught")
      operator fun get(project: Project): Release? {
        return try {
          val file = File(project.prop(Publishing.ENV_NAME_CREDENTIALS))
          val credentials = Credentials.from(file) ?: return null
          Release(credentials)
        } catch (e: Exception) {
          null
        }
      }
    }
  }

  /**
   * Singing credentials.
   *
   * @property keyId The ID of the signing key.
   * @property keyRing The location of the keyring containing the signing key.
   * @property password The password for accessing the signing key.
   */
  data class Credentials(
    val keyId: String,
    val keyRing: String,
    val password: String
  ) {
    /**
     * Companion object to retrieve credentials from a file.
     */
    companion object {
      /**
       * Load the credentials from a file.
       *
       * @param file The file containing the credentials
       * @return The [Credentials] or null if it failed to parse the credentials.
       */
      fun from(file: File): Credentials? {
        var keyId: String? = null
        var keyring: String? = null
        var password: String? = null
        file.forEachLine { line ->
          if (line.contains("=")) {
            val key = line.substringBefore("=")
            val value = line.substringAfter("=")
            when (key) {
              KEY_ID -> keyId = value
              KEYRING -> keyring = value
              PASSWORD -> password = value
            }
          }
        }
        return let(keyId, keyring, password) { id, key, pass ->
          Credentials(id, key, pass)
        }
      }
    }
  }

  private companion object {
    private const val KEY_ID = "SIGNING_KEY_ID"
    private const val KEYRING = "SIGNING_KEYRING"
    private const val PASSWORD = "SIGNING_PASSWORD"

    private fun <A, B, C, D> let(a: A?, b: B?, c: C?, block: (a: A, b: B, c: C) -> D): D? {
      return if (a != null && b != null && c != null) block(a, b, c) else null
    }

    private infix fun Project.prop(name: String): String {
      return (System.getenv(name) ?: findProperty(name)) as String
    }
  }
}
