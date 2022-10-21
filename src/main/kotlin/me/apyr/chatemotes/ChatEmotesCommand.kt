package me.apyr.chatemotes

import me.apyr.chatemotes.exceptions.InvalidCommandArgumentException
import org.bukkit.command.CommandSender

interface ChatEmotesCommand {
  val name: String
  val description: String
  val usage: String?
    get() = null

  /**
   * Don't display in command list
   */
  val isShadow: Boolean
    get() = false

  fun onCommand(sender: CommandSender, args: List<String>)
  fun onTabComplete(sender: CommandSender, args: List<String>): List<String> = emptyList()
  fun hasPermission(sender: CommandSender): Boolean

  fun checkArgument(arg: String?): String {
    if (arg == null) {
      throw InvalidCommandArgumentException()
    }

    return arg
  }
}

