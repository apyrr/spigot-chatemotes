package me.apyr.chatemotes

import me.apyr.chatemotes.emote.Emote
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class EmoteCommand : CommandExecutor {
  private val plugin: ChatEmotes = ChatEmotes.getInstance()

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    fun checkAdmin(): Boolean {
      if (!sender.hasPermission(ChatEmotesPermission.MANAGE)) {
        sender.spigot().sendMessage(
          TextComponent("Not enough permissions").apply { color = ChatColor.RED }
        )
        return false
      }
      return true
    }

    fun list() {
      val emotes: Collection<Emote> = plugin.emotes.values

      sender.spigot().sendMessage(
        TextComponent("All emotes (${emotes.size}) - click to copy").apply { color = ChatColor.YELLOW }
      )
      sender.spigot().sendMessage(
        *emotes.fold(ComponentBuilder()) { builder, emote ->
          val text = TextComponent(emote.char)
          text.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, emote.char)
          text.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(emote.name ))
          builder
            .append(text, ComponentBuilder.FormatRetention.NONE)
            .append("  ", ComponentBuilder.FormatRetention.NONE)
        }.create()
      )
    }

    fun refresh() {
      if (!checkAdmin()) {
        return
      }
      plugin.refreshEmotes()
      sender.spigot().sendMessage(
        TextComponent("Successfully refreshed all emotes and resource pack").apply { color = ChatColor.GREEN }
      )
    }

    fun reload() {
      if (!checkAdmin()) {
        return
      }
      plugin.reload()
      sender.spigot().sendMessage(
        TextComponent("Successfully reloaded config and emote provider").apply { color = ChatColor.GREEN }
      )
    }

    fun addEmote() {
      if (!checkAdmin()) {
        return
      }
      fun sendUsage() = sender.spigot()
        .sendMessage(TextComponent("Usage: /emote add <name> <url>").apply { color = ChatColor.RED })
      val name = args.getOrNull(1) ?: return sendUsage()
      val url = args.getOrNull(2) ?: return sendUsage()
      plugin.getEmoteProvider().addEmote(name, url)
      sender.spigot().sendMessage(
        TextComponent("Emote successfully added. Use '/emote refresh' to announce the resource pack")
          .apply { color = ChatColor.GREEN }
      )
    }

    fun delEmote() {
      if (!checkAdmin()) {
        return
      }
      fun sendUsage() = sender.spigot()
        .sendMessage(TextComponent("Usage: /emote del <name>").apply { color = ChatColor.RED })
      val name = args.getOrNull(1) ?: return sendUsage()
      plugin.getEmoteProvider().deleteEmote(name)
      sender.spigot().sendMessage(
        TextComponent("Emote successfully deleted. Use '/emote refresh' to announce the resource pack")
          .apply { color = ChatColor.GREEN }
      )
    }

    when (args.firstOrNull()) {
      "list" -> list()
      "refresh" -> refresh()
      "reload" -> reload()
      "add" -> addEmote()
      "del" -> delEmote()
      null -> list()
    }

    return true
  }
}
