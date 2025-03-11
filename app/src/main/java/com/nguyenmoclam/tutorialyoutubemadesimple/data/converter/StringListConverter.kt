package com.nguyenmoclam.tutorialyoutubemadesimple.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converter for Room database to convert between List<String> and String.
 * Uses Gson for JSON serialization/deserialization.
 */
class StringListConverter {
    private val gson = Gson()
    private val type = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromString(value: String): List<String> {
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }
}