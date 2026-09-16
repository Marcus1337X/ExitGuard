package com.exitguard.app.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import com.exitguard.app.ui.components.AppIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.exitguard.app.model.CheckMode
import com.exitguard.app.ui.components.AppIconImage
import com.exitguard.app.ui.theme.AlertRed
import com.exitguard.app.ui.theme.SafeGreen
import com.exitguard.app.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToConfig: (String) -> Unit,
    onLaunchIntent: (Intent) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var appToDelete by remember { mutableStateOf<AppRuleItem?>(null) }
    var pendingLaunchPackage by remember { mutableStateOf<String?>(null) }
    var pendingLaunchTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var isAppStopped by remember { mutableStateOf(false) }
    var showPermissionHint by remember { mutableStateOf(false) }

    // Reset pending confirmation after 1500ms
    androidx.compose.runtime.LaunchedEffect(pendingLaunchPackage, pendingLaunchTime) {
        if (pendingLaunchPackage != null) {
            kotlinx.coroutines.delay(1500L)
            pendingLaunchPackage = null
        }
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_STOP) {
        isAppStopped = true
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        isAppStopped = false
        showPermissionHint = false
        viewModel.refreshExitInfo()
    }

    val handleLaunchIntent: (Intent) -> Unit = { intent ->
        isAppStopped = false
        onLaunchIntent(intent)
        coroutineScope.launch {
            kotlinx.coroutines.delay(400L)
            // If the activity did not stop, the system modal is blocking foreground jump
            if (!isAppStopped) {
                showPermissionHint = true
            }
        }
    }

    val context = LocalContext.current
    val appLogo = remember {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIconImage(
                                drawable = appLogo,
                                size = 26.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ExitGuard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        // Egress status pill & permission hint
                        EgressStatusRow(
                            exitInfo = state.currentExitInfo,
                            isDetecting = state.isDetectingExit,
                            errorMessage = state.exitDetectionError,
                            onRefresh = { viewModel.refreshExitInfo() },
                            permissionHintVisible = showPermissionHint
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshExitInfo() },
                        enabled = !state.isDetectingExit
                    ) {
                        if (state.isDetectingExit) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新出口 IP"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                modifier = Modifier.padding(end = 12.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加应用"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.appItems.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.appItems, key = { it.rule.packageName }) { item ->
                        val isPending = pendingLaunchPackage == item.rule.packageName
                        AppCard(
                            item = item,
                            isPendingConfirmation = isPending,
                            onClick = {
                                val isConfigured = when (item.rule.mode) {
                                    CheckMode.IP_STRICT -> item.rule.allowedIps.isNotEmpty()
                                    CheckMode.COUNTRY -> item.rule.allowedCountries.isNotEmpty()
                                }
                                if (!isConfigured) {
                                    android.widget.Toast.makeText(context, "该应用尚未配置规则，请点击右侧齿轮配置", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    val now = System.currentTimeMillis()
                                    if (pendingLaunchPackage == item.rule.packageName && now - pendingLaunchTime < 1500L) {
                                        pendingLaunchPackage = null
                                        viewModel.onAppClicked(item, handleLaunchIntent)
                                    } else {
                                        pendingLaunchPackage = item.rule.packageName
                                        pendingLaunchTime = now
                                    }
                                }
                            },
                            onConfigClick = {
                                onNavigateToConfig(item.rule.packageName)
                            },
                            onDeleteClick = {
                                appToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Launch Checking Dialog
    if (state.launchDialogState is LaunchDialogState.Checking) {
        val checkingState = state.launchDialogState as LaunchDialogState.Checking
        Dialog(onDismissRequest = { /* Non-cancellable during security verification */ }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在检测公网出口...",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "正在核对 ${checkingState.appName} 独立出口规则",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Launch Blocked Alert Dialog
    if (state.launchDialogState is LaunchDialogState.Blocked) {
        val blockedState = state.launchDialogState as LaunchDialogState.Blocked
        AlertDialog(
            onDismissRequest = { viewModel.dismissLaunchDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AlertRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "启动被拦截",
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "应用 \"${blockedState.appName}\" 因出口安全规则未满足而被拦截启动：",
                        fontSize = 14.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AlertRed.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = blockedState.reason,
                            color = AlertRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (blockedState.exitInfo != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "当前检测到的出口:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "IP: ${blockedState.exitInfo.ip}",
                                    fontSize = 13.sp
                                )
                                val countryDesc = if (blockedState.exitInfo.country.isNotEmpty()) {
                                    "${blockedState.exitInfo.country} (${blockedState.exitInfo.countryCode})"
                                } else {
                                    blockedState.exitInfo.countryCode
                                }
                                Text(
                                    text = "地区: $countryDesc",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    blockedState.rule?.let { rule ->
                        val ruleSummary = when (rule.mode) {
                            CheckMode.IP_STRICT -> "允许 IP: ${rule.allowedIps.ifEmpty { setOf("未设置") }.joinToString()}"
                            CheckMode.COUNTRY -> "允许地区: ${rule.allowedCountries.ifEmpty { setOf("未设置") }.joinToString()}"
                        }
                        Text(
                            text = "配置规则: $ruleSummary",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "根据安全策略，未通过验证的应用严禁启动。",
                        fontSize = 12.sp,
                        color = AlertRed
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissLaunchDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("确定")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    appToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = { Text(text = "移除受保护应用") },
            text = { Text("确定要从 ExitGuard 移除 ${item.rule.appName} 及其规则吗？这不会卸载该应用。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApp(item.rule.packageName)
                        appToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(onClick = { appToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun EgressStatusRow(
    exitInfo: com.exitguard.app.model.ExitInfo?,
    isDetecting: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    permissionHintVisible: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(enabled = !isDetecting, onClick = onRefresh)
            .padding(vertical = 2.dp)
    ) {
        val (dotColor, statusText) = when {
            isDetecting -> WarningOrange to "出口检测中..."
            errorMessage != null -> AlertRed to "检测失败: $errorMessage"
            exitInfo != null -> SafeGreen to "${exitInfo.ip} (${exitInfo.countryCode})"
            else -> Color.Gray to "未获取出口信息"
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        AnimatedVisibility(
            visible = permissionHintVisible,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarningOrange.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.8f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.TouchApp,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "点击下方授权「始终允许」",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(
    item: AppRuleItem,
    isPendingConfirmation: Boolean = false,
    onClick: () -> Unit,
    onConfigClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isPendingConfirmation) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isPendingConfirmation) {
            androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor)
        } else {
            androidx.compose.foundation.BorderStroke(1.5.dp, Color.Transparent)
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPendingConfirmation) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconImage(
                drawable = item.icon,
                bitmap = item.iconBitmap,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.rule.appName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                val modeBadge = when (item.rule.mode) {
                    CheckMode.IP_STRICT -> {
                        val count = item.rule.allowedIps.size
                        if (count == 0) "严格 IP: 未配置" else "严格 IP: $count 个允许"
                    }
                    CheckMode.COUNTRY -> {
                        val list = item.rule.allowedCountries
                        if (list.isEmpty()) "匹配地区: 未配置" else "匹配地区: ${list.joinToString(", ")}"
                    }
                }

                val badgeColor = if (
                    (item.rule.mode == CheckMode.IP_STRICT && item.rule.allowedIps.isEmpty()) ||
                    (item.rule.mode == CheckMode.COUNTRY && item.rule.allowedCountries.isEmpty())
                ) {
                    AlertRed
                } else {
                    MaterialTheme.colorScheme.primary
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPendingConfirmation) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = primaryColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "再按一次启动",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = modeBadge,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onConfigClick) {
                Icon(
                    imageVector = AppIcons.Settings,
                    contentDescription = "配置规则",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "移除应用",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "暂无受保护应用",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角「+」添加需要保护的应用",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
