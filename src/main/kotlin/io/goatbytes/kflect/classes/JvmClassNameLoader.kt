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

import io.goatbytes.kflect.dsl.tryOrNull
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.stream.Collectors

/**
 * The `JvmClassNameLoader` object provides functionality to load class names in a JVM environment.
 * It identifies class files within directories, JAR files, and certain OSGi bundles on the
 * classpath, consolidating all available class names across various class loaders.
 */
internal object JvmClassNameLoader : ClassNameLoader {

  /**
   * Loads all available class names from the classpath, including classes in JAR files
   * and directories associated with each class loader in the JVM.
   *
   * @return A list of fully qualified class names.
   */
  override fun loadClassNames(): List<String> =
    classLoaders.flatMap { loader -> loader.scanClassNames() }.toSet().toList()

  /*
   * Collects a set of relevant class loaders, including system class loader,
   * context class loader, this class's class loader, OSGi class loader,
   * and all class loaders found in the call stack.
   */
  private val classLoaders: Set<ClassLoader>
    get() = buildSet {
      addIfNotNull(ClassLoader.getSystemClassLoader())
      addIfNotNull(Thread.currentThread().contextClassLoader)
      addIfNotNull(this::class.java.classLoader)
      addIfNotNull(osgiClassLoader)
      addAll(callStackClassLoaders)
    }

  private val osgiClassLoader: ClassLoader? by lazy {
    tryOrNull { Class.forName(OSGI_CLASS_LOADER_NAME).classLoader }
  }

  private val callStackClassLoaders
    get() = Thread.currentThread().stackTrace.mapNotNull { element ->
      tryOrNull { Class.forName(element.className).classLoader }
    }.toSet()

  private val ClassLoader.files: List<File>
    get() = when (this) {
      is URLClassLoader -> urLs.mapNotNull { url -> tryOrNull { File(url.toURI()) } }
      else -> JAVA_CLASS_PATH.split(File.pathSeparator).map { path -> File(path) }
    }

  private fun ClassLoader.scanClassNames(): Set<String> =
    files.parallelStream()
      .flatMap { file -> file.scanClassFiles().stream() }
      .collect(Collectors.toSet())

  private fun File.scanClassFiles(): List<String> = when {
    isDirectory -> scanDirectory(this)
    isJarFile -> scanJarFile(this)
    else -> emptyList()
  }

  private fun scanDirectory(directory: File): List<String> =
    directory.walkTopDown().filter { file -> file.isClassFile() && !file.isMetaInf() }
      .map { file -> file.toClassName(directory) }.toList()

  private fun scanJarFile(jarFile: File): List<String> =
    JarFile(jarFile).use { jar ->
      jar.entries().asSequence().filter { entry ->
        entry.isClassFile() && !entry.isMetaInf()
      }.map { entry -> entry.toClassName() }.toList()
    }

  private fun File.isClassFile(): Boolean = extension == FileExt.CLASS
  private fun JarEntry.isClassFile(): Boolean = extension == FileExt.CLASS

  private fun File.toClassName(directory: File): String =
    path.substring(directory.path.length + 1)
      .replace(File.separatorChar, '.')
      .removeSuffix(".${FileExt.CLASS}")

  private fun JarEntry.toClassName(): String =
    name.replace(File.separatorChar, '.').removeSuffix(".${FileExt.CLASS}")

  private val File.isJarFile: Boolean
    get() = extension == FileExt.JAR

  private val JarEntry.extension: String
    get() = name.substringAfterLast('.', "")

  private fun File.isMetaInf(): Boolean = name.contains(META_INF_VERSIONS_PATH)
  private fun JarEntry.isMetaInf(): Boolean = name.contains(META_INF_VERSIONS_PATH)

  private val JAVA_CLASS_PATH by lazy {
    try {
      System.getProperty("java.class.path", ".")
    } catch (_: SecurityException) {
      "."
    }
  }

  private fun <E> MutableCollection<E>.addIfNotNull(element: E?): Boolean =
    element?.let { add(it) } ?: false

  private object FileExt {
    const val JAR = "jar"
    const val CLASS = "class"
  }

  private const val OSGI_CLASS_LOADER_NAME = "org.osgi.framework.Bundle"
  private const val META_INF_VERSIONS_PATH = "META-INF/versions"
}
