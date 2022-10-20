package me.apyr.chatemotes

import org.bukkit.permissions.Permissible

internal enum class ChatEmotesPermission(val value: String) {
  MANAGE("chatemotes.manage");
}

internal fun Permissible.hasPermission(perm: ChatEmotesPermission) = hasPermission(perm.value)
