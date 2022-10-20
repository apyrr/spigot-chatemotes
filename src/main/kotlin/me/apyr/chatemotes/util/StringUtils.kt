package me.apyr.chatemotes.util

internal object StringUtils {
  private val hexArray = ('0'..'9') + ('a'..'f')

  fun ByteArray.hex(): String {
    val hexChars = CharArray(this.size * 2)
    for (j in this.indices) {
      val v = this[j].toInt() and 0xFF
      hexChars[j * 2] = hexArray[v.ushr(4)]
      hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
  }

  fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
      .map { it.toInt(16).toByte() }
      .toByteArray()
  }
}
