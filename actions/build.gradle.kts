plugins {
    kotlin("jvm") version "1.3.72"
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("com.github.ajalt:clikt:2.7.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
