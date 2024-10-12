import wing.GroupIdMavenCentral
import wing.publishMavenCentral

plugins {
    id("com.android.library")
    alias(vcl.plugins.gene.android)
    alias(vcl.plugins.compose.compiler)
}

group = GroupIdMavenCentral
version = wings.versions.gene.cubic.get()

android {
    namespace = "osp.sparkj.cubic"
}

publishMavenCentral("cubic", withSource = true)

dependencies {
    implementation(vcl.gene.cartoon)
    implementation(vcl.androidx.compose.ui)
    implementation(vcl.androidx.compose.ui.tooling)
    implementation(vcl.androidx.compose.ui.tooling.preview)
    implementation(vcl.androidx.compose.foundation)
    implementation(vcl.androidx.compose.material)
//    implementation(wings.androidx.compose.material3)
    implementation(vcl.androidx.compose.animation)
}