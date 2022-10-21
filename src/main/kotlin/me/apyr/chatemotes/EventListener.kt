package me.apyr.chatemotes

import me.apyr.chatemotes.emote.Emote
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent

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
}
