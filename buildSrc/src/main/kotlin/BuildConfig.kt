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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Contains the configuration for the project.
 */
object BuildConfig {

  const val GROUP = "io.goatbytes"
  val VERSION by lazy { Version[project] }

  internal lateinit var project: Project

  /**
   * Version Update Instructions:
   *
   * - IDENTIFIER:    Indicate the current stage of release (Alpha, Beta, RC, or Release)
   *                  based on the software's readiness for deployment.
   * - MAJOR:         Increment for incompatible API changes. Resets minor and patch levels to 0.
   * - MINOR:         Increment for adding functionality in a backwards-compatible manner.
   *                  Resets the patch level to 0.
   * - PATCH:         Increment for making backwards-compatible bug fixes.
   * - BUILD_NUMBER:  Increment with each build to uniquely identify it.
   *
   * Refer to the Semantic Versioning specification at https://semver.org/ for detailed guidelines.
   */
  object Version {
    private const val MAJOR = 1
    private const val MINOR = 0
    private const val PATCH = 2
    private const val BUILD_NUMBER = 0
    private val IDENTIFIER = build.Version.Identifier.Release

    private val BUILD_TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH")

    /**
     * Generates the project version using semantic versioning.
     *
     * @param project The project
     * @return The project version
     */
    operator fun get(project: Project) = build.Version.Semantic(
      major = MAJOR,
      minor = MINOR,
      patch = PATCH,
      identifier = IDENTIFIER,
      metadata = build.Version.Metadata(
        buildNumber = BUILD_NUMBER,
        gitSha = project.gitCommitSha,
        gitBranch = project.gitBranchName,
        buildTime = LocalDateTime.now().format(BUILD_TIME_PATTERN)
      )
    )
  }

  /**
   * Initialize the [BuildConfig] to reference the root project.
   *
   * @param rootProject The root project
   * @return The [BuildConfig] for chaining any method calls.
   */
  fun initialize(rootProject: Project) = apply {
    this.project = rootProject
    rootProject.group = GROUP
    rootProject.version = VERSION
  }

  /**
   * Contains configuration for Detekt static analysis plugin.
   */
  object Detekt {
    val CONFIG by lazy { "${project.rootDir}/detekt.yml" }
    val jvmTarget = JavaVersion.VERSION_1_8.toString()
  }

  /**
   * Contains constants for Dokka.
   */
  object Dokka {
    val outputDirectory: File get() = project.file("${project.rootDir}/docs/docs")
  }

  /**
   * Defines constants for SCM (Software Configuration Management) related to the project.
   */
  object SCM {
    /** Host of the SCM. */
    const val HOST = "github.com"

    /** Organization name within the SCM. */
    const val ORG = "goatbytes"

    /** Project name within the SCM. */
    const val NAME = "KFlect"

    /** Full SCM path constructed from host, organization, and project name. */
    const val PATH = "$HOST/$ORG/$NAME"
  }

  /**
   * Contains the License name and URL for the project.
   */
  object License {
    const val NAME = "Apache 2.0"
    const val URL = "https://${SCM.PATH}/blob/main/LICENSE"
  }

  /**
   * Contains the developer metadata for the POM.
   */
  object Developer {
    const val ID = "goatbytes"
    const val NAME = "GoatBytes.IO"
    const val EMAIL = "engineering@goatbytes.io"
    const val URL = "https://goatbytes.io"
    const val TIMEZONE = "America/Los_Angeles"
  }

  internal object Publishing {
    const val ARTIFACT_ID = "kflect"
    const val NAME = "KFlect"
    const val DESCRIPTION = "Kotlin Reflection Library"
    const val URL = "https://${SCM.ORG}.github.io/${SCM.NAME}"
    const val MAVEN_NAME = "MavenCentral"
    const val MAVEN_URL = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val MAVEN_SNAPSHOT_URL = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    const val ENV_NAME_CREDENTIALS = "${ARTIFACT_ID}_CREDENTIALS"
  }
}
