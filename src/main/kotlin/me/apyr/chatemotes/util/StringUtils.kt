package me.apyr.chatemotes.util

internal object StringUtils {
  fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

  fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
      .map { it.toInt(16).toByte() }
      .toByteArray()
  }
}
