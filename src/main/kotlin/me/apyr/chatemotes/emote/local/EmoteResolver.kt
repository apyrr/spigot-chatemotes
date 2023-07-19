package me.apyr.chatemotes.emote.local

import me.apyr.chatemotes.exceptions.ChatEmotesException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import javax.imageio.ImageIO

object EmoteResolver {
  private const val maxEmoteSize = 128 * 1024

  private val http: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(3))
    .build()

  private val urlHandlers: Map<Regex, (List<String>) -> String> = mapOf(
    Regex("""^(https://cdn\.betterttv\.net/emote/\w+/)""") to { it[1] + "2x.png" },
    Regex("""^https://betterttv\.com/emotes/(\w+)""") to { "https://cdn.betterttv.net/emote/${it[1]}/2x.png" },

    Regex("""^(https://cdn\.frankerfacez\.com/emoticon/\w+/)""") to { it[1] + "2" },
    Regex("""^https://www\.frankerfacez\.com/emoticon/(\d+)""") to { "https://cdn.frankerfacez.com/emoticon/${it[1]}/2" },
  )

  private val pngMagic: ByteArray = byteArrayOf(
    0x89.toByte(),
    0x50.toByte(),
    0x4E.toByte(),
    0x47.toByte(),
    0x0D.toByte(),
    0x0A.toByte(),
    0x1A.toByte(),
    0x0A.toByte()
  )

  private val gifMagic: ByteArray = byteArrayOf(
    0x47.toByte(),
    0x49.toByte(),
    0x46.toByte(),
    0x38.toByte()
  )

  private fun rewriteUrl(url: String): String {
    return urlHandlers.firstNotNullOfOrNull { (pattern, transform) ->
      pattern.find(url)?.let { transform(it.groupValues) }
    }
      ?: url
  }

  private fun sendRequest(url: String) = HttpRequest.newBuilder()
    .uri(URI(url))
    .timeout(Duration.ofSeconds(5))
    .GET()
    .build()
    .let { http.send(it, HttpResponse.BodyHandlers.ofByteArray()) }

  private fun check(value: Boolean, lazyMessage: () -> String) {
    if (!value) {
      throw EmoteResolveException(lazyMessage())
    }
  }

  fun resolve(url: String): ByteArray {
    val sevenTvMatch = Regex("""^https://(?:cdn\.)?7tv\.app/emotes?/(\w+)""").find(url)

    val finalUrl: String = when {
      sevenTvMatch != null -> let {
        val id = sevenTvMatch.groupValues[1]
        val response = sendRequest("https://cdn.7tv.app/emote/$id/2x.png")
        if (response.statusCode() == 200) {
          return response.body()
        }

        val finalUrl = "https://cdn.7tv.app/emote/$id/2x.gif"
        check(response.statusCode() == 403) {
          "Invalid 7tv response code (${response.statusCode()}) for URL $finalUrl"
        }

        return@let finalUrl
      }

      else -> rewriteUrl(url)
    }

    val response = sendRequest(finalUrl)
    check(response.statusCode() == 200) { "Invalid response code (${response.statusCode()}), URL $finalUrl" }

    val body = response.body()
    val bytes = when {
      body.take(8).toByteArray().contentEquals(pngMagic) -> body
      body.take(4).toByteArray().contentEquals(gifMagic) -> fromGifToPng(body)
      else -> throw EmoteResolveException("Unknown image format, URL $finalUrl")
    }

    check(bytes.size <= maxEmoteSize) {
      "Emote is too large (${body.size / 1024} KB), ${maxEmoteSize / 1024}KB max"
    }

    return bytes
  }

  private fun sevenTvEmoteBytes(id: String): ByteArray {
    val response = sendRequest("https://cdn.7tv.app/emote/$id/2x.png")
    if (response.statusCode() == 200) {
      return response.body()
    }

    check(response.statusCode() == 403) {
      "Invalid 7tv response code (${response.statusCode()}) for URL https://cdn.7tv.app/emote/$id/2x.png"
    }

    val workaroundResponse = sendRequest("https://cdn.7tv.app/emote/$id/2x.gif")
    check(workaroundResponse.statusCode() == 200) {
      "Invalid 7tv response code (${response.statusCode()}) for URL https://cdn.7tv.app/emote/$id/2x.gif"
    }

    return response.body()
  }

  private fun fromGifToPng(input: ByteArray): ByteArray {
    return try {
      ImageIO
        .read(ByteArrayInputStream(input))
        .let {
          val out = ByteArrayOutputStream();
          ImageIO.write(it, "png", out)
          return@let out.toByteArray()
        }
    } catch (e: IOException) {
      e.printStackTrace()
      throw (EmoteResolveException("Failed to convert from GIF: ${e.message}"))
    }
  }

  private class EmoteResolveException(override val displayMessage: String) : ChatEmotesException()
}
