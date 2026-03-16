package com.psipro.app.ui.models

data class FaqItem(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
) 



