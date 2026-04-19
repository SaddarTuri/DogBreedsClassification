package com.saddar.dogbreeds.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saddar.dogbreeds.R
import com.saddar.dogbreeds.data.BreedResult
import com.saddar.dogbreeds.ui.theme.*
import com.saddar.dogbreeds.viewmodel.BreedUiState
import kotlinx.coroutines.launch

// ── Tab definitions ───────────────────────────────────────────────────────────

enum class BottomTab { IDENTIFY, BREEDS }

private data class BreedInfo(
    val name: String,
    val traits: List<String>,
    val emoji: String
)

private val ALL_BREEDS = listOf(
    BreedInfo("Akita",             listOf("Loyal", "Dignified", "Protective"),  "🐕"),
    BreedInfo("Beagle",            listOf("Friendly", "Curious", "Merry"),       "🐶"),
    BreedInfo("Boxer",             listOf("Playful", "Bright", "Energetic"),     "🥊"),
    BreedInfo("Bulldog",           listOf("Calm", "Courageous", "Friendly"),     "💪"),
    BreedInfo("Chihuahua",         listOf("Alert", "Sassy", "Devoted"),          "🌮"),
    BreedInfo("Doberman",          listOf("Alert", "Fearless", "Loyal"),         "🛡️"),
    BreedInfo("Golden Retriever",  listOf("Friendly", "Reliable", "Gentle"),     "🌟"),
    BreedInfo("Husky",             listOf("Athletic", "Alert", "Gentle"),        "❄️"),
    BreedInfo("Labrador",          listOf("Friendly", "Active", "Outgoing"),     "🎾"),
    BreedInfo("Yorkshire Terrier", listOf("Affectionate", "Spirited", "Bold"),   "🎀"),
)

// ── Root screen ───────────────────────────────────────────────────────────────

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
    var selectedTab by remember { mutableStateOf(BottomTab.IDENTIFY) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onClose = { scope.launch { drawerState.close() } },
                onShare = onShareClick
            )
        }
    ) {
        Scaffold(
            containerColor = LightLavender,
            topBar = {
                AppTopBar(onMenuClick = { scope.launch { drawerState.open() } })
            },
            bottomBar = {
                AppBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onCameraClick = onCameraClick
                )
            }
        ) { innerPadding ->
            when (selectedTab) {
                BottomTab.IDENTIFY -> IdentifyContent(
                    uiState = uiState,
                    innerPadding = innerPadding,
                    onCameraClick = onCameraClick,
                    onGalleryClick = onGalleryClick,
                    onDetectClick = onDetectClick
                )
                BottomTab.BREEDS -> BreedsContent(innerPadding = innerPadding)
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun AppTopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = DarkNavy,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = "Dog Breeds",
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = DarkNavy
        )

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(DarkNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_paw),
                contentDescription = "App icon",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── IDENTIFY tab content ──────────────────────────────────────────────────────

@Composable
private fun IdentifyContent(
    uiState: BreedUiState,
    innerPadding: PaddingValues,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDetectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ── Image preview card (no overlaid buttons) ──────────────────
        ImagePreviewCard(uiState = uiState)

        // ── Camera & Gallery buttons BELOW the image ──────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkNavy)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Camera",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            OutlinedButton(
                onClick = onGalleryClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DarkNavy)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Gallery",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        // ── Result card ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.detectionResult != null,
            enter = slideInVertically(tween(400)) { it / 2 } + fadeIn(tween(400)),
            exit  = fadeOut(tween(200))
        ) {
            uiState.detectionResult?.let { ResultCard(it) }
        }

        // ── Status / hint text ────────────────────────────────────────
        if (uiState.detectionResult == null && uiState.statusMessage.isNotBlank()) {
            Text(
                text = uiState.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        // ── Identify Breed button ─────────────────────────────────────
        IdentifyBreedButton(isDetecting = uiState.isDetecting, onClick = onDetectClick)

        Spacer(Modifier.height(8.dp))
    }
}

// ── Image preview card (clean, no overlaid buttons) ──────────────────────────

@Composable
private fun ImagePreviewCard(uiState: BreedUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0D4EA))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
        }
    }
}

// ── Result card ───────────────────────────────────────────────────────────────

@Composable
private fun ResultCard(result: BreedResult) {
    val isUnidentified = result.breedName == "Unidentified Breed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DETECTION RESULT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (isUnidentified) Color(0xFFFFE0E0) else ChipBlue,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUnidentified) "LOW CONFIDENCE" else "BREED IDENTIFIED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = if (isUnidentified) Color(0xFFB00020) else DarkNavy
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Breed name
            Text(
                text = result.breedName,
                style = MaterialTheme.typography.headlineLarge,
                color = if (isUnidentified) Color(0xFFB00020) else DarkNavy
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
                        color = if (isUnidentified) Color(0xFFB00020) else DeepBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Confidence bar
            LinearProgressIndicator(
                progress = result.confidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (isUnidentified) Color(0xFFE53935) else DeepBlue,
                trackColor = ProgressTrack
            )

            Spacer(Modifier.height(18.dp))

            // Trait chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                result.traits.forEach { TraitChip(it) }
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

// ── BREEDS tab content ────────────────────────────────────────────────────────

@Composable
private fun BreedsContent(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Supported Breeds",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = DarkNavy,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "10 breeds the AI can identify",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
            Spacer(Modifier.height(8.dp))
        }

        items(ALL_BREEDS) { breed ->
            BreedListCard(breed)
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun BreedListCard(breed: BreedInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(ChipBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(breed.emoji, fontSize = 26.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = breed.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    breed.traits.forEach { trait ->
                        Box(
                            modifier = Modifier
                                .background(LightLavender, RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = trait,
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkNavy
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun AppBottomBar(
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
            // Identify tab
            BottomNavItem(
                icon = { Icon(Icons.Default.Search, null, Modifier.size(24.dp)) },
                label = "IDENTIFY",
                selected = selectedTab == BottomTab.IDENTIFY,
                onClick = { onTabSelected(BottomTab.IDENTIFY) }
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

            // Breeds tab
            BottomNavItem(
                icon = { Icon(Icons.Default.Pets, null, Modifier.size(24.dp)) },
                label = "BREEDS",
                selected = selectedTab == BottomTab.BREEDS,
                onClick = { onTabSelected(BottomTab.BREEDS) }
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
        CompositionLocalProvider(LocalContentColor provides tint) { icon() }
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
private fun AppDrawerContent(onClose: () -> Unit, onShare: () -> Unit) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Brush.linearGradient(colors = listOf(DarkNavy, NavyLight)))
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
                Text("Dog Breeds", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("AI-powered breed identifier", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
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
