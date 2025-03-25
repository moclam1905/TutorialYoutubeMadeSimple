package com.nguyenmoclam.tutorialyoutubemadesimple.domain.usecase.quiz

import com.nguyenmoclam.tutorialyoutubemadesimple.lib.LLMProcessor
import com.nguyenmoclam.tutorialyoutubemadesimple.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenerateMindMapUseCase @Inject constructor(
    private val llmProcessor: LLMProcessor,
    private val networkUtils: NetworkUtils
) {
    private var lastExtractedKeyPoints: List<String> = emptyList()

    data class MindMapResult(val mermaidCode: String, val error: String? = null)

    suspend operator fun invoke(transcriptContent: String, transcriptLanguage: String, videoTitle: String?): MindMapResult = withContext(Dispatchers.IO) {
        try {
            // Prompt 1: Extract key points
            val keyPoints = llmProcessor.extractKeyPointsForMindMap(transcriptContent, transcriptLanguage)
                .takeIf { it.isNotEmpty() } ?: return@withContext MindMapResult("", error = "No key points extracted")
            lastExtractedKeyPoints = keyPoints

            // Prompt 2: Generate Mermaid code from key points
            val code = llmProcessor.generateMermaidMindMapCode(keyPoints, videoTitle ?: "")
                .takeIf { it.isNotBlank() } ?: return@withContext MindMapResult("", error = "Mind map generation failed")
            
            // Use NetworkUtils to determine the appropriate complexity level for the mind map
            // based on network quality
            val networkQuality = networkUtils.getRecommendedImageQuality()
            
            // Optimize the mind map code based on network quality
            // For lower quality networks, we can simplify the mind map
            val optimizedCode = when (networkQuality) {
                "low" -> simplifyMermaidCode(code, "low")
                "medium" -> simplifyMermaidCode(code, "medium")
                else -> code // Use the original code for high quality networks
            }
            
            MindMapResult(mermaidCode = optimizedCode)
        } catch(e: Exception) {
            MindMapResult("", error = e.message ?: "Unknown error during mind map generation")
        }
    }
    
    /**
     * Simplifies a Mermaid mind map code based on network quality
     * 
     * @param originalCode The original Mermaid code
     * @param qualityLevel The network quality level ("low", "medium")
     * @return Simplified Mermaid code
     */
    private fun simplifyMermaidCode(originalCode: String, qualityLevel: String): String {
        return when (qualityLevel) {
            "low" -> {
                // For low quality, remove all styling and simplify the structure
                // Remove all style definitions
                val noStyles = originalCode.replace(Regex("\\s*style\\s+[^\\n]+"), "")
                    .replace(Regex("\\s*linkStyle\\s+[^\\n]+"), "")
                    .replace(Regex("\\s*::[^\\n]+"), "")
                
                // Simplify node shapes (convert complex shapes to simple ones)
                val simplifiedShapes = noStyles
                    .replace(Regex("\\(\\([^)]*\\)\\)"), "[")
                    .replace(Regex("\\)\\)"), "]")
                    .replace(Regex("\\(\\("), "[")
                
                // Remove icons to reduce complexity
                simplifiedShapes.replace(Regex("\\s*[\\p{Emoji}\\p{Emoji_Presentation}]"), "")
            }
            "medium" -> {
                // For medium quality:
                // 1. Keep basic structure but simplify styling
                // 2. Keep some node shapes but remove complex styling
                // 3. Limit color variations
                
                // Remove complex styling but keep basic style definitions
                val simplifiedStyles = originalCode
                    .replace(Regex("\\s*style\\s+[^\\n]+\\s+fill:[^,]+,[^\\n]+"), "")
                    .replace(Regex("stroke-width:[^,\\n]+"), "stroke-width:1px")
                
                // Keep only a subset of icons if present (limit to 3 most common)
                val commonIcons = listOf("💡", "🔑", "📊")
                var result = simplifiedStyles
                
                // Keep only common icons, remove others
                val iconPattern = Regex("[\\p{Emoji}\\p{Emoji_Presentation}]")
                val matches = iconPattern.findAll(result)
                for (match in matches) {
                    val icon = match.value
                    if (!commonIcons.contains(icon)) {
                        result = result.replace(icon, "")
                    }
                }
                
                result
            }
            else -> originalCode // High quality - keep original code
        }
    }

    fun getLastExtractedKeyPoints(): List<String> = lastExtractedKeyPoints
}
