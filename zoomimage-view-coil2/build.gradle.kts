plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

androidLibrary(nameSpace = "com.github.panpf.zoomimage.view.coil2") {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    api(projects.zoomimageView)
    api(projects.zoomimageCoreCoil2)

    androidTestImplementation(projects.internal.testUtils)
}