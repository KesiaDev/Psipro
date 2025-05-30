package com.example.psipro.ui.models

data class FaqItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
) 