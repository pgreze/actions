package com.github.pgreze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlin.math.max

fun actions(args: Array<String>, block: ActionContext.() -> Unit) =
    ActionContext().also(block).main(args)

class ActionContext : NoOpCliktCommand(
    name = "actions",
    invokeWithoutSubcommand = false,
    allowMultipleSubcommands = true
) {
    val verbose by option("-v").flag("--verbose", default = false)

    fun action(
        name: String,
        help: String = "",
        block: () -> Unit
    ): Action =
        Action(this, name, help, block)
}

class Action(
    context: ActionContext,
    name: String,
    help: String = "",
    private val block: () -> Unit
) : CliktCommand(name = name, help = help) {
    init {
        context.subcommands(this)
    }

    override fun run() {
        echo(announce("Start $commandName", commandHelp.takeIf(String::isNotEmpty)))
        echo("")
        block()
        echo("")
        echo(announce("End $commandName"))
        echo("")
    }

    operator fun invoke() = run()
}

private fun announce(title: String, subtitle: String? = null): String {
    val longestMessageLength = max(title.length, subtitle?.length ?: 0)
    val lineLength = (longestMessageLength + 14) / 10 * 10
    val border = "=".repeat(lineLength)
    return "$border\n${formatMessage(title, lineLength)}\n${subtitle?.let {
        formatMessage(subtitle, lineLength) + "\n"
    } ?: ""}$border"
}

private fun formatMessage(msg: String, fullLength: Int): String {
    val firstSpaceLength = (fullLength - msg.length - 2) / 2
    val lastSpace = " ".repeat(fullLength - msg.length - firstSpaceLength - 2) // -2 for = borders
    return "=${" ".repeat(firstSpaceLength)}$msg$lastSpace="
}
