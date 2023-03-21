package me.apyr.chatemotes.emote.local

import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.emote.EmoteProvider
import me.apyr.chatemotes.emote.ResourcePackInfo
import me.apyr.chatemotes.util.CustomConfig
import me.apyr.chatemotes.util.HashUtils.sha1
import me.apyr.chatemotes.util.MinecraftUtils
import me.apyr.chatemotes.util.StringUtils.toHex
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.logging.Level

class LocalEmoteProvider : EmoteProvider {
  private val store: CustomConfig = CustomConfig("local_emotes.yml")

  private val packServer: ResourcePackServer = ResourcePackServer()
  private val loginListener: LoginListener = LoginListener()

  override fun getEmotes(): Map<String, LocalEmote> {
    store.reload()
    return store.getFileConfiguration()
      .getMapList("emotes")
      .mapIndexedNotNull { index, map ->
        try {
          val name: String = checkNotNull(map["name"] as? String) { "name is null" }
          val char: String = checkNotNull(map["char"] as? String) { "char is null" }
          val imageBase64: String = checkNotNull(map["image"] as? String) { "image is null" }
          val image: ByteArray = Base64.getDecoder().decode(imageBase64)

          LocalEmote(name = name, char = char, image = image)
        } catch (e: Exception) {
          ChatEmotes.getLogger().log(Level.WARNING, "Failed to convert emote #$index", e)
          null
        }
      }
      .associateBy { it.name }
  }

  override fun addEmote(name: String, url: String) {
    val emotes = getEmotes()
    val occupiedEmojis: Set<String> = emotes.values.map { it.char }.toSet()
    val nextEmoji: String = (MinecraftUtils.SUPPORTED_EMOJI - occupiedEmojis).randomOrNull() ?: return
    val image: ByteArray = EmoteResolver.resolve(url)

    emotes
      .toMutableMap()
      .apply { set(name, LocalEmote(name = name, char = nextEmoji, image = image)) }
      .also { saveToFile(it.values) }
  }

  override fun deleteEmote(name: String): Boolean {
    val emotes = getEmotes()

    if (!emotes.containsKey(name)) {
      return false
    }

    emotes.filterKeys { it != name }.also { saveToFile(it.values) }
    return true
  }

  override fun renameEmote(old: String, new: String): Boolean {
    val emotes = getEmotes().toMutableMap()
    if (emotes.containsKey(new)) {
      return false
    }

    val emote: LocalEmote = emotes[old] ?: return false
    emotes.remove(old)
    emotes[new] = emote.copy(name = new)
    saveToFile(emotes.values)

    return true
  }

  override fun getResourcePackInfo(): ResourcePackInfo? {
    val emotes = getEmotes()
    if (emotes.isEmpty()) {
      ChatEmotes.getLogger().info("Failed to generate resource pack: no emotes are present")
      return null
    }

    val hostname: String = publicServerHostname ?: return let {
      ChatEmotes.getLogger().info("Failed to generate resource pack: unknown public server hostname")
      null
    }
    val port: Int = ChatEmotes.getInstance().settings.httpPort()

    val pack: ByteArray = ResourcePackGenerator.generate(emotes.values.toList())
    val hash: ByteArray = pack.sha1()
    packServer.updateResourcePack(pack)

    return ResourcePackInfo("http://${hostname}:${port}/chat_emotes.zip?h=${hash.toHex()}", hash)
  }

  override fun onEnable() {
    packServer.start(ChatEmotes.getInstance().settings.httpPort())

    if (publicServerHostname == null) {
      Bukkit.getServer().pluginManager.registerEvents(loginListener, ChatEmotes.getInstance())
    }
  }

  override fun onDisable() {
    packServer.stop()
    PlayerLoginEvent.getHandlerList().unregister(loginListener)
  }

  private fun saveToFile(emotes: Collection<LocalEmote>) {
    val map: List<Map<String, String>> = emotes.map { emote ->
      mapOf(
        "name" to emote.name,
        "char" to emote.char,
        "image" to Base64.getEncoder().encodeToString(emote.image)
      )
    }
    store.getFileConfiguration().set("emotes", map)
    store.save()
  }

  companion object {
    // this hostname is used to serve the resource pack
    private var publicServerHostname: String? = ChatEmotes.getInstance().settings.hostnameOverride()

    init {
      if (publicServerHostname != null) {
        ChatEmotes.getLogger().info("Using public server hostname from config: $publicServerHostname")
      }
    }
  }

  private inner class LoginListener : Listener {
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val hostnamePattern: Regex = Regex("(.+):\\d+")
    private var attempts: Int = 0

    @EventHandler
    fun onLogin(e: PlayerLoginEvent) {
      if (publicServerHostname != null || attempts >= 5) {
        PlayerLoginEvent.getHandlerList().unregister(this)
        return
      }

      // TODO: handle localhost?
      val hostname: String = hostnamePattern.matchEntire(e.hostname)
        ?.groupValues
        ?.getOrNull(1)
        ?: return

      attempts++

      Bukkit.getScheduler().runTaskAsynchronously(ChatEmotes.getInstance(), Runnable {
        if (checkServerHostname(hostname)) {
          publicServerHostname = hostname
          ChatEmotes.getLogger().info("Found valid hostname for this server: $hostname")

          Bukkit.getScheduler().runTask(ChatEmotes.getInstance(), Runnable {
            ChatEmotes.getInstance().apply {
              refreshEmotes()
              sendResourcePack()
            }
            PlayerLoginEvent.getHandlerList().unregister(this)
          })
        }
      })
    }

    // Check if hostname points to this Minecraft server.
    private fun checkServerHostname(hostname: String): Boolean {
      val expectedUuid: UUID = UUID.randomUUID()
      packServer.checkUuid = expectedUuid

      val request = java.net.http.HttpRequest.newBuilder()
        .uri(URI("http://$hostname:${ChatEmotes.getInstance().settings.httpPort()}/check"))
        .timeout(Duration.ofSeconds(1))
        .GET()
        .build()
      val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

      return response.body() == expectedUuid.toString()
    }
  }
}

