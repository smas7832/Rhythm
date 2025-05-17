package chromahub.rhythm.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.data.Song

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    song: Song? = null,
    onConfirmWithSong: (String) -> Unit = {}
) {
    var playlistName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var addSong by remember { mutableStateOf(song != null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { 
                        playlistName = it
                        isError = it.isBlank()
                    },
                    label = { Text("Playlist Name") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Playlist name cannot be empty")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (song != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = addSong,
                            onCheckedChange = { addSong = it }
                        )
                        
                        Text(
                            text = "Add \"${song.title}\" to this playlist",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistName.isBlank()) {
                        isError = true
                    } else {
                        if (song != null && addSong) {
                            onConfirmWithSong(playlistName)
                        } else {
                            onConfirm(playlistName)
                        }
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 