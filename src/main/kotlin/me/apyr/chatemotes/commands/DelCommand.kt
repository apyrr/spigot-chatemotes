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

class DelCommand : ChatEmotesCommand {
  override val name: String = "del"
  override val description: String = "delete an emote"
  override val usage: String = "<name>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val name: String = checkArgument(args.getOrNull(0))

    val success: Boolean = ChatEmotes.getInstance().getEmoteProvider().deleteEmote(name)

    if (success) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Emote successfully deleted. ")
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
      sender.spigot().sendMessage(TextComponent("Emote does not exists").apply { color = ChatColor.RED })
    }
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return when {
      args.size == 1 && args[0].isEmpty() -> listOf("<name>")
      else -> emptyList()
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
