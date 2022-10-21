package me.apyr.chatemotes

import me.apyr.chatemotes.emote.Emote
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.meta.BookMeta

class EventListener : Listener {

  @EventHandler
  fun onJoin(e: PlayerJoinEvent) {
    ChatEmotes.getInstance().sendResourcePack(e.player)
  }

  @EventHandler
  fun onChat(e: AsyncPlayerChatEvent) {
    val emotes: Map<String, Emote> = ChatEmotes.getInstance().emotes

    if (emotes.isEmpty()) {
      return
    }

    e.message = e.message
      .split(" ")
      .joinToString(" ") { part -> emotes[part]?.char ?: part }
  }

  @EventHandler
  fun onSign(e: SignChangeEvent) {
    val emotes: Map<String, Emote> = ChatEmotes.getInstance().emotes

    if (emotes.isEmpty()) {
      return
    }

    e.lines.forEachIndexed { index, s ->
      val formatted: String = formatSignOrBookLine(s, emotes)
      if (s != formatted) {
        e.setLine(index, formatted)
      }
    }
  }

  @EventHandler
  fun onBook(e: PlayerEditBookEvent) {
    val emotes: Map<String, Emote> = ChatEmotes.getInstance().emotes

    if (emotes.isEmpty()) {
      return
    }

    val meta: BookMeta = e.newBookMeta
    meta.pages.forEachIndexed { index, s ->
      val formatted: String = formatSignOrBookLine(s, emotes)
      if (s != formatted) {
        meta.setPage(index + 1, formatted)
      }
    }

    if (meta.hasTitle()) {
      meta.title = meta.title
        ?.split(" ")
        ?.joinToString(" ") { part -> emotes[part]?.char ?: part }
    }

    e.newBookMeta = meta
  }

  private fun formatSignOrBookLine(input: String, emotes: Map<String, Emote>): String {
    var formatted: String = input

    for (emote: Emote in emotes.values) {
      if (formatted.contains(emote.char)) {
        formatted = formatted.replace(emote.char, "${ChatColor.WHITE}${emote.char}${ChatColor.RESET}")
      }
    }

    formatted = formatted.split(" ").joinToString(" ") { part ->
      emotes[part]?.char?.let { "${ChatColor.WHITE}$it${ChatColor.RESET}" } ?: part
    }

    return formatted
  }
}
