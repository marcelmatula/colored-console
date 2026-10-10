import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinToolingSetupTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
}

val jdkVersion = 25

kotlin {
    jvmToolchain(jdkVersion)
    jvm()
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

tasks.register<JavaExec>("readmeImages") {
    description = "Renders the output of the README examples to .images/*.svg."
    group = "documentation"
    val test = kotlin.jvm().compilations["test"]
    classpath(test.output.allOutputs, test.runtimeDependencyFiles)
    mainClass = "readme.ReadmeImagesKt"
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(jdkVersion) }
    args(layout.projectDirectory.dir(".images").asFile.absolutePath)
}
