import com.jyodroid.kunasismoayuda.convention.configureAndroidTarget
import com.jyodroid.kunasismoayuda.convention.configureIosTargets
import com.jyodroid.kunasismoayuda.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class CmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.jyodroid.kunasismoayuda.convention.android.application.compose")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
            }

            configureIosTargets()
            configureAndroidTarget()

            dependencies {
                "debugImplementation"(libs.findLibrary("ui-tooling").get())
            }
        }
    }
}