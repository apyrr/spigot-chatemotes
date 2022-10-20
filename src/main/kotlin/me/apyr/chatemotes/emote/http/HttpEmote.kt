package me.apyr.chatemotes.emote.http

import me.apyr.chatemotes.emote.Emote

data class HttpEmote(override val name: String, override val char: String) : Emote
