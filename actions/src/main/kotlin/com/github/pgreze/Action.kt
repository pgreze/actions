package com.github.pgreze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlin.math.max

fun actions(args: Array<String>, block: ActionContext.() -> Unit) =
    ActionContext().also(block).main(args)

typealias ActionLifecycle = ActionContext.(Action<*>) -> Unit

class ActionContext : NoOpCliktCommand(
    name = "actions",
    invokeWithoutSubcommand = false,
    allowMultipleSubcommands = true
) {
    val quiet by option("-q", help = "Disable all internal logging using echo")
        .flag("--quiet", default = false)
    val verbose by option("-v", help = "Display debug information like shell commands, etc")
        .flag("--verbose", default = false)

    internal val _actions = mutableListOf<Action<*>>()
    val actions: List<Action<*>> = _actions

    private var firstAction: Action<*>? = null
    private var beforeAll: ActionLifecycle = lifecyclePrinter({ echo(it) }) {
        announce("Start $commandName", commandHelp.takeIf(String::isNotEmpty))
    }
    private var afterAll: ActionLifecycle = lifecyclePrinter({ echo(it) }, before = "") {
        announce("End $commandName")
    }
    private var beforeEach: ActionLifecycle = { echo(">> Start ${it.commandName}", verbose = true) }
    private var afterEach: ActionLifecycle = { echo(">> End ${it.commandName}", verbose = true) }

    fun <T> action(
        name: String,
        help: String = "",
        block: () -> T
    ): Action<T> = Action(this, name, help, block)

    fun echo(msg: Any?, err: Boolean = false, verbose: Boolean = false) {
        if (!quiet && (!verbose || this.verbose)) {
            super.echo(msg, trailingNewline = true, err = err, lineSeparator = currentContext.console.lineSeparator)
        }
    }

    fun beforeAll(block: ActionLifecycle) {
        beforeAll = beforeAll.finalizeBy(block)
    }

    fun afterAll(block: ActionLifecycle) {
        afterAll = block.finalizeBy(afterAll)
    }

    fun beforeEach(block: ActionLifecycle) {
        beforeEach = beforeEach.finalizeBy(block)
    }

    fun afterEach(block: ActionLifecycle) {
        afterEach = block.finalizeBy(afterEach)
    }

    internal fun onBeforeAction(action: Action<*>): Boolean =
        if (firstAction == null) {
            firstAction = action
            beforeAll.invoke(this, action)
            true
        } else {
            false
        }.also { beforeEach(action) }

    internal fun onAfterAction(action: Action<*>) {
        afterEach(action)
        if (firstAction == action) {
            afterAll.invoke(this, action)
        }
    }
}

class Action<T>(
    private val actionContext: ActionContext,
    name: String,
    help: String = "",
    private val block: () -> T
) : CliktCommand(name = name, help = help) {
    init {
        actionContext._actions.add(this)
        actionContext.subcommands(this)
    }

    override fun run() {
        invoke()
    }

    operator fun invoke(): T {
        actionContext.onBeforeAction(this)
        return block().also {
            actionContext.onAfterAction(this)
        }
    }
}

private fun ActionLifecycle.finalizeBy(block: ActionLifecycle): ActionLifecycle = {
    this@finalizeBy(this, it)
    block(this, it)
}

private fun lifecyclePrinter(
    echo: (String) -> Unit,
    before: String? = null,
    after: String = "",
    accouncer: Action<*>.() -> String
): ActionLifecycle = {
    before?.let { s -> echo(s) }
    echo(it.accouncer())
    echo(after)
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
