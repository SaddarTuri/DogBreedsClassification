package com.saddar.dogbreeds.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saddar.dogbreeds.R
import com.saddar.dogbreeds.ui.theme.ChipBlue
import com.saddar.dogbreeds.ui.theme.DarkNavy
import com.saddar.dogbreeds.ui.theme.DeepBlue
import com.saddar.dogbreeds.ui.theme.OverlayWhite
import com.saddar.dogbreeds.ui.theme.ProgressTrack
import com.saddar.dogbreeds.ui.theme.TextGray
import androidx.compose.foundation.Image

@Composable
fun SplashScreen(
    isModelReady: Boolean,
    onBeginCurating: () -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = if (isModelReady) 1f else 0.65f,
        animationSpec = tween(durationMillis = if (isModelReady) 400 else 1800),
        label = "progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen background dog photo ──────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.dog),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Top scrim — let text read clearly
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x88000000), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Upper content area ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // "ADVANCED AI" badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 20.dp)
                        .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "ADVANCED AI",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // "Dog Breeds" hero text
                Text(
                    text = "Dog Breeds",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    color = DarkNavy,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 54.sp,
                        letterSpacing = (-1.5).sp
                    )
                )
            }

            // ── Bottom frosted panel ──────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = OverlayWhite,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp, vertical = 28.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = DeepBlue,
                        trackColor = ProgressTrack
                    )

                    Spacer(Modifier.height(10.dp))

                    // Status row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (!isModelReady) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = DeepBlue
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isModelReady) "READY" else "INITIALIZING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // CTA button
                    Button(
                        onClick = onBeginCurating,
                        enabled = isModelReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavy,
                            disabledContainerColor = DarkNavy.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = "Start Detection",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
