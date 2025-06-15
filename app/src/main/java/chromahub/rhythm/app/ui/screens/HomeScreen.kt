package chromahub.rhythm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import chromahub.rhythm.app.data.Album
import chromahub.rhythm.app.data.Artist
import chromahub.rhythm.app.data.Song
import chromahub.rhythm.app.ui.components.MiniPlayer
import chromahub.rhythm.app.ui.components.RhythmIcons
import chromahub.rhythm.app.util.ImageUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    recentlyPlayed: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onViewAllSongs: () -> Unit,
    onViewAllAlbums: () -> Unit,
    onViewAllArtists: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val featuredPagerState = rememberPagerState(pageCount = { minOf(5, albums.size) })
    
    // Animated scroll position for parallax effects
    val scrollOffset = remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
    
    // Auto-scroll featured albums every 5 seconds
    LaunchedEffect(key1 = Unit) {
        while(true) {
            delay(5000)
            if (featuredPagerState.pageCount > 0) {
                val nextPage = (featuredPagerState.currentPage + 1) % featuredPagerState.pageCount
                featuredPagerState.animateScrollToPage(nextPage)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rhythm",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = RhythmIcons.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                modifier = Modifier.graphicsLayer {
                    // Slight parallax and opacity effect for the app bar based on scroll
                    val scrollPercentage = (scrollOffset.value / 1000f).coerceIn(0f, 1f)
                    alpha = 1f - (scrollPercentage * 0.2f)
                    translationY = -scrollPercentage * 8
                }
            )
        },
        bottomBar = {}
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Featured albums carousel
            if (albums.isNotEmpty()) {
                item {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        HorizontalPager(
                            state = featuredPagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp),
                            pageSpacing = 16.dp,
                            pageContent = { page ->
                                val album = albums[page % albums.size]
                                FeaturedAlbumCard(
                                    album = album,
                                    onClick = { onAlbumClick(album) },
                                    pageOffset = (page - featuredPagerState.currentPage).toFloat().absoluteValue
                                )
                            }
                        )
                        
                        // Pager indicators with animation
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(featuredPagerState.pageCount) { iteration ->
                                val isSelected = featuredPagerState.currentPage == iteration
                                val color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                
                                // Animate indicator size and color
                                val indicatorSize by animateFloatAsState(
                                    targetValue = if (isSelected) 10f else 6f, 
                                    label = "indicator_size"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(indicatorSize.dp)
                                        .clickable { 
                                            scope.launch {
                                                featuredPagerState.animateScrollToPage(iteration)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
            
            // Recently played section with larger cards
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column {
                            SectionHeader(
                                title = "Recently Played",
                                onViewAll = {}
                            )
                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recentlyPlayed.take(10)) { song ->
                                    RecentlyPlayedCard(
                                        song = song,
                                        onClick = { onSongClick(song) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // For You section (personalized recommendations)
            if (songs.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column {
                            SectionHeader(
                                title = "For You",
                                subtitle = "Recommended based on your listening",
                                onViewAll = onViewAllSongs
                            )
                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(songs.shuffled().take(6)) { song ->
                                    SongCard(
                                        song = song,
                                        onClick = { onSongClick(song) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Popular Albums section with modern cards
            if (albums.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column {
                            SectionHeader(
                                title = "Popular Albums",
                                onViewAll = onViewAllAlbums
                            )
                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(albums.take(8)) { album ->
                                    AlbumCard(
                                        album = album,
                                        onClick = { onAlbumClick(album) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Top Artists section with circular images
            if (artists.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column {
                            SectionHeader(
                                title = "Top Artists",
                                onViewAll = onViewAllArtists
                            )
                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(artists.take(10)) { artist ->
                                    ArtistCard(
                                        artist = artist,
                                        onClick = { onArtistClick(artist) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Quick Picks section (horizontal song list)
            if (songs.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        Column {
                            SectionHeader(
                                title = "Quick Picks",
                                subtitle = "Jump back into your favorites",
                                onViewAll = onViewAllSongs
                            )
                            
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                songs.take(4).forEach { song ->
                                    QuickPickItem(
                                        song = song,
                                        onClick = { onSongClick(song) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Add bottom spacing for mini-player
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onViewAll: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onViewAll() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturedAlbumCard(
    album: Album,
    onClick: () -> Unit,
    pageOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Scale based on page offset
    val scale = lerp(
        start = 0.9f, 
        stop = 1f,
        fraction = 1f - (pageOffset * 0.25f).coerceIn(0f, 0.25f)
    )
    
    // Alpha based on page offset
    val alpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            focusedElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background image with blur
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .apply(ImageUtils.buildImageRequest(
                        album.artworkUri,
                        album.title,
                        context.cacheDir,
                        ImageUtils.PlaceholderType.ALBUM
                    ))
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 3.dp)
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Album info
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Album artwork (small)
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.size(80.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .apply(ImageUtils.buildImageRequest(
                                album.artworkUri,
                                album.title,
                                context.cacheDir,
                                ImageUtils.PlaceholderType.ALBUM
                            ))
                            .build(),
                        contentDescription = "Album artwork for ${album.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Album text info
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "${album.numberOfSongs} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                // Play button - using FilledIconButton from M3
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play album",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Animate on hover/press effect
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "scale",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp
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
                            ImageUtils.PlaceholderType.ALBUM
                        ))
                        .build(),
                    contentDescription = "Album artwork for ${album.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Play button overlay with M3 styling
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    FilledIconButton(
                        onClick = { 
                            isPressed = true
                            onClick() 
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                    .padding(16.dp)
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Text(
                    text = "${album.numberOfSongs} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Animate on hover/press effect
    var isHovered by remember { mutableStateOf(false) }
    val elevation by animateFloatAsState(
        targetValue = if (isHovered) 8f else 2f,
        label = "elevation"
    )
    
    Column(
        modifier = modifier
            .width(100.dp)
            .clickable { 
                onClick()
                isHovered = true 
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image with animated elevation
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    alpha = if (isHovered) 1f else 0.9f
                },
            shadowElevation = elevation.dp,
            tonalElevation = if (isHovered) 4.dp else 0.dp
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            artist.artworkUri,
                            artist.name,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.ARTIST
                        ))
                        .build(),
                    contentDescription = "Artist image for ${artist.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Subtle gradient overlay for better text visibility
                if (isHovered) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
    
    // Reset the hover state after animation
    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(300)
            isHovered = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPickItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art with shadow
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .apply(ImageUtils.buildImageRequest(
                            song.artworkUri,
                            song.title,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Song info with improved typography
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Text(
                    text = song.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Play button with tonal button style
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlyPlayedCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isHovered by remember { mutableStateOf(false) }
    
    // Scale animation on hover
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale_animation"
    )
    
    ElevatedCard(
        onClick = { 
            isHovered = true
            onClick() 
        },
        modifier = modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
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
                            song.artworkUri,
                            song.title,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Modern gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Play button with reveal animation
                if (isHovered) {
                    Box(
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        FilledIconButton(
                            onClick = onClick,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Play,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                // Always visible play indicator (smaller when not hovered)
                if (!isHovered) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Play,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    
    // Reset hover state after some time
    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(3000)
            isHovered = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    
    // Ripple animation when pressed
    val animatedElevation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 1f,
        label = "elevation_animation"
    )
    
    ElevatedCard(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = animatedElevation.dp
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
                            song.artworkUri,
                            song.title,
                            context.cacheDir,
                            ImageUtils.PlaceholderType.TRACK
                        ))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Modern material overlay 
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // Play button with modern styling
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(if (isPressed) 1f else 0.7f),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    
    // Reset pressed state after animation completes
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(500)
            isPressed = false
        }
    }
} 