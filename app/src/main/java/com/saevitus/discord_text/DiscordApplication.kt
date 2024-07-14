package com.saevitus.discord_text

import net.dv8tion.jda.api.JDA

object DiscordApplication {
  lateinit var jda: JDA
  val isJDAAvailable
    get() = this::jda.isInitialized

  lateinit var consoleAdapter: ConsoleAdapter
}
