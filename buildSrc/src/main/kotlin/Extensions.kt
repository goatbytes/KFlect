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

@file:Suppress("Filename")

import build.CIPlatform
import org.gradle.api.Project
import java.util.*

val Project.gitBranchName: String
  get() = CIPlatform.detect()?.getBranchName() ?: exec(
    "git branch --show-current || git symbolic-ref --short HEAD || echo 'unknown'"
  )

val Project.gitCommitSha: String
  get() = CIPlatform.detect()?.getCommitSha() ?: exec(
    "git --no-pager log -1 --format=%h || git rev-parse --short HEAD || echo 'unknown'"
  )

/**
 * Executes a single command line specified as a string.
 *
 * @param command string representing the command to execute and its arguments, separated by spaces.
 * @return The command's output as a string.
 */
fun Project.exec(command: String) = external.CommandLine(this).execute(command).stdout

/**
 * Returns a copy of this string having its first letter title-cased using the rules of the default
 * locale, or the original string if it's empty or already starts with a title case letter.
 */
fun String.capitalize() =
  replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/**
 * Verify the environment is configured with maven credentials.
 *
 * @return True if `OSSRH_USERNAME` and `OSSRH_PASSWORD` environment variables exist.
 */
fun hasMavenCredentials(): Boolean {
  return listOf("OSSRH_USERNAME", "OSSRH_PASSWORD").all { System.getenv(it) != null }
}
