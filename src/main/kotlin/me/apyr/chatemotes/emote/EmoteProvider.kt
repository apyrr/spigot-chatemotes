package me.apyr.chatemotes.emote

interface EmoteProvider {
  fun getEmotes(): Map<String, Emote>
  fun addEmote(name: String, url: String)
  fun deleteEmote(name: String)
  fun getResourcePackInfo(): ResourcePackInfo?

  fun onEmoteRefresh() {}
  fun onEnable() {}
  fun onDisable() {}
}
