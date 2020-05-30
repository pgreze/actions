@file:DependsOn("com.github.ajalt:clikt:2.7.1")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class MyCommand : CliktCommand() {
    val times by option("-t").int().default(1)

    override fun run() {
        repeat(times) { println("Hello world") }
    }
}

MyCommand().main(args)
