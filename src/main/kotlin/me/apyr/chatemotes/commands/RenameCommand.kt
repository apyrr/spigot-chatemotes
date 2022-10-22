package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.ChatEmotesPermission
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
      args.size == 1 && args[0].isEmpty() -> ChatEmotes.getInstance().emotes.values.map { it.name }
      args.size == 2 && args[1].isEmpty() -> listOf("<new>")
      else -> emptyList()
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
