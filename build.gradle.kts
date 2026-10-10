import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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

tasks.register<JavaExec>("readmeImages") {
    description = "Renders the output of the README examples to .images/*.svg."
    group = "documentation"
    val test = kotlin.jvm().compilations["test"]
    classpath(test.output.allOutputs, test.runtimeDependencyFiles)
    mainClass = "readme.ReadmeImagesKt"
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(jdkVersion) }
    args(layout.projectDirectory.dir(".images").asFile.absolutePath)
}
