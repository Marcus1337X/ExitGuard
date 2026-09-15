package com.exitguard.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

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

                InstalledApp(
                    packageName = pkgName,
                    appName = appName,
                    icon = icon
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
