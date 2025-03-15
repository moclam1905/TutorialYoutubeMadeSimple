package com.nguyenmoclam.tutorialyoutubemadesimple.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converter for Room database to convert between Map<String, String> and String.
 * Uses Gson for JSON serialization/deserialization.
 */
class StringMapConverter {
    private val gson = Gson()
    private val type = object : TypeToken<Map<String, String>>() {}.type

    @TypeConverter
    fun fromString(value: String): Map<String, String> {
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }
}