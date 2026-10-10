import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinToolingSetupTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.mavenPublish)
}

group = "io.github.marcelmatula"
// The publish workflow passes the release tag without its "v" (-PreleaseVersion=1.4.0).
version = providers.gradleProperty("releaseVersion").getOrElse("0.0.0-SNAPSHOT")

repositories {
    mavenCentral()
}

val jdkVersion = 25

kotlin {
    jvmToolchain(jdkVersion)
    jvm {
        // The published jar runs on Java 8, like the Kotlin standard library. Only the main code is limited to Java 8
        // bytecode and API: the README examples in jvmTest use newer APIs (Console.isTerminal) and run on Java 25.
        compilations.named("main") {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_1_8
                    freeCompilerArgs.add("-Xjdk-release=1.8")
                }
            }
        }
    }
    js {
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
    linuxX64()
    mingwX64()
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// With the configuration cache on, Gradle runs the tasks of a project in parallel, so the JS and Wasm yarn installs ran at
// the same time. On Windows CI that sometimes made yarn succeed without installing anything (no mocha for the JS tests,
// no yarn.lock for Wasm). Let only one task run yarn at a time.
abstract class YarnSerializer : BuildService<BuildServiceParameters.None>

val yarnSerializer = gradle.sharedServices.registerIfAbsent("yarnSerializer", YarnSerializer::class) {
    maxParallelUsages = 1
}
tasks.withType<KotlinNpmInstallTask>().configureEach { usesService(yarnSerializer) }
tasks.withType<KotlinToolingSetupTask>().configureEach { usesService(yarnSerializer) }

mavenPublishing {
    // Uploads to the Central Portal; the release is published by hand there.
    publishToMavenCentral()
    // The publish workflow always passes the key (and fails without it); local publishing works unsigned.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) signAllPublications()
    coordinates(artifactId = "colored-console")
    pom {
        name = "Colored Console"
        description = "A small Kotlin DSL for printing colored and styled text to the terminal using ANSI escape codes."
        inceptionYear = "2019"
        url = "https://github.com/marcelmatula/colored-console"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "marcelmatula"
                name = "Marcel Matula"
                url = "https://github.com/marcelmatula"
            }
        }
        scm {
            url = "https://github.com/marcelmatula/colored-console"
            connection = "scm:git:https://github.com/marcelmatula/colored-console.git"
            developerConnection = "scm:git:ssh://git@github.com/marcelmatula/colored-console.git"
        }
    }
}

tasks.register<JavaExec>("readmeImages") {
    description = "Renders the output of the README examples to .images/*.svg."
    group = "documentation"
    val test = kotlin.jvm().compilations["test"]
    classpath(test.output.allOutputs, test.runtimeDependencyFiles)
    mainClass = "readme.ReadmeImagesKt"
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(jdkVersion) }
    args(layout.projectDirectory.dir(".images").asFile.absolutePath)
}
