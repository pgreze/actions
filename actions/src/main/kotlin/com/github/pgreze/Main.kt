package com.github.pgreze

fun main(args: Array<String>) {
    actions(args) {
        val buildDebug by action {
            println("Hello world")
        }

        val uploadToPlayStore by action {
            //buildDebug()
            println("Upload to play store")
        }
    }
}
