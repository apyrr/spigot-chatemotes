package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotesCommand
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent

class SetSignCommand : ChatEmotesCommand {
  override val name: String = "setsign"
  override val description: String = "modify a line on the sign"
  override val usage: String = "<1-4> <text>"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    if (sender !is Player) {
      return
    }

    val line: Int = (checkArgument(args.getOrNull(0)).toIntOrNull() ?: 1)
      .coerceAtLeast(1)
      .coerceAtMost(4)
    checkArgument(args.getOrNull(1))
    val text: String = args.drop(1).joinToString(" ")

    val sign: Block = sender.getTargetBlock(null, 5)
    val state: BlockState = sign.state

    if (state !is Sign) {
      return sender.spigot().sendMessage(
        TextComponent("Please, look at a sign.").apply { color = ChatColor.RED }
      )
    }

    val event = SignChangeEvent(sign, sender, state.lines.copyOf().apply { set(line - 1, text) })
    Bukkit.getPluginManager().callEvent(event)

    if (event.isCancelled) {
      return sender.spigot().sendMessage(
        TextComponent("Something went wrong. Do you have permissions to edit this sign?")
          .apply { color = ChatColor.RED }
      )
    }

    event.lines.forEachIndexed { index, s ->
      state.setLine(index, s)
    }
    state.update()
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> {
    return when {
      args.size == 1 && args[0].isEmpty() -> listOf("<1-4>")
      args.size == 2 && args[1].isEmpty() -> listOf("<text>")
      else -> emptyList()
    }
  }

  override fun hasPermission(sender: CommandSender): Boolean = true
}
