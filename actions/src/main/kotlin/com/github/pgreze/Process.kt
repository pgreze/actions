package com.github.pgreze

import java.io.BufferedReader
import java.util.concurrent.TimeUnit

fun ActionContext.run(
    cmd: String,
    verbose: Boolean = false,
    timeout: Pair<Long, TimeUnit>? = null
): Process {
    if (this.verbose || verbose) println(">> Run $cmd")
    return Runtime.getRuntime().exec(cmd).also {
        if (timeout != null) it.waitFor(timeout.first, timeout.second)
    }
}

val Process.out: String
    // Notice: inputStream is the input for us, in other words Process output...
    get() = inputStream.bufferedReader().use(BufferedReader::readText)

val Process.err: String
    get() = errorStream.bufferedReader().use(BufferedReader::readText)
