package com.saddar.dogbreeds.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saddar.dogbreeds.R
import com.saddar.dogbreeds.data.BreedResult
import com.saddar.dogbreeds.ui.theme.*
import com.saddar.dogbreeds.viewmodel.BreedUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class BottomTab { DISCOVER, LIBRARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: BreedUiState,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDetectClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(BottomTab.DISCOVER) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CanineDrawerContent(
                onClose = { scope.launch { drawerState.close() } },
                onShare = onShareClick
            )
        }
    ) {
        Scaffold(
            containerColor = LightLavender,
            topBar = {
                CanineTopBar(onMenuClick = { scope.launch { drawerState.open() } })
            },
            bottomBar = {
                CanineBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onCameraClick = onCameraClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Image card with overlaid camera/gallery buttons ───────
                ImagePreviewCard(
                    uiState = uiState,
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick
                )

                // ── Detection result card (animated appearance) ───────────
                AnimatedVisibility(
                    visible = uiState.detectionResult != null,
                    enter = slideInVertically(tween(400)) { it / 2 } + fadeIn(tween(400)),
                    exit  = fadeOut(tween(200))
                ) {
                    uiState.detectionResult?.let { ResultCard(it) }
                }

                // ── Status / hint text ────────────────────────────────────
                if (uiState.detectionResult == null && uiState.statusMessage.isNotBlank()) {
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // ── Identify Breed button ─────────────────────────────────
                IdentifyBreedButton(
                    isDetecting = uiState.isDetecting,
                    onClick = onDetectClick
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun CanineTopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = DarkNavy,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = "Canine Intel",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = DarkNavy
        )

        // Circular icon button (right)
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(DarkNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = "App",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Image preview card ────────────────────────────────────────────────────────

@Composable
private fun ImagePreviewCard(
    uiState: BreedUiState,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0D4EA))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Dog image
            if (uiState.currentBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = uiState.currentBitmap.asImageBitmap(),
                    contentDescription = "Dog photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.dog),
                    contentDescription = "Dog photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x99000000))
                        )
                    )
            )

            // Camera + Gallery overlay buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverlayButton(
                    icon = { Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp)) },
                    label = "Camera",
                    onClick = onCameraClick
                )
                OverlayButton(
                    icon = { Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)) },
                    label = "Gallery",
                    onClick = onGalleryClick
                )
            }
        }
    }
}

@Composable
private fun OverlayButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = DarkNavy
            )
        }
    }
}

// ── Result card ───────────────────────────────────────────────────────────────

@Composable
private fun ResultCard(result: BreedResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {

            // Header row: "CURATED RESULT" + "ELITE IDENTIFICATION" chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURATED RESULT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(ChipBlue, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ELITE IDENTIFICATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = DarkNavy
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Breed name
            Text(
                text = result.breedName,
                style = MaterialTheme.typography.headlineLarge,
                color = DarkNavy
            )

            Spacer(Modifier.height(18.dp))

            // Confidence row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI CONFIDENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${"%.1f".format(result.confidence * 100)}%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = DeepBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = result.confidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = DeepBlue,
                trackColor = ProgressTrack
            )

            Spacer(Modifier.height(18.dp))

            // Trait chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                result.traits.forEach { trait ->
                    TraitChip(trait)
                }
            }
        }
    }
}

@Composable
private fun TraitChip(label: String) {
    Box(
        modifier = Modifier
            .background(ChipBlue, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = DarkNavy
        )
    }
}

// ── Identify Breed button ─────────────────────────────────────────────────────

@Composable
private fun IdentifyBreedButton(isDetecting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isDetecting,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkNavy,
            disabledContainerColor = DarkNavy.copy(alpha = 0.5f)
        )
    ) {
        if (isDetecting) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
            Spacer(Modifier.width(12.dp))
            Text("Identifying…", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        } else {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(10.dp))
            Text("Identify Breed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun CanineBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCameraClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Discover tab
            BottomNavItem(
                icon = { Icon(Icons.Default.Explore, null, Modifier.size(24.dp)) },
                label = "DISCOVER",
                selected = selectedTab == BottomTab.DISCOVER,
                onClick = { onTabSelected(BottomTab.DISCOVER) }
            )

            // Center camera FAB
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(DarkNavy, CircleShape)
                    .clickable(onClick = onCameraClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Take photo",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Library tab
            BottomNavItem(
                icon = { Icon(Icons.Default.LibraryBooks, null, Modifier.size(24.dp)) },
                label = "LIBRARY",
                selected = selectedTab == BottomTab.LIBRARY,
                onClick = { onTabSelected(BottomTab.LIBRARY) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) DarkNavy else TextGray
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            icon()
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = tint
        )
    }
}

// ── Navigation drawer ─────────────────────────────────────────────────────────

@Composable
private fun CanineDrawerContent(
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(DarkNavy, NavyLight)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_paw),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Canine Intel", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Let's have fun with pets", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Spacer(Modifier.height(12.dp))

        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.HelpOutline, null) },
            label = { Text("How to Use") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Share, null) },
            label = { Text("Share App") },
            selected = false,
            onClick = { onShare(); onClose() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Outlined.Info, null) },
            label = { Text("About") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
