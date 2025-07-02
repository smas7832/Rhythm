package chromahub.rhythm.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistBottomSheet(
    onDismissRequest: () -> Unit,
    onAddToPlaylist: (String) -> Unit, // Placeholder for adding to existing playlist
    onCreateNewPlaylist: () -> Unit, // Placeholder for creating new playlist
    playlists: List<String> // Placeholder for list of existing playlists
) {
    val modalBottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() },
        sheetState = modalBottomSheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Add to Playlist",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Option to create a new playlist
            ListItem(
                headlineContent = { Text("Create New Playlist") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Playlist"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { modalBottomSheetState.hide() }.invokeOnCompletion {
                            if (!modalBottomSheetState.isVisible) {
                                onCreateNewPlaylist()
                            }
                        }
                    }
            )

            // List of existing playlists
            LazyColumn {
                items(playlists) { playlistName ->
                    ListItem(
                        headlineContent = { Text(playlistName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { modalBottomSheetState.hide() }.invokeOnCompletion {
                                    if (!modalBottomSheetState.isVisible) {
                                        onAddToPlaylist(playlistName)
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}
