package me.apyr.chatemotes.emote.local

import me.apyr.chatemotes.ChatEmotes
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.MethodNotSupportedException
import org.apache.http.config.SocketConfig
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.bootstrap.HttpServer
import org.apache.http.impl.bootstrap.ServerBootstrap
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestHandler
import java.util.*
import java.util.concurrent.TimeUnit

internal class ResourcePackServer {
  private var resourcePack: ByteArray = byteArrayOf()
  private var server: HttpServer? = null

  var checkUuid: UUID? = null

  fun updateResourcePack(new: ByteArray) {
    this.resourcePack = new
  }

  fun start(port: Int) {
    require(server == null) { "Server is already running" }

    ChatEmotes.getLogger().info("Starting resource pack HTTP server with port $port")

    server = ServerBootstrap.bootstrap()
      .setListenerPort(port)
      .setServerInfo("ResourcePackServer/1.1")
      .setSocketConfig(
        SocketConfig.custom()
          .setSoTimeout(15_000)
          .setTcpNoDelay(true)
          .build()
      )
      .registerHandler("/chat_emotes.zip", PackHandler())
      .registerHandler("/check", PingHandler())
      .create()
      .also { server ->
        server.start()
        object : Thread("chatemotes-await") {
          override fun run() {
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
          }
        }.start()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
          override fun run() {
            server.shutdown(2, TimeUnit.SECONDS)
          }
        })
      }
  }

  fun stop() {
    ChatEmotes.getLogger().info("Stopping resource pack HTTP server")
    server?.stop()
    server = null
  }

  private inner class PackHandler : HttpRequestHandler {
    override fun handle(request: HttpRequest, response: HttpResponse, context: HttpContext) {
      val method: String = request.requestLine.method.uppercase(Locale.ROOT)
      if (method != "GET" && method != "HEAD") {
        throw MethodNotSupportedException("$method method not supported")
      }

      response.setStatusCode(HttpStatus.SC_OK)
      val body = ByteArrayEntity(resourcePack, ContentType.APPLICATION_OCTET_STREAM)
      response.entity = body
    }
  }

  private inner class PingHandler : HttpRequestHandler {
    override fun handle(request: HttpRequest, response: HttpResponse, context: HttpContext) {
      response.setStatusCode(HttpStatus.SC_OK)
      response.entity = StringEntity(checkUuid.toString(), ContentType.TEXT_PLAIN)
    }
  }
}
