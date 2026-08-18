import com.android.build.api.dsl.ApplicationExtension
import com.jyodroid.kunasismoayuda.convention.configureKotlinAndroid
import com.jyodroid.kunasismoayuda.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    targetSdk =
                        libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                }

                packaging {
                    resources {
                        excludes += "META-INF/{AL2.0, LGPL2.1}"
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }

                configureKotlinAndroid(this)
            }
        }
    }
}
