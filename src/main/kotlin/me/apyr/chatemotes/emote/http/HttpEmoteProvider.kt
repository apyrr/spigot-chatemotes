package me.apyr.chatemotes.emote.http

import com.google.gson.GsonBuilder
import me.apyr.chatemotes.ChatEmotes
import me.apyr.chatemotes.ChatEmotesSettings
import me.apyr.chatemotes.emote.Emote
import me.apyr.chatemotes.emote.EmoteProvider
import me.apyr.chatemotes.emote.ResourcePackInfo
import me.apyr.chatemotes.util.GsonUtils.fromJson
import me.apyr.chatemotes.util.StringUtils.decodeHex
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.time.Duration

class HttpEmoteProvider : EmoteProvider {
  private val settings: ChatEmotesSettings = ChatEmotes.getInstance().settings
  private val httpClient: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(2))
    .build()
  private val gson = GsonBuilder().create()

  override fun getEmotes(): Map<String, Emote> {
    val request: HttpRequest = request(settings.httpUrlsEmotesList()).GET().build()
    return httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .checkErrors()
      .let { gson.fromJson(it.body()) }
  }

  override fun addEmote(name: String, url: String) {
    val body = gson.toJson(mapOf("name" to name, "url" to url))
    val request: HttpRequest = request(settings.httpUrlsEmotesAdd()).PUT(BodyPublishers.ofString(body)).build()
    httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .checkErrors()
  }

  override fun deleteEmote(name: String): Boolean {
    val request: HttpRequest = request(settings.httpUrlsEmotesDelete().replace("{name}", name)).DELETE().build()
    httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .checkErrors()

    return true
  }

  override fun renameEmote(old: String, new: String): Boolean {
    val body = gson.toJson(mapOf("name" to new))
    val request: HttpRequest = request(settings.httpUrlsEmotesRename().replace("{name}", old))
      .POST(BodyPublishers.ofString(body))
      .build()
    httpClient
      .send(request, HttpResponse.BodyHandlers.ofString())
      .checkErrors()
    return true
  }

  override fun getResourcePackInfo(): ResourcePackInfo {
    val hash: String = request(settings.httpUrlsPackHash()).GET().build().let { req ->
      httpClient
        .send(req, HttpResponse.BodyHandlers.ofString())
        .checkErrors()
        .let {
          data class Response(val hash: String)
          gson.fromJson<Response>(it.body()).hash
        }
    }
    return ResourcePackInfo(
      url = settings.httpUrlsPackDownload() + "?h=$hash",
      sha1 = hash.decodeHex()
    )
  }

  private fun request(url: String): HttpRequest.Builder = HttpRequest.newBuilder()
    .uri(URI(url))
    .timeout(Duration.ofSeconds(5))
    .let { req -> settings.httpHeaders().entries.fold(req) { acc, (key, value) -> acc.setHeader(key, value) } }

  private fun <T> HttpResponse<T>.checkErrors(): HttpResponse<T> {
    require(this.statusCode() == 200) { "Received response code ${this.statusCode()}" }
    return this
  }
}
