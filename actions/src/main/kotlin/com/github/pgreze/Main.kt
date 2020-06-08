package com.github.pgreze

import java.io.File

fun main(args: Array<String>) = actions(args) {
    beforeAll { echo("Run ${it.commandName}") }
    afterAll { echo("Finished ${it.commandName}") }

    val buildDebug = action("build-debug", help = "Build APK and upload it to App Distribution") {
        println("Hello world")
        return@action File("my/debug.apk")
    }

    action("upload-to-play-store", help = "Upload APK to the Play Store") {
        val apk: File = buildDebug()
        println("Upload $apk to play store")
    }

    action("list-projects") {
        val cmd = run(gradle("projects"))
        val projects = cmd.out
            .let { out -> Regex("':(.*)'").findAll(out).asIterable().map { it.groups.last()?.value } }
        println("${actions.size} actions can be used on following projects:" + projects.fold("") { s1, s2 -> "$s1 $s2" })
    }

    action("shell") {
        run("ls ~/.my | cat")
        run("echo home=\$HOME")
    }
}
