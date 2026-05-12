package com.example.food.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * A unified profile image component that handles:
 * 1. Base64-encoded images (stored in Firestore as "data:image/jpeg;base64,...")
 * 2. Regular HTTP URLs (loaded via Coil)
 * 3. Null/empty state (shows initials fallback)
 */
@Composable
fun ProfileImage(
    photoUrl: String?,
    displayName: String?,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent
) {
    val borderMod = if (borderWidth > 0.dp)
        modifier.clip(CircleShape).border(borderWidth, borderColor, CircleShape)
    else
        modifier.clip(CircleShape)

    when {
        photoUrl != null && photoUrl.startsWith("data:image") -> {
            // Base64 image — decode and render as Bitmap
            val bitmap = remember(photoUrl) {
                try {
                    val base64Part = photoUrl.substringAfter("base64,")
                    val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = borderMod,
                    contentScale = ContentScale.Crop
                )
            } else {
                InitialsFallback(displayName = displayName, modifier = borderMod)
            }
        }
        photoUrl != null && (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) -> {
            // Regular URL — use Coil
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile Picture",
                modifier = borderMod,
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            // No photo — show initials
            InitialsFallback(displayName = displayName, modifier = borderMod)
        }
    }
}

@Composable
private fun InitialsFallback(displayName: String?, modifier: Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF1E88E5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayName?.take(2)?.uppercase() ?: "??",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
