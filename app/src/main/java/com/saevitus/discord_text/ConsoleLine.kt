package com.saevitus.discord_text

data class ConsoleLine(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(), // Optional timestamp
    val level: String = "INFO" // Optionallog level
)
