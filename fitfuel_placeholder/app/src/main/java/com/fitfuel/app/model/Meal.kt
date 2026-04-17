package com.fitfuel.app.model

data class Meal(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val tags: List<String>,
    val emoji: String,
    val description: String
)
