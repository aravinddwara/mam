dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

plugins {
    id("cloudstream.plugin")
}

version = "1.0.0"
cloudstream {
    language = "ta"
    description = "Watch Tamil serials and shows from Vijay TV, Sun TV, Zee Tamil, and more."
    authors = listOf("YourName")
}


    // Random CC logo I found
    iconUrl = "https://upload.wikimedia.org/wikipedia/commons/2/2f/Korduene_Logo.png"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
