package com.saddar.dogbreeds.data

data class BreedResult(
    val breedName: String,
    val confidence: Float,
    val traits: List<String>
)
