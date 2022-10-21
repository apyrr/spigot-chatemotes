package me.apyr.chatemotes

import me.apyr.chatemotes.emote.Emote
import me.apyr.chatemotes.emote.EmoteProvider
import me.apyr.chatemotes.emote.ResourcePackInfo
import me.apyr.chatemotes.emote.http.HttpEmoteProvider
import me.apyr.chatemotes.emote.local.LocalEmoteProvider
import me.apyr.chatemotes.util.StringUtils.toHex
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class ChatEmotes : JavaPlugin() {
  init {
    instance = this
  }

  val settings: ChatEmotesSettings = ChatEmotesSettings(config)

  private lateinit var emoteProvider: EmoteProvider

  // cached emotes
  var emotes: Map<String, Emote> = emptyMap()
    private set

  // cached resource pack
  var resourcePackInfo: ResourcePackInfo? = null
    private set

  private var lastAnnouncedResourcePackHash: String? = null

  override fun onEnable() {
    config.options().copyDefaults(true)
    saveConfig()

    initEmoteProvider()

    server.pluginManager.registerEvents(EventListener(), this)

    // TODO: rework commands
    getCommand("emote")?.apply {
      setExecutor(EmoteCommand())
      setTabCompleter { sender, _, _, args ->
        return@setTabCompleter if (args.size <= 1) {
          val base = listOf("list")
          if (sender.hasPermission(ChatEmotesPermission.MANAGE)) {
            base + listOf("add", "del", "refresh", "reload")
          } else {
            base
          }
        } else {
          emptyList()
        }
      }
    }
  }

  private fun initEmoteProvider() {
    // disable previous emote provider
    if (this::emoteProvider.isInitialized) {
      emoteProvider.onDisable()
    }

    emoteProvider = when (settings.emoteProvider()) {
      "local" -> LocalEmoteProvider()
      "http" -> HttpEmoteProvider()
      else -> throw IllegalArgumentException("Unknown emote provider: $emoteProvider")
    }

    emoteProvider.onEnable()
    refreshEmotes()
  }

  override fun onDisable() {
    emoteProvider.onDisable()
  }

  fun reload() {
    initEmoteProvider()
  }

  fun refreshEmotes() {
    emoteProvider.onEmoteRefresh()
    emotes = emoteProvider.getEmotes()
    resourcePackInfo = emoteProvider.getResourcePackInfo()

    if (emotes.isNotEmpty()) {
      logger.info("Loaded ${emotes.size} emotes from ${emoteProvider::class.simpleName}:")
      logger.info(emotes.values.joinToString(", ") { it.name })
    }

    announceResourcePack()
  }

  fun getEmoteProvider(): EmoteProvider = emoteProvider

  fun setEmoteProvider(provider: EmoteProvider) {
    emoteProvider.onDisable()
    emoteProvider = provider
  }

  fun announceResourcePack() {
    val info: ResourcePackInfo = resourcePackInfo ?: return
    val sha1: String = info.sha1.toHex()
    if (sha1 == lastAnnouncedResourcePackHash) {
      logger.info("Resource pack didn't change, skipping announcement")
      return
    }
    logger.info("Announcing resource pack: ${resourcePackInfo?.url}")
    for (player: Player in Bukkit.getOnlinePlayers()) {
      player.setResourcePack(info.url, info.sha1, settings.resourcePackPrompt())
    }
    lastAnnouncedResourcePackHash = sha1
  }

  fun announceResourcePack(player: Player) {
    val info: ResourcePackInfo = resourcePackInfo ?: return
    player.setResourcePack(info.url, info.sha1, settings.resourcePackPrompt())
  }

  companion object {
    private lateinit var instance: ChatEmotes
    internal fun getInstance(): ChatEmotes = instance
    internal fun getLogger(): Logger = instance.logger
  }
}
