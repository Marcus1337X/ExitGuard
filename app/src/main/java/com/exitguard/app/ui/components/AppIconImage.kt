package com.exitguard.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Global LRU cache for Drawables converted to Bitmap, avoiding repeated Canvas draws on main thread
private val drawableBitmapCache = LruCache<Int, Bitmap>(200)

@Composable
fun AppIconImage(
    drawable: Drawable? = null,
    bitmap: Bitmap? = null,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val finalBitmap = remember(bitmap, drawable) {
        if (bitmap != null) {
            bitmap
        } else if (drawable != null) {
            val key = System.identityHashCode(drawable)
            drawableBitmapCache.get(key) ?: run {
                try {
                    if (drawable is BitmapDrawable && drawable.bitmap != null) {
                        drawable.bitmap.also { drawableBitmapCache.put(key, it) }
                    } else {
                        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 144) else 96
                        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 144) else 96
                        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp.also { drawableBitmapCache.put(key, it) }
                    }
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    if (finalBitmap != null) {
        Image(
            bitmap = finalBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AppIcons.Android,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

