package me.apyr.chatemotes.exceptions

open class ChatEmotesException(
  val displayMessage: String? = null, // is sent to player
  message: String? = null,
  cause: Throwable? = null,
) : Exception(message, cause)
