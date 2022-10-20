package me.apyr.chatemotes.util

import com.google.common.base.Charsets
import me.apyr.chatemotes.ChatEmotes
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level

internal class CustomConfig(private val name: String) {
  private val file: File = File(ChatEmotes.getInstance().dataFolder, name)
  private var config: FileConfiguration? = null

  fun getFileConfiguration(): FileConfiguration {
    return config ?: reload()
  }

  fun reload(): YamlConfiguration {
    val config = YamlConfiguration.loadConfiguration(file)
    this.config = config
    val defConfigStream = ChatEmotes.getInstance().getResource(name) ?: return config
    config.setDefaults(YamlConfiguration.loadConfiguration(InputStreamReader(defConfigStream, Charsets.UTF_8)))
    return config
  }

  fun save() {
    try {
      config?.save(file)
    } catch (ex: IOException) {
      ChatEmotes.getInstance().logger.log(Level.SEVERE, "Could not save config to $file", ex)
    }
  }

  fun saveDefault() {
    if (!file.exists()) {
      ChatEmotes.getInstance().saveResource(name, false)
    }
  }
}
