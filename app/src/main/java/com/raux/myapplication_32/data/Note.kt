package com.raux.myapplication_32.data

import java.util.Date
import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

