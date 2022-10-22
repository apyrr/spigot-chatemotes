package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import org.bukkit.command.CommandSender

class SearchCommand : ChatEmotesCommand {
  override val name: String = "s"
  override val description: String = "search for an emote"
  override val usage: String = "<name>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return ChatEmotes.getInstance()
      .emotes.values
      .let { emotes ->
        val term: String? = args.firstOrNull()?.takeIf { it.isNotEmpty() }
        if (term != null) {
          emotes.filter { e -> e.name.contains(term, ignoreCase = true) }
        } else {
          emotes
        }
      }
      .map { "${it.name} ${it.char}" }
  }

  override fun hasPermission(sender: CommandSender): Boolean = true
}
