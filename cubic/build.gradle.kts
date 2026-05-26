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
    implementation(vcl.gene.cartoon)
}