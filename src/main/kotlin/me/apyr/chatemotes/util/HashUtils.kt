package me.apyr.chatemotes.util

import java.security.MessageDigest

internal object HashUtils {
  private val sha1: MessageDigest = MessageDigest.getInstance("SHA-1")

  fun ByteArray.sha1(): ByteArray {
    return sha1.digest(this)
  }
}
