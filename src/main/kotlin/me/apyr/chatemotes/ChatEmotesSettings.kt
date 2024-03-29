package me.apyr.chatemotes

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class ChatEmotesSettings(private val plugin: JavaPlugin) {
  private val config: FileConfiguration
    get() = plugin.config

  fun cmdListPageSize(): Int = config.getInt("page-size")
  fun strictEmoteNames(): Boolean = config.getBoolean("strict-emote-names")

  fun useEmotesInChat(): Boolean = config.getBoolean("use-emotes-in.chat")
  fun useEmotesInSigns(): Boolean = config.getBoolean("use-emotes-in.signs")
  fun useEmotesInBooks(): Boolean = config.getBoolean("use-emotes-in.books")
  fun useEmotesInAnvils(): Boolean = config.getBoolean("use-emotes-in.anvils")
  fun useEmotesInPlayerNames(): Boolean = config.getBoolean("use-emotes-in.player-names")

  fun emoteProvider(): String = config.getString("emote-provider.use")!!

  // local
  fun resourcePackPrompt(): String = config.getString("emote-provider.local.pack.height")!!
  fun resourcePackHeight(): Int = config.getInt("emote-provider.local.pack.height")
  fun resourcePackAscent(): Int = config.getInt("emote-provider.local.pack.ascent")
  fun hostnameOverride(): String? = config.getString("emote-provider.local.hostname-override")?.ifEmpty { null }
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
  fun httpUrlsEmotesRename(): String = config.getString("emote-provider.http.urls.emotes.rename")!!
  fun httpUrlsPackDownload(): String = config.getString("emote-provider.http.urls.pack.download")!!
  fun httpUrlsPackHash(): String = config.getString("emote-provider.http.urls.pack.hash")!!

  companion object {
    val STRICT_EMOTE_NAME_PATTERN = Regex("""[\w:()<>]+""")
  }
}
