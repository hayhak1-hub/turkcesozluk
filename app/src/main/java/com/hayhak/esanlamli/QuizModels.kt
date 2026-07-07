package com.hayhak.esanlamli

data class QuizQuestion(
    val word: String,
    val correctAnswer: String,
    val options: List<String>
)
