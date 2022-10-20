package me.apyr.chatemotes.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GsonUtils {
  inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, object : TypeToken<T>() {}.type)
  }
}
