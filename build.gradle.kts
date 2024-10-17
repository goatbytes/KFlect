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

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import java.net.URI

BuildConfig.initialize(rootProject)

plugins {
  kotlin("jvm") version libs.versions.kotlin
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  id("publication.s01-oss-sonatype")
}

group = "io.goatbytes.kotlin"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("reflect"))
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  detektPlugins(libs.detekt.formatting)
}

detekt {
  buildUponDefaultConfig = true
  config.setFrom(BuildConfig.Detekt.CONFIG)
}

tasks.withType<Detekt>().configureEach {
  reports {
    html.required.set(true)
    md.required.set(true)
    sarif.required.set(false)
    txt.required.set(false)
    xml.required.set(false)
  }
}

tasks.withType<Detekt>().configureEach {
  jvmTarget = BuildConfig.Detekt.jvmTarget
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
  jvmTarget = BuildConfig.Detekt.jvmTarget
}

val detektAll by tasks.registering(Detekt::class) {
  description = "Run detekt analysis on entire project"
  parallel = true
  buildUponDefaultConfig = true
  config.setFrom(BuildConfig.Detekt.CONFIG)
  setSource(files(projectDir))

  include("**/*.kt", "**/*.kts")
  exclude("resources/", "*/build/*")
}

val javadocJar by tasks.registering(Jar::class) {
  archiveClassifier.set("javadoc")
  from(tasks["javadoc"])
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(project.sourceSets.main.get().allSource)
}

dokka {
  moduleName.set(BuildConfig.SCM.NAME)
  dokkaSourceSets.main {
    sourceLink {
      localDirectory.set(file("src/main/kotlin"))
      remoteUrl.set(URI("https://github.com/goatbytes/KFlect"))
      remoteLineSuffix.set("#L")
    }
  }
  pluginsConfiguration.html {
    customStyleSheets.from(".dokka/dokka.css")
    customAssets.from(".dokka/logo.svg")
    footerMessage.set("Copyright (c) 2024 GoatBytes.IO")
  }
}
