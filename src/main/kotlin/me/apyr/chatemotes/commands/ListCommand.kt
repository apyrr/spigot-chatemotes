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

  private val pageSize: Int = 14

  override fun onCommand(sender: CommandSender, args: List<String>) {
    val allEmotes = plugin.emotes.values

    if (allEmotes.isEmpty()) {
      sender.spigot().sendMessage(TextComponent("No emotes yet!").apply { color = ChatColor.GRAY })
      return
    }

    val currentPage: Int = max(0, (args.getOrNull(0)?.toIntOrNull() ?: 1) - 1)
    val totalPages: Int = allEmotes.size / pageSize
    val emotes: List<Emote> = allEmotes.asSequence().drop(currentPage * pageSize).take(pageSize).toList()

    if (emotes.isEmpty()) {
      return
    }

    emotes
      .foldIndexed(ComponentBuilder()) { index, builder, emote ->
        if (index == 0) {
          builder.append("\n")
        }
        builder
          .append("\n${emote.char} ", ComponentBuilder.FormatRetention.NONE)
          .append("- ${emote.name}").color(ChatColor.GRAY)
          .event(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, emote.char))
          .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to copy ${emote.char}")))
      }
      .create()
      .also {
        sender.spigot().sendMessage(*it)
      }

    /*sender.spigot().sendMessage(
      *emotes.fold(ComponentBuilder("\n")) { builder, emote ->
        builder
          .append(
            TextComponent(emote.char).apply {
              clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, emote.char)
              hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to copy"))
            }
          )
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
    )*/

    sender.spigot().sendMessage(
      *ComponentBuilder()
        .apply {
          append("\n")
          if (currentPage >= 1) {
            append("←  ")
              .color(ChatColor.GRAY)
              .bold(true)
              .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote list $currentPage"))
              .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click too see previous page")))
          } else {
            append("    ")
          }

          append("[page ${currentPage + 1} / ${totalPages + 1}]", ComponentBuilder.FormatRetention.NONE)
            .color(ChatColor.GRAY)

          if (currentPage < totalPages) {
            append("  →")
              .color(ChatColor.GRAY)
              .bold(true)
              .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote list ${currentPage + 2}"))
              .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click too see next page")))
          }
        }
        .create()
    )
  }

  override fun onTabComplete(sender: CommandSender, args: List<String>): List<String> = emptyList()
  override fun hasPermission(sender: CommandSender): Boolean = true
}
