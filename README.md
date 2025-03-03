# TutorialYoutubeMadeSimple 📱

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A powerful Android application that transforms YouTube videos into interactive learning experiences by extracting transcripts, generating summaries, and creating structured tutorials. This project is inspired by [Tutorial-Youtube-Made-Simple](https://github.com/The-Pocket/Tutorial-Youtube-Made-Simple).

## 🌟 Features

- **Transcript Extraction**: Automatically fetches and processes YouTube video transcripts
- **Smart Summarization**: Uses AI to generate concise, topic-based summaries
- **Interactive Learning**: Converts video content into Q&A format
- **Multi-language Support**: Currently supports English transcripts only
- **Export Functionality**: Share summaries in HTML format
- **Error Handling**: Robust error management for various scenarios

## 🎥 Demo

Watch our demo video to see TutorialYoutubeMadeSimple in action:

[Demo Video](demo/demo_video.webm)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Coroutines
- **Networking**: OkHttp, Retrofit
- **JSON Processing**: org.json, kotlinx.serialization
- **AI Integration**: OpenRouter API with Gemini model

## 🙏 Acknowledgments

- YouTube Data API
- OpenRouter API
- Gemini AI Model
- Apache Commons Text Library

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Kotlin 1.9.0 or later
- YouTube API Key
- OpenRouter API Key

### Installation

1. Clone the repository:
```bash
git clone https://github.com/moclam1905/TutorialYoutubeMadeSimple.git
```

2. Open the project in Android Studio

3. Add your API keys in the project:
```kotlin
// MainActivity.kt
companion object {
    const val YOUTUBE_API_KEY = "your_youtube_api_key"
    const val OPENROUTER_API_KEY = "your_openrouter_api_key"
}
```

4. Build and run the project

## 📱 Usage

### Basic Usage

1. Input a YouTube video URL or ID:
```kotlin
viewModel.startSummarization(
    videoUrlOrId = "https://www.youtube.com/watch?v=example",
    youtubeApiKey = YOUTUBE_API_KEY
)
```

2. Get video transcript:
```kotlin
val transcripts = YouTubeTranscriptLight.create().getTranscript(videoId)
val transcriptText = transcripts.joinToString(" ") { it.text }
```

3. Export summary:
```kotlin
viewModel.exportSummaryToHtml(context)
```

### Error Handling

```kotlin
try {
    val transcripts = YouTubeTranscriptLight.create().getTranscript(videoId)
    // Process transcripts
} catch (e: YouTubeTranscriptLight.TranscriptError) {
    when (e) {
        is YouTubeTranscriptLight.TranscriptError.VideoNotFound -> 
            // Handle video not found
        is YouTubeTranscriptLight.TranscriptError.TranscriptsDisabled -> 
            // Handle disabled transcripts
        is YouTubeTranscriptLight.TranscriptError.NoTranscriptAvailable -> 
            // Handle no transcript available
        is YouTubeTranscriptLight.TranscriptError.NetworkError -> 
            // Handle network errors
        is YouTubeTranscriptLight.TranscriptError.LanguageNotFound -> 
            // Handle language not found
    }
}
```

## 🏗️ Architecture

The project follows MVVM architecture with the following components:

### Core Components

- **YouTubeTranscriptLight**: Handles transcript fetching and parsing
- **LLMProcessor**: Manages AI-powered content processing
- **SummaryViewModel**: Coordinates the summarization workflow
- **ApiService**: Manages API communications

### Data Flow

1. User inputs YouTube URL
2. ViewModel initiates processing
3. Transcript is fetched and parsed
4. Content is processed by LLM
5. Summary is generated and displayed

## 🔍 Core Components Deep Dive

### YouTubeTranscriptLight

A lightweight, efficient transcript extractor designed for YouTube videos, inspired by [youtube-transcript-api](https://github.com/jdepoix/youtube-transcript-api). This is an unofficial implementation that provides the following key features:

#### Implementation Details
- **Network Efficiency**: Uses OkHttp for optimized HTTP requests
- **Regex-based Parsing**: Employs pattern matching to extract transcript data
- **Error Classification**: Implements sealed classes for granular error handling

#### Workflow
1. **Video Page Fetching**: Retrieves raw HTML content
2. **Captions Extraction**:
    - Parses JSON data from video page
    - Validates video ID and captions availability
    - Handles reCAPTCHA detection
3. **Transcript Processing**:
    - Extracts timing information
    - Cleans and formats text content
    - Handles HTML entity decoding

### LLMProcessor

AI-powered content analyzer using OpenRouter's Gemini model. Features:

#### Implementation Details
- **Two-Pass Processing**:
    1. Topic extraction and question generation
    2. Content simplification and answer generation
- **Batch Processing**: Efficient handling of multiple topics
- **JSON-based Communication**: Structured data exchange with LLM

#### Prompt Engineering
- **Topic Extraction**:
    - Limits to 5 most relevant topics
    - Generates up to 3 thought-provoking questions per topic
- **Content Simplification**:
    - ELI5 (Explain Like I'm 5) approach
    - HTML formatting for enhanced readability
    - Keyword highlighting and structured lists

### HtmlGenerator

Responsive HTML document generator with modern styling:

#### Implementation Details
- **Tailwind CSS Integration**: Responsive design out of the box
- **Custom Font Integration**: Patrick Hand font for improved readability
- **Section-based Structure**: Organized content presentation

#### Features
- **Responsive Layout**: Adapts to different screen sizes
- **Semantic HTML**: Proper heading hierarchy and list structures
- **Custom Styling**:
    - Consistent typography
    - Proper spacing and margins
    - Interactive elements styling


## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


