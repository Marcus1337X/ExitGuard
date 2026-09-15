package com.exitguard.app.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToConfig: (String) -> Unit,
    onLaunchIntent: (Intent) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var appToDelete by remember { mutableStateOf<AppRuleItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ExitGuard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        // Egress status pill
                        EgressStatusRow(
                            exitInfo = state.currentExitInfo,
                            isDetecting = state.isDetectingExit,
                            errorMessage = state.exitDetectionError,
                            onRefresh = { viewModel.refreshExitInfo() }
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
                EmptyStateView(onAddClick = onNavigateToAdd)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.appItems, key = { it.rule.packageName }) { item ->
                        AppCard(
                            item = item,
                            onClick = {
                                viewModel.onAppClicked(item, onLaunchIntent)
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
                    text = "禁止启动: ${blockedState.appName}",
                    fontWeight = FontWeight.Bold,
                    color = AlertRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = blockedState.reason,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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
                                    text = "国家: $countryDesc",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    blockedState.rule?.let { rule ->
                        val ruleSummary = when (rule.mode) {
                            CheckMode.IP_STRICT -> "允许 IP: ${rule.allowedIps.ifEmpty { setOf("未设置") }.joinToString()}"
                            CheckMode.COUNTRY -> "允许国家: ${rule.allowedCountries.ifEmpty { setOf("未设置") }.joinToString()}"
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
    onRefresh: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
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
    }
}

@Composable
fun AppCard(
    item: AppRuleItem,
    onClick: () -> Unit,
    onConfigClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconImage(
                drawable = item.icon,
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
                        if (count == 0) "严格 IP: 未配置 (禁止启动)" else "严格 IP: $count 个允许"
                    }
                    CheckMode.COUNTRY -> {
                        val list = item.rule.allowedCountries
                        if (list.isEmpty()) "国家模式: 未配置 (禁止启动)" else "国家: ${list.joinToString(", ")}"
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

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = modeBadge,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onConfigClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
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
fun EmptyStateView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "尚未添加受保护应用",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "在 ExitGuard 中添加应用并配置独立出口 IP 或国家规则。每次点击启动时，将先检测公网出口，符合安全规则才放行启动。",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加应用")
        }
    }
}
