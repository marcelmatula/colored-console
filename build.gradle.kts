import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockStoreTask

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

// kotlinWasmStoreYarnLock takes build/wasm/yarn.lock as input. The Wasm tests have no npm dependencies, and yarn then does
// not always write that file (one Windows CI run had none), so Gradle failed the task on the missing input. With nothing
// to store, skip it.
tasks.withType<YarnLockStoreTask>().configureEach {
    if (name == "kotlinWasmStoreYarnLock") {
        val lock = inputFile
        onlyIf("yarn wrote the Wasm lock file") { lock.get().asFile.exists() }
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
