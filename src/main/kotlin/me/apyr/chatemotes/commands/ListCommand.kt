package me.apyr.chatemotes.commands

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesCommand
import me.apyr.chatemotes.emote.Emote
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender
import kotlin.math.max

class ListCommand : ChatEmotesCommand {
  private val plugin: ChatEmotes = ChatEmotes.getInstance()

  override val name: String = "list"
  override val description: String = "list all emotes"
  override val usage: String = "[page]"

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val allEmotes = plugin.emotes.values

    if (allEmotes.isEmpty()) {
      sender.spigot().sendMessage(TextComponent("No emotes yet!").apply { color = ChatColor.GRAY })
      return
    }

    val currentPage: Int = max(0, (args.getOrNull(0)?.toIntOrNull() ?: 1) - 1)
    val totalPages: Int = allEmotes.size / 15
    val emotes: List<Emote> = allEmotes.asSequence().drop(currentPage * 15).take(15).toList()

    if (emotes.isEmpty()) {
      return
    }

    sender.spigot().sendMessage(
      *emotes.fold(ComponentBuilder("\n")) { builder, emote ->
        builder
          .append(TextComponent(emote.char))
          .append(
            TextComponent(" ${emote.name}").apply {
              color = ChatColor.GRAY
              clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, emote.char)
              hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to copy"))
            },
            ComponentBuilder.FormatRetention.NONE
          )
          .append(" ".repeat(3), ComponentBuilder.FormatRetention.NONE)
      }.create()
    )

    sender.spigot().sendMessage(
      *ComponentBuilder(" ".repeat(25))
        .apply {
          if (currentPage >= 1) {
            append(
              TextComponent("←  ").apply {
                color = ChatColor.GRAY
                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote list $currentPage")
              }
            ).bold(true)
          }

          append("[page ${currentPage + 1} / ${totalPages + 1}]", ComponentBuilder.FormatRetention.NONE)
            .color(ChatColor.GRAY)

          if (currentPage < totalPages) {
            append(
              TextComponent("  →").apply {
                color = ChatColor.GRAY
                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote list ${currentPage + 2}")
              },
              ComponentBuilder.FormatRetention.NONE
            ).bold(true)
          }
        }
        .create()
    )
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> = emptyList()
  override fun hasPermission(sender: CommandSender): Boolean = true
}
