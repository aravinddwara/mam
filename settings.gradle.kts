rootProject.name = "CloudstreamPlugins"

// Add plugin repositories
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

val disabled = listOf<String>()

File(rootDir, ".").eachDir { dir ->
    if (!disabled.contains(dir.name) && File(dir, "build.gradle.kts").exists()) {
        include(dir.name)
    }
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}

// Example to only include one plugin:
// include("MyPlugin")
