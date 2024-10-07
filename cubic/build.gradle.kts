import wing.publishMavenCentral

plugins {
    id("com.android.library")
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.compose.compiler)
}

group = "io.github.5gene"
version = wings.versions.cubic.get()

android {
    namespace = "osp.sparkj.cubic"
}

publishMavenCentral("cubic", withSource = true)

dependencies {
    implementation(wings.gene.cartoon)
    implementation(wings.androidx.compose.ui)
    implementation(wings.androidx.compose.ui.tooling)
    implementation(wings.androidx.compose.ui.tooling.preview)
    implementation(wings.androidx.compose.foundation)
    implementation(wings.androidx.compose.material)
//    implementation(wings.androidx.compose.material3)
    implementation(wings.androidx.compose.animation)
}