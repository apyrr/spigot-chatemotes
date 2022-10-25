package me.apyr.chatemotes.exceptions

class InvalidEmoteNameException : ChatEmotesException(
  displayMessage = "Some special characters are not allowed in emote names"
)
