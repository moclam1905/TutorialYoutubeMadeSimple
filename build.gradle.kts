import org.jetbrains.kotlin.gradle.dsl.JvmTarget
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

private val bytecodeVersion = JavaVersion.toVersion(libs.versions.jvmBytecode.get())
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(bytecodeVersion.toString()))
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlin.time.ExperimentalTime"
                )
            )
        }
    }
}

