package com.github.pgreze

fun requireEnv(name: String): String =
    System.getenv(name) ?: throw NullPointerException("Missing environment variable $name")

fun requireNonBlankEnv(name: String): String =
    requireEnv(name).takeIf(String::isNotBlank) ?: throw NullPointerException("Invalid environment variable $name")
