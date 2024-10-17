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

import build.Version.Identifier.Alpha
import build.Version.Identifier.Beta
import build.Version.Identifier.RC
import build.Version.Identifier.SNAPSHOT

/**
 * Represents a base class for different versioning strategies.
 */
sealed class Version {

  /** Represents the version as a human-readable string. */
  abstract val name: String

  /** Represents the version as a unique, sortable numeric code. */
  abstract val code: Long

  /** Enumerates common pre-release identifiers for software versions. */
  enum class Identifier {
    Alpha, Beta, RC, Release, SNAPSHOT
  }

  /**
   * Contains metadata related to the build or version, including Git information and build timing.
   *
   * @property buildNumber An incremental number representing the sequence of the build.
   * @property gitSha The Git SHA hash of the commit from which the build was created.
   * @property gitBranch The name of the Git branch from which the build was created.
   * @property buildTime The timestamp representing when the build was created.
   */
  data class Metadata(
    val buildNumber: Int?,
    val gitSha: String?,
    val gitBranch: String?,
    val buildTime: String?,
  )

  /**
   * Represents a semantic version, including major, minor, and patch levels, along with optional
   * pre-release identifiers and metadata.
   *
   * @property major The major version number, indicating incompatible API changes.
   * @property minor The minor version number, indicating added functionality in a backwards-compatible manner.
   * @property patch The patch version number, indicating backwards-compatible bug fixes.
   * @property identifier An optional pre-release identifier to denote version stability.
   * @property metadata Optional additional metadata providing context about the build or version.
   */
  data class Semantic(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val identifier: Identifier? = null,
    val metadata: Metadata? = null
  ) : Version() {

    /**
     * True if the [identifier] is [SNAPSHOT].
     */
    val isSnapshot: Boolean get() = identifier == SNAPSHOT

    override val name by lazy {
      "$major.$minor.$patch${preRelease()}${buildMetadata()}"
    }

    override val code: Long
      get() {
        val majorPart = major * MAJOR_MULTIPLIER
        val minorPart = minor * MINOR_MULTIPLIER
        val patchPart = patch * PATCH_MULTIPLIER
        val identifierPart = (identifier?.ordinal ?: 0) * IDENTIFIER_MULTIPLIER
        val buildNumberPart = metadata?.buildNumber?.toLong() ?: 0L
        return majorPart + minorPart + patchPart + identifierPart + buildNumberPart
      }

    override fun toString() = name

    private fun preRelease(): String = when (identifier) {
      Identifier.Release, null -> ""
      SNAPSHOT -> "-SNAPSHOT"
      else -> "-${identifier.name.lowercase()}"
    }

    private fun buildMetadata() = buildString {
      when (identifier) {
        Alpha, Beta, RC -> metadata?.run {
          var separator = "+"
          listOf(
            buildNumber?.takeIf { it > 0 },
            buildTime,
            gitBranch?.takeIf { it !in listOf("main") && !it.contains("/") },
            gitSha
          ).mapNotNull { it }.forEach { data ->
            append(separator).append(data).also { separator = "." }
          }
        }
        else -> {
          // no-op
        }
      }
    }

    private companion object {
      private const val MAJOR_MULTIPLIER = 1_000_000_000_000L
      private const val MINOR_MULTIPLIER = 1_000_000_000L
      private const val PATCH_MULTIPLIER = 1_000_000L
      private const val IDENTIFIER_MULTIPLIER = 100_000L
    }
  }
}
