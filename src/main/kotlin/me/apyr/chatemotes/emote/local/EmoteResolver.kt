package me.apyr.chatemotes.emote.local

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object EmoteResolver {
  private val http: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(3))
    .build()

  private val urlHandlers: Map<Regex, (List<String>) -> String> = mapOf(
    Regex("""^(https://cdn.7tv.app/emote/\w+/)""") to { it[1] + "2x.png" },
    Regex("""^https://7tv.app/emotes/(\w+)""") to { "https://cdn.7tv.app/emote/${it[1]}/2x.png" },

    Regex("""^(https://cdn.betterttv.net/emote/\w+)""") to { it[1] + "2x" },
    Regex("""^https://betterttv.com/emotes/(\w+)""") to { "https://cdn.betterttv.net/emote/${it[1]}/2x" },

    Regex("""^(https://cdn.frankerfacez.com/emoticon/\w+/)""") to { it[1] + "2" },
    Regex("""^https://www.frankerfacez.com/emoticon/(\d+)""") to { "https://cdn.frankerfacez.com/emoticon/${it[1]}/2" },
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

  fun resolve(url: String): ByteArray {
    val finalUrl: String = urlHandlers.firstNotNullOfOrNull { (pattern, transform) ->
      pattern.find(url)?.let { transform(it.groupValues) }
    } ?: url

    val request: HttpRequest = HttpRequest.newBuilder()
      .uri(URI(finalUrl))
      .timeout(Duration.ofSeconds(5))
      .GET()
      .build()

    val response: HttpResponse<ByteArray> = http.send(request, HttpResponse.BodyHandlers.ofByteArray())
    check(response.statusCode() == 200) { "Invalid response code: ${response.statusCode()}" }
    check(response.body().size <= 256 * 1024) { "Emote is too large (${response.body().size / 1024} KB)" }
    check(response.body().take(8).toByteArray().contentEquals(pngMagic)) { "Emote is not a PNG image" }

    return response.body()
  }
}
