plugins {
    id("cloudstream.plugin") version "0.0.1"
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 1

cloudstream {
    language = "ta"
    description = "Tamil TV serials and shows from TamilDhool with Dailymotion support"
    authors = listOf("TamilDhool")

    status = 1
    tvTypes = listOf("TvSeries", "Movie")
    iconUrl = "https://www.tamildhool.net/wp-content/uploads/2020/08/cropped-tamildhool-favicon-32x32.png"
    requiresResources = false // Set to true if your plugin has assets
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
