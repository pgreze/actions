package com.github.pgreze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlin.math.max

fun actions(args: Array<String>, block: ActionContext.() -> Unit) =
    ActionContext().also(block).main(args)

typealias ActionListener = ActionContext.(Action<*>) -> Unit

class ActionContext : NoOpCliktCommand(
    name = "actions",
    invokeWithoutSubcommand = false,
    allowMultipleSubcommands = true
) {
    val verbose by option("-v").flag("--verbose", default = false)
    internal val _actions = mutableListOf<Action<*>>()
    val actions: List<Action<*>> = _actions

    private var firstAction: Action<*>? = null
    private var beforeAll: ActionListener = { it.firstActionBegin() }
    private var afterAll: ActionListener = { it.firstActionEnd() }
    private var beforeEach: ActionListener = { if (verbose) echo(">> Start ${it.commandName}") }
    private var afterEach: ActionListener = { if (verbose) echo(">> End ${it.commandName}") }

    fun <T> action(
        name: String,
        help: String = "",
        block: () -> T
    ): Action<T> = Action(this, name, help, block)

    fun beforeAll(block: ActionListener) {
        val previous = beforeAll
        beforeAll = {
            previous(this, it)
            block(this, it)
        }
    }

    fun afterAll(block: ActionListener) {
        val previous = afterAll
        afterAll = {
            block(this, it)
            previous(this, it)
        }
    }

    fun beforeEach(block: ActionListener) {
        val previous = beforeEach
        beforeEach = {
            previous(this, it)
            block(this, it)
        }
    }

    fun afterEach(block: ActionListener) {
        val previous = afterEach
        afterEach = {
            block(this, it)
            previous(this, it)
        }
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

// TODO: provide echo outside a command
private fun <T> Action<T>.firstActionBegin() {
    println(announce("Start $commandName", commandHelp.takeIf(String::isNotEmpty)))
    println("")
}

private fun <T> Action<T>.firstActionEnd() {
    println("")
    println(announce("End $commandName"))
    println("")
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
