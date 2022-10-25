package me.apyr.chatemotes

import me.apyr.chatemotes.emote.Emote
import net.md_5.bungee.api.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta

class EventListener : Listener {

  private val plugin: ChatEmotes = ChatEmotes.getInstance()
  private val settings: ChatEmotesSettings = plugin.settings

  @EventHandler
  fun onJoin(e: PlayerJoinEvent) {
    ChatEmotes.getInstance().sendResourcePack(e.player)

    if (!settings.useEmotesInPlayerNames()) {
      return
    }

    val emotes: Map<String, Emote> = plugin.emotes.takeIf { it.isNotEmpty() } ?: return

    e.player.displayName.let { displayName ->
      val formatted: String = formatMessage(displayName, emotes)

      if (displayName != formatted) {
        e.player.setDisplayName(formatted)
      }
    }

    e.player.playerListName.let { listName ->
      val formatted: String = formatMessage(listName, emotes)

      if (listName != formatted) {
        e.player.setPlayerListName(formatted)
      }
    }
  }

  @EventHandler
  fun onChat(e: AsyncPlayerChatEvent) {
    if (!settings.useEmotesInChat()) {
      return
    }

    e.message = formatMessage(e.message, plugin.emotes.takeIf { it.isNotEmpty() } ?: return)
  }

  @EventHandler
  fun onSign(e: SignChangeEvent) {
    if (!settings.useEmotesInSigns()) {
      return
    }

    val emotes: Map<String, Emote> = plugin.emotes.takeIf { it.isNotEmpty() } ?: return
    e.lines.forEachIndexed { index, s ->
      val formatted: String = formatSignOrBookLine(s, emotes)
      if (s != formatted) {
        e.setLine(index, formatted)
      }
    }
  }

  @EventHandler
  fun onBook(e: PlayerEditBookEvent) {
    if (!settings.useEmotesInBooks()) {
      return
    }

    val emotes: Map<String, Emote> = plugin.emotes.takeIf { it.isNotEmpty() } ?: return

    val meta: BookMeta = e.newBookMeta
    meta.pages.forEachIndexed { index, s ->
      val formatted: String = s
        .split("\n")
        .joinToString("\n") { line -> formatSignOrBookLine(line, emotes) }
      if (s != formatted) {
        meta.setPage(index + 1, formatted)
      }
    }

    meta.title?.let { title ->
      meta.title = formatMessage(title, emotes)
    }

    e.newBookMeta = meta
  }

  @EventHandler
  fun onAnvil(e: PrepareAnvilEvent) {
    if (!settings.useEmotesInAnvils()) {
      return
    }

    val emotes: Map<String, Emote> = plugin.emotes.takeIf { it.isNotEmpty() } ?: return

    val item: ItemStack = e.result ?: return
    val meta: ItemMeta = item.itemMeta ?: return
    val formatted = formatMessage(meta.displayName, emotes)

    if (meta.displayName != formatted) {
      meta.setDisplayName(formatted)
      item.itemMeta = meta
    }
  }

  private fun formatMessage(input: String, emotes: Map<String, Emote>): String = input
    .split(" ")
    .joinToString(" ") { part -> emotes[part]?.char ?: part }

  private fun formatSignOrBookLine(input: String, emotes: Map<String, Emote>): String {
    var formatted: String = input

    for (emote: Emote in emotes.values) {
      // don't replace existing emotes
      val pattern = Regex("(?<!${ChatColor.WHITE})${emote.char}(?!${ChatColor.RESET})")
      if (formatted.contains(pattern)) {
        formatted = formatted.replace(pattern, "${ChatColor.WHITE}${emote.char}${ChatColor.RESET}")
        println(formatted)
      }
    }

    formatted = formatted.split(" ").joinToString(" ") { part ->
      emotes[part]?.char?.let { "${ChatColor.WHITE}$it${ChatColor.RESET}" } ?: part
    }

    return formatted
  }
}
