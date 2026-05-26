import june.wing.GroupIdMavenCentral
import june.wing.publishAndroidMavenCentral

plugins {
    id("com.android.library")
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.gene.compose)
    alias(vcl.plugins.compose.compiler)
}

group = GroupIdMavenCentral
version = wings.versions.gene.cubic.get()

android {
    namespace = "osp.spark.cubic"
}

publishAndroidMavenCentral("cubic")

dependencies {
//    implementation("androidx.compose.runtime:runtime:1.11.2")
    implementation(vcl.gene.cartoon)
    platform(vcl.androidx.compose.bom)
//    implementation(vcl.androidx.compose.ui)
//    implementation(vcl.androidx.compose.ui.tooling)
//    implementation(vcl.androidx.compose.ui.tooling.preview)
//    implementation(vcl.androidx.compose.foundation)
//    implementation(vcl.androidx.compose.material)
//    implementation(wings.androidx.compose.material3)
//    implementation(vcl.androidx.compose.animation)
}