package com.exitguard.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconBitmapCache = LruCache<String, Bitmap>(200)

    suspend fun getInstalledLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val currentPackage = context.packageName
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        resolveInfos
            .mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                if (pkgName == currentPackage) return@mapNotNull null // Don't list ExitGuard itself

                val appName = try {
                    resolveInfo.loadLabel(packageManager).toString()
                } catch (e: Exception) {
                    pkgName
                }

                val icon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }

                val iconBitmap = icon?.let { d ->
                    rasterizeDrawable(d, 96, 96).also { bmp ->
                        if (bmp != null) iconBitmapCache.put(pkgName, bmp)
                    }
                }

                InstalledApp(
                    packageName = pkgName,
                    appName = appName,
                    icon = icon,
                    iconBitmap = iconBitmap
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun getAppIconBitmap(packageName: String): Bitmap? {
        iconBitmapCache.get(packageName)?.let { return it }
        val drawable = getAppIcon(packageName) ?: return null
        return rasterizeDrawable(drawable, 96, 96)?.also {
            iconBitmapCache.put(packageName, it)
        }
    }

    private fun rasterizeDrawable(drawable: Drawable, targetWidth: Int, targetHeight: Int): Bitmap? {
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 144) else targetWidth
                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 144) else targetHeight
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
