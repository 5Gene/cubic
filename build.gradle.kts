// Top-level build file where you can add configuration options common to all sub-projects/modules.
//@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(vcl.plugins.android.application) apply false
    alias(vcl.plugins.kotlin.jvm) apply false
    alias(vcl.plugins.kotlin.serialization) apply false
    alias(vcl.plugins.gene.compose) apply false
    alias(vcl.plugins.gene.android) apply false
    alias(vcl.plugins.compose.compiler) apply false
}

//buildscript {
////val compose_version by extra("1.2.1")
//    //    val kotlin_version = "1.7.10"
////    val compose_version = "1.2.1"
////    project.ext {
////        set("compose_version", "1.2.1")
////        set("kotlin_version", "1.7.10")
////    }
//    println(project.extensions.getByName("ext"))
//}
//println(" ================================================= jspark >> $project ${project.ext} ${project.ext.properties}")