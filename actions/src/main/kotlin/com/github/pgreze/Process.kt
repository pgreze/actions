package com.github.pgreze

import java.io.BufferedReader
import java.io.InputStream
import java.io.PrintStream
import java.util.concurrent.TimeUnit

fun ActionContext.run(
    cmd: String,
    printPolicy: PrintPolicy = PrintPolicy.ALL,
    timeout: TimeOutConstraint? = null
): Process {
    echo(">> Run $cmd")
    // TODO: allow ~ or $HOME usage
    return Runtime.getRuntime().exec(cmd).also {
        if (printPolicy.stdout) it.inputStream.asyncReadLines(System.out)
        if (printPolicy.stderr) it.errorStream.asyncReadLines(System.err)
        if (timeout == null) {
            it.waitFor()
        } else {
            it.waitFor(timeout.timeout, timeout.unit)
        }
    }
}

data class TimeOutConstraint(val timeout: Long, val unit: TimeUnit)

enum class PrintPolicy(val stdout: Boolean, val stderr: Boolean) {
    NONE(stdout = false, stderr = false),
    STDOUT(stdout = true, stderr = false),
    STDERR(stdout = false, stderr = true),
    ALL(stdout = true, stderr = true)
}

val Process.out: String
    // Notice: inputStream is the input for us, in other words Process output...
    get() = inputStream.bufferedReader().use(BufferedReader::readText)

val Process.err: String
    get() = errorStream.bufferedReader().use(BufferedReader::readText)

// TODO: use coroutines
private fun InputStream.asyncReadLines(printer: PrintStream) = Thread(Runnable {
    reader().useLines { lines -> lines.forEach { line -> printer.println(line) } }
}).start()
