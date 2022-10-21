package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.ChatEmotesPermission
import me.apyr.chatemotes.hasPermission
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

class DelCommand : ChatEmotesCommand {
  override val name: String = "del"
  override val description: String = "delete an emote"
  override val usage: String = "<name>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val name: String = checkArgument(args.getOrNull(0))

    if (!ChatEmotes.getInstance().emotes.containsKey(name)) {
      sender.spigot().sendMessage(TextComponent("Emote does not exists").apply { color = ChatColor.RED })
      return
    }

    ChatEmotes.getInstance().getEmoteProvider().deleteEmote(name)
    sender.spigot().sendMessage(
      TextComponent("Emote successfully deleted. Use '/emote refresh' to announce the resource pack")
        .apply { color = ChatColor.GREEN }
    )
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
