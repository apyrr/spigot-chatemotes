package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.ChatEmotesPermission
import me.apyr.chatemotes.hasPermission
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

class ReloadCommand : ChatEmotesCommand {
  override val name: String = "reloadconfig"
  override val description: String = "Reload config and emote provider"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    ChatEmotes.getInstance().reload()
    sender.spigot().sendMessage(
      TextComponent("Successfully reloaded config and emote provider").apply { color = ChatColor.GREEN }
    )
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
