package com.raux.myapplication_32.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raux.myapplication_32.data.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class NotesViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSampleNotes()
    }

    private fun loadSampleNotes() {
        _isLoading.value = true
        viewModelScope.launch {
            // Добавляем несколько примеров заметок
            val sampleNotes = listOf(
                Note(
                    title = "Добро пожаловать!",
                    content = "Это ваша первая заметка. Вы можете создавать, редактировать и удалять заметки.",
                    createdAt = Date(System.currentTimeMillis() - 86400000) // вчера
                ),
                Note(
                    title = "Идеи для проекта",
                    content = "• Изучить Jetpack Compose\n• Создать красивое UI\n• Добавить анимации\n• Протестировать приложение",
                    createdAt = Date(System.currentTimeMillis() - 172800000) // позавчера
                )
            )
            _notes.value = sampleNotes
            _isLoading.value = false
        }
    }

    fun addNote(title: String, content: String) {
        val newNote = Note(
            title = title,
            content = content,
            createdAt = Date(),
            updatedAt = Date()
        )
        _notes.value = _notes.value + newNote
    }

    fun updateNote(noteId: String, title: String, content: String) {
        _notes.value = _notes.value.map { note ->
            if (note.id == noteId) {
                note.copy(
                    title = title,
                    content = content,
                    updatedAt = Date()
                )
            } else {
                note
            }
        }
    }

    fun deleteNote(noteId: String) {
        _notes.value = _notes.value.filter { it.id != noteId }
    }

    fun getNoteById(noteId: String): Note? {
        return _notes.value.find { it.id == noteId }
    }
}

