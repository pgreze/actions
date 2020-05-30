package com.github.pgreze

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun actions(args: Array<String>, block: ActionContext.() -> Unit) =
    ActionContext().also {
        it.block()
        MainAction().subcommands(it.actions).main(args)
    }

class ActionContext {

    internal var actions = listOf<UserAction>()

    /** Returns the delegate allowing to declare a new action. */
    fun action(block: () -> Unit) =
        UserActionDelegate(block)
}

class MainAction(
) : NoOpCliktCommand(
    name = "actions",
    invokeWithoutSubcommand = false,
    allowMultipleSubcommands = true
) {
    init {
        subcommands(UserAction("test") { println("test") })
    }
}

class UserAction(
    name: String,
    private val action: () -> Unit
) : CliktCommand(name = name) {

    override fun run() {
        action()
    }
}

class UserActionDelegate(
    private val block: () -> Unit
) : ReadOnlyProperty<Nothing?, UserAction> {

    lateinit var action: UserAction

    override fun getValue(thisRef: Nothing?, property: KProperty<*>): UserAction =
        if (this::action.isInitialized) action else UserAction(property.name, block)
}
