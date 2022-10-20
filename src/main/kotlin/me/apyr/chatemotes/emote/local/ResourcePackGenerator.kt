package me.apyr.chatemotes.emote.local

import com.google.gson.Gson
import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.emote.Emote
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object ResourcePackGenerator {
  private val gson = Gson()

  fun generate(emotes: List<LocalEmote>): ByteArray {
    val sortedEmotes = emotes.sortedBy { it.char }
    val out = ByteArrayOutputStream(512 * 1024)
    val zip = ZipOutputStream(out)

    fun putEntry(path: String, data: ByteArray) {
      val e = ZipEntry(path).apply {
        time = 0
      }
      zip.putNextEntry(e)
      zip.write(data, 0, data.size)
      zip.closeEntry()
    }

    putEntry("pack.mcmeta", """{"pack":{"description":"ChatEmotes","pack_format":9}}""".toByteArray())
    putEntry("assets/minecraft/font/default.json", generateFontJson(sortedEmotes).toByteArray())

    for (emote: LocalEmote in sortedEmotes) {
      val fileName = emote.name.lowercase() + ".png"
      putEntry("assets/minecraft/textures/font/$fileName", emote.image)
    }

    zip.close()

    return out.toByteArray()
  }

  private fun generateFontJson(emotes: List<Emote>): String {
    data class Provider(
      val type: String = "bitmap",
      val file: String,
      val height: Int,
      val ascent: Int,
      val chars: List<String>
    )

    val providers = emotes.map { e ->
      Provider(
        file = "minecraft:font/${e.name.lowercase()}.png",
        height = ChatEmotes.getInstance().settings.resourcePackHeight(),
        ascent = ChatEmotes.getInstance().settings.resourcePackAscent(),
        chars = listOf(e.char)
      )
    }

    return gson.toJson(mapOf("providers" to providers))
  }
}