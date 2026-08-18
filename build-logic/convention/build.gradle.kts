import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


//https://developer.android.com/kotlin/multiplatform/kmp-integration Build custom Gradle plugins for Android KMP
//How to create a convention polugin for multi-module android app https://leonardobbai.medium.com/simplifying-multi-module-android-projects-with-convention-plugins-b7afdae6b777
plugins {
    `kotlin-dsl`
}

group = "com.jyodroid.kunasismoayuda.convention.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.jyodroid.kunasismoayuda.convention.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidComposeApplication") {
            id = "com.jyodroid.kunasismoayuda.convention.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("cmpApplication") {
            id = "com.jyodroid.kunasismoayuda.convention.cmp.application"
            implementationClass = "CmpApplicationConventionPlugin"
        }
    }
}