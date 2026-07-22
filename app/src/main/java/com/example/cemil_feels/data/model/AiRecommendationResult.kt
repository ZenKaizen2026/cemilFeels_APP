package com.example.cemil_feels.data.model

/**
 * Model untuk memetakan respon JSON dari Gemini AI.
 * Gemini dipaksa mengembalikan JSON persis dalam format ini
 * dengan menggunakan responseMimeType = "application/json".
 *
 * Contoh respon:
 * {
 *   "selected_snack_ids": [1, 2],
 *   "empathy_message": "Kamu pasti lagi capek banget, itu wajar kok...",
 *   "reasoning": "Camilan pedas bisa membantu mengalihkan rasa frustasi secara alami!"
 * }
 */
data class AiRecommendationResult(
    val selected_snack_ids: List<Int>,
    val empathy_message: String,
    val reasoning: String
)
