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
