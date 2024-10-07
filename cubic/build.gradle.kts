import wing.publishMavenCentral

plugins {
    id("com.android.library")
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.gene.compose)
}

group = "io.github.5gene"
version = "1.0"

android {
    namespace = "osp.sparkj.cubic"
}

publishMavenCentral("cubic", withSource = true)

dependencies {
    implementation(wings.gene.cartoon)
}