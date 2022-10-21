package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesPermission
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.hasPermission
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

class AddCommand : ChatEmotesCommand {
  override val name: String = "add"
  override val description: String = "add new emote"
  override val usage: String = "<name> <url>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val name: String = checkArgument(args.getOrNull(0))
    val url: String = checkArgument(args.getOrNull(1))

    ChatEmotes.getInstance().getEmoteProvider().addEmote(name, url)
    sender.spigot().sendMessage(
      *ComponentBuilder("Emote successfully added. ")
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
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return when {
      args.size == 1 && args[0].isEmpty() -> listOf("<name>")
      args.size == 2 && args[1].isEmpty() -> listOf("<url>")
      else -> emptyList()
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
