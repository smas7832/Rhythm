package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.ui.components.M3PlaceholderType
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistBottomSheet(
    artist: Artist,
    songs: List<Song>,
    albums: List<Album>,
    onDismiss: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    val context = LocalContext.current
    val viewModel = viewModel<MusicViewModel>()
    
    // Filter songs and albums for this artist
    val artistSongs = remember(songs, artist) {
        songs.filter { it.artist == artist.name }
    }
    
    val artistAlbums = remember(albums, artist) {
        albums.filter { it.artist == artist.name }
    }
    
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(500),
        label = "headerAlpha"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(700),
        label = "contentAlpha"
    )
    
    val contentTranslation by animateFloatAsState(
        targetValue = if (showContent) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "contentTranslation"
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Artist Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .graphicsLayer { alpha = headerAlpha }
                ) {
                    // Artist Image
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(ImageUtils.buildImageRequest(
                                artist.artworkUri,
                                artist.name,
                                context.cacheDir,
                                M3PlaceholderType.ARTIST
                            ))
                            .build(),
                        contentDescription = "Artist image for ${artist.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Enhanced gradient overlay with multiple layers
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    )
                    
                    // Horizontal gradient for more depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                    
                    // Close button with improved design
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Artist info at bottom with enhanced layout
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${artistSongs.size} songs • ${artistAlbums.size} albums",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action buttons row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Play All button
                            FilledTonalButton(
                                onClick = { 
                                    // Create a queue of all artist songs and play from start
                                    viewModel.playQueue(artistSongs)
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Play,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Play All")
                            }
                            
                            // Shuffle button
                            FilledIconButton(
                                onClick = { 
                                    // Enable shuffle and play shuffled artist songs
                                    viewModel.toggleShuffle()
                                    viewModel.playQueue(artistSongs.shuffled())
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Shuffle,
                                    contentDescription = "Shuffle play",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Albums Section with animation
            if (artistAlbums.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .graphicsLayer { 
                                alpha = contentAlpha
                                translationY = contentTranslation
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Albums",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "${artistAlbums.size} albums",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(artistAlbums) { album ->
                                ArtistAlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Songs Section with animation
            if (artistSongs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .graphicsLayer { 
                                alpha = contentAlpha
                                translationY = contentTranslation
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Songs",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${artistSongs.size} songs",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                items(artistSongs) { song ->
                    ArtistSongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        modifier = Modifier.graphicsLayer { 
                            alpha = contentAlpha
                            translationY = contentTranslation
                        }
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                            .graphicsLayer { alpha = contentAlpha }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistAlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            album.artworkUri,
                            album.title,
                            context.cacheDir,
                            M3PlaceholderType.ALBUM
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    FilledIconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = "Play album",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${album.numberOfSongs} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistSongItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = song.album,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            song.artworkUri,
                            song.title,
                            context.cacheDir,
                            M3PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        trailingContent = {
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play song",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}
