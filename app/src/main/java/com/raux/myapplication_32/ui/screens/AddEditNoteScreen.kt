package com.raux.myapplication_32.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled. Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.raux.myapplication_32.data.Note
import com.raux.myapplication_32.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onNavigateBack: () -> Unit
) {
    val note = noteId?.let { viewModel.getNoteById(it) }
    val isEditing = note != null

    var title by remember { 
        mutableStateOf(TextFieldValue(note?.title ?: "")) 
    }
    var content by remember { 
        mutableStateOf(TextFieldValue(note?.content ?: "")) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать заметку" else "Новая заметка") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.text.isNotBlank()) {
                                if (isEditing) {
                                    viewModel.updateNote(noteId!!, title.text, content.text)
                                } else {
                                    viewModel.addNote(title.text, content.text)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = title.text.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Сохранить")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Содержание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
                
                Button(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateNote(noteId!!, title.text, content.text)
                        } else {
                            viewModel.addNote(title.text, content.text)
                        }
                        onNavigateBack()
                    },
                    enabled = title.text.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Сохранить" else "Создать")
                }
            }
        }
    }
}

