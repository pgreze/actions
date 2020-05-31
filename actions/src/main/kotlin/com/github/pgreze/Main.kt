package com.github.pgreze

fun main(args: Array<String>) {
    actions(args) {
        val buildDebug = action("build-debug", help = "Build APK and upload it to App Distribution") {
            println("Hello world")
        }

        action("upload-to-play-store", help = "Upload APK to the Play Store") {
            buildDebug()
            println("Upload to play store")
        }

        action("list-projects") {
            val cmd = gradle("projects")
            val projects = cmd.out
                .let { out -> Regex("':(.*)'").findAll(out).asIterable().map { it.groups.last()?.value } }
            println("Projects:" + projects.fold("") { s1, s2 -> "$s1 $s2" })
        }
    }
}
