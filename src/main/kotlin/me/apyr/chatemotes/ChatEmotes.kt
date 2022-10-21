package me.apyr.chatemotes

import me.apyr.chatemotes.commands.*
import me.apyr.chatemotes.emote.Emote
import me.apyr.chatemotes.emote.EmoteProvider
import me.apyr.chatemotes.emote.ResourcePackInfo
import me.apyr.chatemotes.emote.http.HttpEmoteProvider
import me.apyr.chatemotes.emote.local.LocalEmoteProvider
import me.apyr.chatemotes.exceptions.InvalidCommandArgumentException
import me.apyr.chatemotes.util.StringUtils.toHex
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class ChatEmotes : JavaPlugin() {
  init {
    instance = this
  }

  val settings: ChatEmotesSettings = ChatEmotesSettings(config)

  private val commands: Map<String, ChatEmotesCommand> = listOf(
    ListCommand(),
    AddCommand(),
    DelCommand(),
    RefreshCommand(),
    ReloadCommand(),
    SendPackCommand()
  ).associateBy { it.name }

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
    refreshEmotes()

    server.pluginManager.registerEvents(EventListener(), this)

    getCommand("emote")?.apply {
      val command = EmoteCommand()
      setExecutor(command)
      tabCompleter = command
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
  }

  override fun onDisable() {
    emoteProvider.onDisable()
  }

  fun reload() {
    reloadConfig()
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
  }

  fun getEmoteProvider(): EmoteProvider = emoteProvider

  fun setEmoteProvider(provider: EmoteProvider) {
    emoteProvider.onDisable()
    emoteProvider = provider
  }

  fun announceResourcePack() {
    val info: ResourcePackInfo = resourcePackInfo
      ?: return logger.warning("Failed to announce resource pack, resourcePackInfo is null")

    val sha1: String = info.sha1.toHex()
    if (sha1 == lastAnnouncedResourcePackHash) {
      logger.info("Resource pack didn't change, skipping announcement")
      return
    }

    logger.info("Announcing resource pack: ${resourcePackInfo?.url}")
    server.spigot().broadcast(
      *ComponentBuilder()
        .append("New emotes are available!\n").color(ChatColor.GREEN).bold(true)
        .append(
          TextComponent("Click here to apply resource pack.").apply {
            isUnderlined = true
            clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/emote sendpack")
          }
        )
        .create()
    )
    lastAnnouncedResourcePackHash = sha1
  }

  fun sendResourcePack(player: Player) {
    val info: ResourcePackInfo = resourcePackInfo ?: return
    player.setResourcePack(info.url, info.sha1, settings.resourcePackPrompt())
  }

  fun sendResourcePack() {
    val info: ResourcePackInfo = resourcePackInfo ?: return
    for (player: Player in Bukkit.getOnlinePlayers()) {
      player.setResourcePack(info.url, info.sha1, settings.resourcePackPrompt())
    }
  }

  companion object {
    private lateinit var instance: ChatEmotes
    internal fun getInstance(): ChatEmotes = instance
    internal fun getLogger(): Logger = instance.logger
  }

  private inner class EmoteCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
      val subcommandName: String? = args.firstOrNull()?.takeIf { it.isNotEmpty() }

      // send all commands
      if (subcommandName == null) {
        for (subcommand in commands.values.filter { !it.isShadow }) {
          val usage: String = subcommand.usage?.let { " $it" } ?: ""
          val cmd = TextComponent("/$label ${subcommand.name}${usage}").apply {
            color = ChatColor.GRAY
            isBold = true
            clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/$label ${subcommand.name}")
          }
          ComponentBuilder(cmd)
            .append(" - ${subcommand.description}", ComponentBuilder.FormatRetention.NONE)
            .color(ChatColor.GRAY)
            .create()
            .also { sender.spigot().sendMessage(*it) }
        }
        return true
      }

      val subcommand = commands[subcommandName] ?: return true
      try {
        subcommand.onCommand(sender, args.drop(1))
      } catch (_: InvalidCommandArgumentException) {
        sender.spigot().sendMessage(
          TextComponent("Usage: /$label $subcommandName ${subcommand.usage}").apply { color = ChatColor.GRAY }
        )
      }
      return true
    }

    override fun onTabComplete(
      sender: CommandSender,
      command: Command,
      label: String,
      args: Array<out String>
    ): List<String> {
      if (args.size <= 1) {
        return commands.values.filter { it.hasPermission(sender) }.map { it.name }
      }

      val firstArg = args.firstOrNull() ?: return emptyList()
      val subCommand = commands[firstArg] ?: return emptyList()
      return subCommand.onTabComplete(sender, args.drop(1))
    }
  }
}
