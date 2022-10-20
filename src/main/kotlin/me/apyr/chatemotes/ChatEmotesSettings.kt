package me.apyr.chatemotes

import org.bukkit.configuration.file.FileConfiguration

class ChatEmotesSettings(private val config: FileConfiguration) {
  fun emoteProvider(): String = config.getString("emote-provider.use")!!

  // local
  fun resourcePackPrompt(): String = config.getString("emote-provider.local.pack.height")!!
  fun resourcePackHeight(): Int = config.getInt("emote-provider.local.pack.height")
  fun resourcePackAscent(): Int = config.getInt("emote-provider.local.pack.ascent")
  fun hostnameOverride(): String? = config.getString("emote-provider.local.hostname-override")
  fun httpPort(): Int = config.getInt("emote-provider.local.http.port")

  // remote
  fun httpHeaders(): Map<String, String> {
    return config.getConfigurationSection("emote-provider.http.headers")
      ?.getValues(false)
      ?.mapNotNull { (key, value) -> (value as? String)?.let { str -> key to str } }
      ?.toMap()
      ?: emptyMap()
  }
  fun httpUrlsEmotesList(): String = config.getString("emote-provider.http.urls.emotes.list")!!
  fun httpUrlsEmotesAdd(): String = config.getString("emote-provider.http.urls.emotes.add")!!
  fun httpUrlsEmotesDelete(): String = config.getString("emote-provider.http.urls.emotes.delete")!!
  fun httpUrlsPackDownload(): String = config.getString("emote-provider.http.urls.pack.download")!!
  fun httpUrlsPackHash(): String = config.getString("emote-provider.http.urls.pack.hash")!!
}
