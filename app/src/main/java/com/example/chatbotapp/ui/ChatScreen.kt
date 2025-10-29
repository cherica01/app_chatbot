package com.example.chatbotapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel) {
    val ctx = LocalContext.current

    // États du ViewModel
    val messages by vm.messages.collectAsStateWithLifecycle()
    val input by vm.input.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val isListening by vm.isListening.collectAsStateWithLifecycle()
    val ttsSpeaking by vm.ttsSpeaking.collectAsStateWithLifecycle()

    // Vérification permission micro
    var hasRecord by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecord = granted
        if (granted) {
            vm.startListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Liste des messages
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Indicateur de chargement
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Indicateur d'écoute
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🎤 Écoute...", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Indicateur de parole
        if (ttsSpeaking) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("🔊 Parle...", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Champ de saisie et boutons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = vm::onInputChange,
                placeholder = { Text("Écrire votre message...") },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && !isListening
            )

            Button(
                onClick = { vm.send() },
                enabled = input.isNotBlank() && !isLoading && !isListening && !ttsSpeaking
            ) {
                Text("Envoyer")
            }

            // Bouton microphone
            FilledTonalButton(
                onClick = {
                    if (!hasRecord) {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    } else if (isListening) {
                        vm.stopListening()
                    } else {
                        vm.startListening()
                    }
                },
                enabled = !isLoading && !ttsSpeaking
            ) {
                if (isListening) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("🎤")
                }
            }

            // Bouton arrêter la parole
            if (ttsSpeaking) {
                FilledTonalButton(
                    onClick = { vm.stopSpeaking() },
                    enabled = true
                ) {
                    Text("🔇")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (message.role) {
                "👤" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Text(
            text = "${message.role} ${message.text}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}