package com.nguyenmoclam.tutorialyoutubemadesimple.data.model.video

data class VideoResponse(val items: List<VideoItem>)

data class VideoItem(val snippet: Snippet)

data class Snippet(
    val title: String,
    val thumbnails: Thumbnails,
    val defaultAudioLanguage: String?
)

data class Thumbnails(
    val default: Thumbnail?,
    val medium: Thumbnail?,
    val high: Thumbnail?,
    val standard: Thumbnail?,
    val maxres: Thumbnail?
)

data class Thumbnail(val url: String)