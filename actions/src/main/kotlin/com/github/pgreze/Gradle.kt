package com.github.pgreze

fun ActionContext.gradle(
    tasks: String,
    properties: Map<String, String> = mapOf(),
    wrapper: String = "./gradlew"
): Process {
    val extras = properties.entries
        .takeIf { it.isNotEmpty() }
        ?.map { "-P${it.key}=${it.value}" }
        ?.reduce { s1, s2 -> "$s1 $s2" }
        ?: ""
    return run("$wrapper $tasks $extras")
}
