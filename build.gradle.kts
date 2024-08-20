plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
//        commonTest.dependencies {
//            implementation("kotlin.test")
//        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
