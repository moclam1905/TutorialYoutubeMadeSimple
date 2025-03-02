package com.nguyenmoclam.tutorialyoutubemadesimple.lib

/**
 * Represents a section in the generated HTML document.
 * Each section contains a title and a list of bullet points with formatted text.
 * This class is used to structure the content before generating the final HTML output.
 *
 * @property title The heading text for this section. This will be rendered as an h2 element
 *                 in the final HTML with appropriate styling.
 * @property bullets A list of pairs where each pair represents a bullet point:
 *                  - First: The bold text (usually a question or key point) that will be
 *                          wrapped in <strong> tags
 *                  - Second: The normal text (usually an answer or explanation) that will be
 *                           rendered as regular text with support for HTML formatting
 *
 * Example:
 * ```kotlin
 * val section = Section(
 *     title = "Understanding Kotlin Coroutines",
 *     bullets = listOf(
 *         Pair("What are coroutines?", "Coroutines are lightweight threads for async programming")
 *     )
 * )
 * ```
 */
data class Section(
    val title: String,
    val bullets: List<Pair<String, String>>
)

/**
 * Utility class for generating HTML documents from structured content.
 * This class provides functionality to create well-formatted, responsive HTML documents
 * that present YouTube video summaries in an easy-to-read format.
 *
 * Key features:
 * - Uses Tailwind CSS for responsive design and modern styling
 * - Implements Patrick Hand font for a friendly, handwritten appearance
 * - Supports structured content organization with sections and bullet points
 * - Includes proper meta tags and viewport settings for mobile compatibility
 * - Maintains consistent styling across different browsers
 */
class HtmlGenerator {
    companion object {
        /**
         * Generates a complete, formatted HTML document from the provided content.
         * This function combines the title, thumbnail image, and structured sections
         * into a cohesive HTML document with consistent styling and layout.
         *
         * The generated HTML includes:
         * - Responsive layout with Tailwind CSS for proper display on all devices
         * - Custom styling with Patrick Hand font for improved readability
         * - Title and thumbnail image section for video identification
         * - Multiple content sections with formatted bullet points
         * - Proper HTML5 structure with meta tags and viewport settings
         *
         * @param title The main title of the document, displayed prominently at the top
         * @param imageUrl URL of the thumbnail image to display below the title
         * @param sections List of Section objects containing the structured content to be rendered
         * @return A complete HTML document as a string, ready to be saved or displayed
         *
         * Example usage:
         * ```kotlin
         * val sections = listOf(
         *     Section(
         *         "Key Points",
         *         listOf(
         *             Pair("What is this about?", "This is the explanation...")
         *         )
         *     )
         * )
         * val html = HtmlGenerator.generate(
         *     "Video Title",
         *     "https://example.com/thumbnail.jpg",
         *     sections
         * )
         * ```
         */
        fun generate(title: String, imageUrl: String, sections: List<Section>): String {
            // Initialize the base HTML template with proper meta tags and styling
            val htmlTemplate = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <!-- Essential meta tags for proper rendering and character encoding -->
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Youtube Made Simple</title>
                  
                  <!-- Tailwind CSS for responsive design and utility classes -->
                  <link
                    rel="stylesheet"
                    href="https://unpkg.com/tailwindcss@2.2.19/dist/tailwind.min.css"
                  />
                  
                  <!-- Patrick Hand font for a friendly, handwritten appearance -->
                  <link rel="preconnect" href="https://fonts.gstatic.com" />
                  <link
                    href="https://fonts.googleapis.com/css2?family=Patrick+Hand&display=swap"
                    rel="stylesheet"
                  />
                  
                  <!-- Custom CSS for consistent styling across browsers -->
                  <style>
                    /* Base styles for body and text */
                    body {
                      background-color: #f7fafc; /* Light gray background */
                      font-family: 'Patrick Hand', sans-serif;
                    }
                    
                    /* Heading styles for clear hierarchy */
                    h1, h2 {
                      font-weight: 700;
                      margin-bottom: 0.5rem;
                    }
                    
                    /* List styling for better readability */
                    ul {
                      list-style-type: disc;
                      margin-left: 1.5rem;
                      margin-bottom: 1.5rem;
                    }
                    li {
                      margin-bottom: 1rem;
                    }
                    
                    /* Ordered list styling for answers */
                    ol {
                      list-style-type: decimal;
                      margin-left: 2rem;
                      margin-top: 0.5rem;
                    }
                    ol li {
                      margin-bottom: 0.2rem;
                    }
                    
                    /* Special styling for bullet content */
                    .bullet-content ol {
                      margin-top: 0.3rem;
                      margin-bottom: 0.3rem;
                    }
                  </style>
                </head>
                
                <!-- Centered content with responsive padding -->
                <body class="min-h-screen flex items-center justify-center p-4">
                  <!-- Main content container with shadow and rounded corners -->
                  <div class="max-w-2xl w-full bg-white rounded-2xl shadow-lg p-6">
                    <!-- Attribution header with link to project -->
                    <div class="mb-6 text-right text-gray-500 text-sm">
                      Generated by 
                      <a href="https://github.com/moclam1905" 
                         class="underline hover:text-gray-700">
                        Tutorial Youtube Made Simple
                      </a>
                    </div>
                    
                    <!-- Video title and thumbnail section -->
                    <h1 class="text-4xl text-gray-800 mb-4">${title}</h1>
                    <img
                      src="$imageUrl"
                      alt="Placeholder image"
                      class="rounded-xl mb-6"
                    />
            """.trimIndent()

            // Process each section into HTML with proper formatting
            val sectionsHtml = sections.joinToString("") { section ->
                // Create section header and unordered list container
                """
                    <h2 class="text-2xl text-gray-800 mb-4">${section.title}</h2>
                    <ul class="text-gray-600">
                        ${
                    // Process each bullet point with bold text and content
                    section.bullets.joinToString("") { (boldText, normalText) ->
                        """
                            <li>
                                <strong>${boldText}</strong><br />
                                <div class="bullet-content">${normalText}</div>
                            </li>
                            """.trimIndent()
                    }
                }
                    </ul>
                """.trimIndent()
            }

            // Close the HTML document structure
            val closingHtml = """
                  </div>
                </body>
                </html>
            """.trimIndent()

            return htmlTemplate + sectionsHtml + closingHtml
        }
    }
}