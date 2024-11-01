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

package external

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * A class for executing commands in Gradle and capturing their output.
 *
 * This class provides a convenient way to run commands within Gradle builds
 * and access the standard output, standard error, and exit code of the executed command.
 */
class CommandLine(private val project: Project) {

  /**
   * Executes a command in the shell and returns the result.
   *
   * This function takes a variable number of strings as arguments, representing
   * the command and its arguments. It constructs a [Source] object with these arguments
   * and retrieves the execution result using Gradle's provider mechanism.
   *
   * @param command The command and its arguments as a list of strings.
   * @return A [Result] object containing the standard output, standard error,
   * and exit code of the executed command.
   */
  fun execute(vararg command: String): Result = project.providers.of(Source::class.java) {
    parameters.commands.set(listOf("sh", "-c") + command.toList())
  }.get()

  /**
   * An interface extending ValueSourceParameters, meant to specify parameters for
   * [CommandLineValueSource]. It includes a commands parameter, representing the executable and
   * its arguments as a list of strings.
   *
   * Example:
   * To execute a git status command:
   *
   * ```kotlin
   * spec.parameters.commands.set(listOf("git", "status"))
   * ```
   */
  interface Params : ValueSourceParameters {
    /** A list property containing the command and its arguments as strings. */
    val commands: ListProperty<String>
  }

  /**
   * The result of executing a command in a shell.
   *
   * @property stdout The standard output
   * @property stderr The standard error
   * @property exitCode The exit status of the command
   */
  data class Result(
    val stdout: String,
    val stderr: String,
    val exitCode: Int
  ) {
    /** If the command returned a success status code. */
    val isSuccess = exitCode == 0
  }

  /**
   * This ValueSource implementation allows for the execution of command-line operations within
   * Gradle's configuration phase and captures their output. Utilizing Gradle's ExecOperations, it
   * executes a specified command line and returns the output as a string.
   *
   * Usage:
   *
   * ```kotlin
   * val commandOutput: String = project.providers.of(CommandLine.Source::class.java) {
   *   parameters.commands.set(listOf("echo", "Hello, World!"))
   * }.get()
   * println("Command Output: $commandOutput")
   * ```
   */
  abstract class Source : ValueSource<Result, Params> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): Result {
      val stdout = ByteArrayOutputStream()
      val stderr = ByteArrayOutputStream()
      try {
        val result = execOperations.exec {
          commandLine(parameters.commands.get())
          environment(System.getenv())
          standardOutput = stdout
          errorOutput = stderr
        }
        return Result(
          stdout = stdout.toString(Charsets.UTF_8).trim(),
          stderr = stderr.toString(Charsets.UTF_8).trim(),
          exitCode = result.exitValue
        )
      } finally {
        stdout.close()
        stderr.close()
      }
    }
  }
}
