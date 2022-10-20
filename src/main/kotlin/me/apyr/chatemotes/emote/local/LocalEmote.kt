package me.apyr.chatemotes.emote.local

import me.apyr.chatemotes.emote.Emote

data class LocalEmote(
  override val name: String,
  override val char: String,
  val image: ByteArray
) : Emote
