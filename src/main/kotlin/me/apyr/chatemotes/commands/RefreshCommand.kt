package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.ChatEmotesPermission
import me.apyr.chatemotes.hasPermission
import org.bukkit.command.CommandSender

class RefreshCommand : ChatEmotesCommand {
  override val name: String = "refresh"
  override val description: String = "refresh emote list"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    ChatEmotes.getInstance().apply {
      refreshEmotes()
      announceResourcePack()
    }
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return emptyList()
  }

  override fun hasPermission(sender: CommandSender): Boolean {
    return sender.hasPermission(ChatEmotesPermission.MANAGE)
  }
}
