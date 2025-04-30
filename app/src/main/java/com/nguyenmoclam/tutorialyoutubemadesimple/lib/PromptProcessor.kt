package com.nguyenmoclam.tutorialyoutubemadesimple.lib

import com.nguyenmoclam.tutorialyoutubemadesimple.domain.model.content.Topic

fun extractTopicsAndQuestionsPrompt(
    transcript: String,
    title: String,
    language: String = "English"
): String {
    return """
            You are an expert content analyzer. Given a YouTube video transcript, identify at most 5 most interesting topics discussed and generate at most 3 most thought-provoking questions for each topic.
            These questions don't need to be directly asked in the video. It's good to have clarification questions.

            VIDEO TITLE: $title

            TRANSCRIPT:
            $transcript

            LANGUAGE:
            $language

            IMPORTANT INSTRUCTIONS:
            1. You MUST format your response as a valid JSON object
            2. Each topic MUST have a title and questions array
            3. Each topic MUST have at most 3 questions
            4. Return at most 5 topics
            5. Questions should be clear and engaging
            6. DO NOT include any markdown code blocks or additional text
            7. Ensure all strings are properly escaped
            8. All topics and questions MUST be in the specified language: $language
            9. The response MUST be a valid JSON object following exactly this structure.

            Expected JSON format:
            ```json
            {
                "topics": [
                    {
                        "title": "Clear and Concise Topic Title",
                        "questions": [
                            "First thought-provoking question about this topic?",
                            "Second interesting question about this topic?",
                            "Third clarifying question about this topic?"
                        ]
                    }
                ]
            }
            ```
            
        """.trimIndent()
}

fun processContentPrompt(
    topics: List<Topic>,
    transcript: String,
    language: String = "English"
): String {
    return """
            You are a content simplifier for children. Given multiple topics and questions from a YouTube video,
            rephrase each topic title and its questions to be clearer, and provide simple ELI5 (Explain Like I'm 5) answers.

            TOPICS AND QUESTIONS:
            ${
        topics.joinToString("\n\n") { topic ->
            """TOPIC: ${topic.title}
                QUESTIONS:
                ${topic.questions.joinToString("\n") { "- ${it.original}" }}""".trimIndent()
        }
    }

            TRANSCRIPT EXCERPT:
            $transcript

            LANGUAGE:
            $language

            For topic titles and questions:
            1. Keep them catchy and interesting, but short
            2. All content MUST be in the specified language: $language

            For your answers:
            1. Format them using HTML with <b> and <i> tags for highlighting.
            2. Prefer lists with <ol> and <li> tags. Ideally, <li> followed by <b> for the key points.
            3. Quote important keywords but explain them in easy-to-understand language (e.g., "<b>Quantum computing</b> is like having a super-fast magical calculator")
            4. Keep answers interesting but short

            IMPORTANT INSTRUCTIONS:
            1. You MUST format your response as a valid JSON object
            2. Each topic MUST have original_title, rephrased_title, and questions array
            3. Each question MUST have original, rephrased, and answer fields
            4. DO NOT include any markdown code blocks or additional text
            5. Ensure all strings are properly escaped
            6. The response MUST be a valid JSON object following exactly this structure

            Expected JSON format:
            ```json
            {
                "topics": [
                    {
                        "original_title": "Original Topic Title",
                        "rephrased_title": "Interesting topic title in 10 words",
                        "questions": [
                            {
                                "original": "Original question from input",
                                "rephrased": "Clearer, child-friendly version of the question",
                                "answer": "Simple, engaging answer with HTML formatting using <b>, <i>, <ol>, and <li> tags"
                            }
                        ]
                    }
                ]
            }
            ```
        """.trimIndent()

}

fun parseBatchProcessedContentPrompt(
    transcript: String,
    language: String
): String {
    return """
            You are an expert content analyzer. Given a YouTube video transcript, identify the key points or important facts discussed in the video. These key points will be used to generate questions later.

            TRANSCRIPT:
            $transcript

            LANGUAGE:
            $language

            INSTRUCTIONS:
            1. Identify at most 10 key points or important facts.
            2. Each key point should be concise and clear.
            3. Format the response as a valid JSON object with the following structure:
               {
                 "key_points": [
                   "Key point 1",
                   "Key point 2"
                 ]
               }
            Ensure the response is a valid JSON object following the specified structure.
        """.trimIndent()
}

fun generateQuestionsFromKeyPointsPrompt(
    keyPoints: List<String>,
    language: String,
    questionType: String,
    numberOfQuestions: Int
): String {
    val keyPointsText = keyPoints.joinToString("\n")
    return """
            You are an expert in creating educational questions. Given a list of key points from a YouTube video transcript, generate questions based on these key points. The questions should be in the specified language, of the specified type, and limited to the specified number.

            KEY POINTS:
            $keyPointsText

            LANGUAGE:
            $language

            QUESTION TYPE:
            $questionType

            NUMBER OF QUESTIONS:
            $numberOfQuestions

            INSTRUCTIONS:
            1. Generate exactly $numberOfQuestions questions based on the provided key points.
            2. All questions and answers must be in $language.
            3. For multiple-choice questions:
                - Provide 4 options labeled as A, B, C, D.
                - Indicate the correct answer(s) using the labels (e.g., "A" or ["A", "C"]).
                - For single-answer questions, there should be exactly one correct answer.
                - For multiple-answer questions, there can be more than one correct answer.
            4. For True/False questions:
               - Provide a statement and indicate whether it is true or false.
            5. Ensure the questions are directly related to the key points.
            6. Format the response as a valid JSON object with the following structure:

            For multiple-choice questions:
            {
              "questions": [
                {
                  "question": "Question text",
                  "options": {
                    "A": "Option 1",
                    "B": "Option 2",
                    "C": "Option 3",
                    "D": "Option 4"
                  },
                  "correct_answers": ["A"] // or ["A", "C"] for multiple answers
                }
              ]
            }

            For True/False questions:
            {
              "questions": [
                {
                  "statement": "Statement text",
                  "is_true": true/false
                }
              ]
            }

            Ensure the response is a valid JSON object following the specified structure.
        """.trimIndent()

}

fun extractKeyPointsForMindMapPrompt(
    transcript: String,
    title: String,
    language: String = "English"
): String {
    return """
        You are an expert content analyzer specializing in educational content. Given a YouTube video transcript, identify the main key points or core concepts discussed in the video. These key points will be used to create a mind map for educational purposes.

        VIDEO TITLE: $title

        TRANSCRIPT:
        $transcript

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. You MUST format your response as a valid JSON object.
        2. Include an array field "key_points" containing the key points.
        3. Analyze the content depth and complexity to determine the appropriate number of key points:
           - For short or simple content: Extract 3-5 key points
           - For medium-length content: Extract 5-7 key points
           - For long or complex content: Extract 7-10 key points
        4. Each key point should be a clear, concise sentence (10-15 words maximum).
        5. Ensure key points are factually accurate based on the transcript content.
        6. Key points should be distinct from each other and cover different aspects of the content.
        7. Arrange key points in a logical order that follows the content's natural progression.
        8. DO NOT include any markdown code blocks or additional text.
        9. Ensure all strings are properly escaped.
        10. All key points MUST be in the specified language: $language
        11. The response MUST be a valid JSON object following exactly this structure.
        
        Expected JSON format:
        ```json
        {
            "key_points": [
                "First key point or main idea...",
                "Second key point...",
                "Third key point...",
                "And so on based on content complexity..."
            ]
        }
        ```
        
    """.trimIndent()
}

fun generateMermaidMindMapCodePrompt(
    keyPoints: List<String>,
    title: String,
    language: String = "English"
): String {
    // Adjust number of sub-points based on number of key points
    val subPointsPerKeyPoint = when {
        keyPoints.size <= 5 -> "2-3"
        keyPoints.size <= 7 -> "2"
        else -> "1-2"
    }
    return """
        You are an expert at generating educational mind maps. Based on the given video title and key points, produce a well-structured Mermaid mind map diagram that clearly visualizes the relationships between concepts.

        VIDEO TITLE: $title

        KEY POINTS:
        ${keyPoints.joinToString("\n") { "- $it" }}

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. Use the video title as the central root node of the mind map.
        2. Each key point should be a top-level node branching from the title.
        3. For each key point, add $subPointsPerKeyPoint relevant sub-points that elaborate on that key point (derived from the key point itself).
        4. Use appropriate icons where relevant (e.g., 💡 for ideas, 🔑 for key concepts, 📊 for data points).
        5. Keep the mind map simple and standard - DO NOT use any styling, class definitions, or node IDs.
        6. Ensure the mind map is well-balanced with similar depth across branches.
        7. Output the diagram in Mermaid syntax inside a markdown code block labeled 'mermaid'.
        8. DO NOT include any explanation or text outside the Mermaid code block.
        9. All text in the mind map MUST be in the specified language: $language

        Expected Mermaid mindmap format:
        ```mermaid
        mindmap
            root(($title))
              1[First key point]
                1.1[Sub-point 1.1]
                1.2[Sub-point 1.2]
              2[Second key point]
                2.1[Sub-point 2.1]
                2.2[Sub-point 2.2]
              ... and so on for all key points
        ```

        The response MUST contain only the Mermaid mind map code in the format above without any styling, class definitions, or node IDs.
    """.trimIndent()
}

fun fixMindMapCodePrompt(
    originalCode: String,
    errorMessage: String,
    language: String = "English"
): String {
    return """
        You are an expert at fixing Mermaid syntax errors in mind maps. I have a Mermaid mind map diagram with syntax errors that needs to be fixed.

        ORIGINAL MERMAID CODE WITH ERRORS:
        ```mermaid
        ${originalCode.trim()}
        ```

        ERROR MESSAGE:
        $errorMessage

        LANGUAGE:
        $language

        IMPORTANT INSTRUCTIONS:
        1. Carefully analyze the error message and identify the syntax issues in the code.
        2. Fix ONLY the syntax errors while preserving the content and structure of the mind map.
        3. Ensure the fixed code follows proper Mermaid mindmap syntax (including correct indentation and one node per line).
        4. If a node contains special characters (like &, (), emoji, or internal quotes), enclose the entire node text in double quotes.
        5. If the node text contains parentheses (), which causes a syntax error, replace them with single quotes ' ' (e.g., (example) → 'example').
        6. Keep all text in the specified language: $language.
        7. Do not add or remove nodes, nor rename any existing text. Only fix syntax issues.
        8. Return ONLY the fixed Mermaid code without any markdown formatting, explanations, or comments.

        The response should contain only the corrected Mermaid mindmap code.
        """.trimIndent()
}