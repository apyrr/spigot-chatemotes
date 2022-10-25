package me.apyr.chatemotes.commands

import me.apyr.chatemotes.*
import me.apyr.chatemotes.ChatEmotesPermission
import me.apyr.chatemotes.ChatEmotesSettings.Companion.STRICT_EMOTE_NAME_PATTERN
import me.apyr.chatemotes.exceptions.InvalidEmoteNameException
import me.apyr.chatemotes.hasPermission
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

class RenameCommand : ChatEmotesCommand {
  override val name: String = "rename"
  override val description: String = "rename an emote"
  override val usage: String = "<old> <new>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val old: String = checkArgument(args.getOrNull(0))
    val new: String = checkArgument(args.getOrNull(1))

    if (ChatEmotes.getInstance().settings.strictEmoteNames() && !STRICT_EMOTE_NAME_PATTERN.matches(new)) {
      throw InvalidEmoteNameException()
    }

    val success: Boolean = ChatEmotes.getInstance().getEmoteProvider().renameEmote(old, new)

    if (success) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Emote was successfully renamed to $new. ")
          .color(ChatColor.GREEN)
          .append(
            TextComponent("Click here").apply {
              isUnderlined = true
              clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote refresh")
            }
          )
          .append(" to announce the resource pack", ComponentBuilder.FormatRetention.NONE)
          .color(ChatColor.GREEN)
          .create()
      )
    } else {
      sender.spigot().sendMessage(
        TextComponent("Emote does not exists or new name is already taken").apply { color = ChatColor.RED }
      )
    }
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return when {
      args.size == 1 -> ChatEmotes.getInstance().emotes.values.let { emotes ->
        val term: String? = args.firstOrNull()?.takeIf { it.isNotEmpty() }
        if (term != null) {
          emotes.filter { e -> e.name.contains(term, ignoreCase = true) }
        } else {
          emotes
        }
      }.map { it.name }
      args.size == 2 && args[1].isEmpty() -> listOf("<new>")
      else -> emptyList()
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
