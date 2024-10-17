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

@file:Suppress("FunctionName")

package publication

import BuildConfig
import BuildConfig.Developer
import BuildConfig.Publishing
import BuildConfig.SCM
import java.net.URI

plugins {
  `maven-publish`
  signing
}

afterEvaluate {
  val ossrhUsername = System.getenv("OSSRH_USERNAME")
  val ossrhPassword = System.getenv("OSSRH_PASSWORD")
  val signingConfig = SigningConfig.Release[project]
  if (ossrhUsername == null || ossrhPassword == null || signingConfig == null) {
    return@afterEvaluate
  }

  publishing {
    repositories {
      maven {
        name = Publishing.MAVEN_NAME
        url = if (BuildConfig.VERSION.isSnapshot) {
          URI(Publishing.MAVEN_SNAPSHOT_URL)
        } else {
          URI(Publishing.MAVEN_URL)
        }
        credentials {
          username = ossrhUsername
          password = ossrhPassword
        }
      }
    }
    publications {
      create<MavenPublication>("mavenJava") {
        from(components["java"])
        groupId = BuildConfig.GROUP
        artifactId = Publishing.ARTIFACT_ID
        version = BuildConfig.VERSION.name
        artifact(tasks["javadocJar"]) {
          classifier = "javadoc"
        }
        artifact(tasks["sourcesJar"]) {
          classifier = "sources"
        }
        pom {
          name.set(Publishing.NAME)
          description.set(Publishing.DESCRIPTION)
          url.set(Publishing.URL)
          licenses {
            license {
              name.set(BuildConfig.License.NAME)
              url.set(BuildConfig.License.URL)
              distribution.set("repo")
            }
          }
          developers {
            developer {
              id.set(Developer.ID)
              name.set(Developer.NAME)
              email.set(Developer.EMAIL)
              url.set(Developer.URL)
              timezone.set(Developer.TIMEZONE)
            }
          }
          scm {
            url.set("https://${SCM.PATH}/")
            connection.set("scm:git:git://${SCM.PATH}.git")
            developerConnection.set("scm:git:ssh://git@${SCM.PATH}.git")
          }
        }
      }
    }
  }
  signing {
    SigningConfig.Release[project]?.credentials?.run {
      useInMemoryPgpKeys(keyId, keyRing, password)
      sign(publishing.publications)
    }
  }
}
