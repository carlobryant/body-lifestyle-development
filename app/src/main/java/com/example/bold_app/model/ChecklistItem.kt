package com.example.bold_app.model

data class ChecklistItem(
    val id: Int,

    val title: String,

    val reps: String,

    val description: String,

    val link: String? = null,

    var isDone: Boolean = false
)
