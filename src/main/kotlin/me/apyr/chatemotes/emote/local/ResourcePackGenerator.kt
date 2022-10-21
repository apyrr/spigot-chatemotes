package me.apyr.chatemotes.emote.local

import com.google.gson.Gson
import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.emote.Emote
import me.apyr.chatemotes.util.StringUtils.toHex
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object ResourcePackGenerator {
  private val gson = Gson()

  // TODO: handle emotes with duplicate names
  fun generate(emotes: List<LocalEmote>): ByteArray {
    val sortedEmotes = emotes.sortedBy { it.char }
    val out = ByteArrayOutputStream(512 * 1024)
    val zip = ZipOutputStream(out)

    fun putEntry(path: String, data: ByteArray) {
      zip.putNextEntry(ZipEntry(path).apply { time = 0 })
      zip.write(data, 0, data.size)
      zip.closeEntry()
    }

    putEntry("pack.mcmeta", """{"pack":{"description":"ChatEmotes","pack_format":9}}""".toByteArray())
    putEntry("assets/minecraft/font/default.json", generateFontJson(sortedEmotes).toByteArray())

    for (emote: LocalEmote in sortedEmotes) {
      putEntry("assets/minecraft/textures/font/${emoteFileName(emote)}", emote.image)
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
        file = "minecraft:font/${emoteFileName(e)}",
        height = ChatEmotes.getInstance().settings.resourcePackHeight(),
        ascent = ChatEmotes.getInstance().settings.resourcePackAscent(),
        chars = listOf(e.char)
      )
    }

    return gson.toJson(mapOf("providers" to providers))
  }

  private fun emoteFileName(emote: Emote): String = emote.name.toByteArray().toHex() + ".png"
}
