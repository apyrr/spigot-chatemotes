package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SendPackCommand : ChatEmotesCommand {
  override val name: String = "sendpack"
  override val description: String = "send resource pack to current player"
  override val isShadow: Boolean = true

  override fun onCommand(sender: CommandSender, args: List<String>) {
    (sender as? Player)?.also { player ->
      ChatEmotes.getInstance().sendResourcePack(player)
      Bukkit.getScheduler().runTaskLater(
        ChatEmotes.getInstance(), Runnable {
          if (player.isOnline) {
            player.performCommand("emote list")
          }
        },
        20 * 2
      )
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean = true
}
