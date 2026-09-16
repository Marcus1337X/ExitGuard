package com.exitguard.app.ui.config

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exitguard.app.model.CheckMode
import com.exitguard.app.model.CheckResult
import com.exitguard.app.ui.components.AppIconImage
import com.exitguard.app.ui.theme.AlertRed
import com.exitguard.app.ui.theme.SafeGreen
import com.exitguard.app.ui.theme.WarningOrange

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var ipInput by remember { mutableStateOf("") }
    var countryInput by remember { mutableStateOf("") }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val handleBackAction = {
        if (state.hasUnsavedChanges) {
            showExitConfirmDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler(enabled = true) {
        handleBackAction()
    }

    LaunchedEffect(state.isSavedMessageVisible) {
        if (state.isSavedMessageVisible) {
            Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
            viewModel.hideSavedMessage()
            onNavigateBack()
        }
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.refreshExitInfo()
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("放弃未保存的更改？") },
            text = { Text("当前配置尚未保存，确定要退出吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("退出", color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("继续编辑")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "规则配置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = handleBackAction) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconImage(drawable = state.icon, size = 52.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.packageName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Real-time Egress Status & Live Match Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "实时公网出口状态",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            IconButton(
                                onClick = { viewModel.refreshExitInfo() },
                                modifier = Modifier.size(28.dp),
                                enabled = !state.isDetectingExit
                            ) {
                                if (state.isDetectingExit) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "刷新出口",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.currentExitInfo != null) {
                            val exit = state.currentExitInfo!!
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "IP: ",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = exit.ip,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "地区: ",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (exit.country.isNotEmpty()) "${exit.country} (${exit.countryCode})" else exit.countryCode,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Match Status Banner
                            val evaluation = state.currentMatchEvaluation
                            val isAllowed = evaluation is CheckResult.Allowed
                            val isNotConfigured = when (state.mode) {
                                CheckMode.IP_STRICT -> state.allowedIps.isEmpty()
                                CheckMode.COUNTRY -> state.allowedCountries.isEmpty()
                            }
                            val statusBg = when {
                                isNotConfigured -> WarningOrange.copy(alpha = 0.15f)
                                isAllowed -> SafeGreen.copy(alpha = 0.15f)
                                else -> AlertRed.copy(alpha = 0.15f)
                            }
                            val statusColor = when {
                                isNotConfigured -> WarningOrange
                                isAllowed -> SafeGreen
                                else -> AlertRed
                            }
                            val statusIcon = when {
                                isNotConfigured -> Icons.Default.Warning
                                isAllowed -> Icons.Default.Check
                                else -> Icons.Default.Close
                            }
                            val statusText = when {
                                isNotConfigured -> when (state.mode) {
                                    CheckMode.IP_STRICT -> "尚未配置任何允许的 IPv4 地址 (禁止启动)"
                                    CheckMode.COUNTRY -> "尚未配置任何允许的地区代码 (禁止启动)"
                                }
                                isAllowed -> "当前出口匹配本配置 (允许启动)"
                                evaluation is CheckResult.Denied -> "未匹配: ${evaluation.reason}"
                                else -> "当前出口未通过配置匹配 (禁止启动)"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = if (state.isDetectingExit) "正在获取公网出口信息..." else "未能获取公网出口信息，点击右侧刷新重试",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Mode Selector
            item {
                Text(
                    text = "检测模式",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = state.mode == CheckMode.IP_STRICT,
                        onClick = { viewModel.setMode(CheckMode.IP_STRICT) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("严格 IP 模式")
                    }
                    SegmentedButton(
                        selected = state.mode == CheckMode.COUNTRY,
                        onClick = { viewModel.setMode(CheckMode.COUNTRY) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("匹配地区模式")
                    }
                }
            }

            // Input error banner if any
            state.inputError?.let { err ->
                item {
                    Text(
                        text = err,
                        color = AlertRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Mode-specific content
            if (state.mode == CheckMode.IP_STRICT) {
                // Strict IP Mode Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "允许出口 IP 列表 (${state.allowedIps.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            // Quick add current IP
                            val currentIp = state.currentExitInfo?.ip
                            if (currentIp != null) {
                                OutlinedButton(
                                    onClick = { viewModel.addCurrentExitIp() },
                                    enabled = !state.allowedIps.contains(currentIp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加当前出口IP", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // IP Input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = ipInput,
                                onValueChange = {
                                    ipInput = it
                                    viewModel.clearInputError()
                                },
                                placeholder = { Text("IPv4地址") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (ipInput.isNotBlank()) {
                                            viewModel.addIp(ipInput)
                                            ipInput = ""
                                        }
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (ipInput.isNotBlank()) {
                                        viewModel.addIp(ipInput)
                                        ipInput = ""
                                    }
                                },
                                enabled = ipInput.isNotBlank()
                            ) {
                                Text("添加")
                            }
                        }
                    }
                }

                items(state.allowedIps.toList(), key = { it }) { ip ->
                    RuleEntryCard(
                        text = ip,
                        onDelete = { viewModel.removeIp(ip) }
                    )
                }
            } else {
                // Country/Region Mode Section
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "允许出口地区 (${state.allowedCountries.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            val currentCountry = state.currentExitInfo?.countryCode
                            if (currentCountry != null) {
                                OutlinedButton(
                                    onClick = { viewModel.addCurrentExitCountry() },
                                    enabled = !state.allowedCountries.contains(currentCountry)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("添加当前出口地区", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Region Code Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = countryInput,
                                onValueChange = {
                                    countryInput = it.uppercase().take(2)
                                    viewModel.clearInputError()
                                },
                                placeholder = { Text("两位地区代码") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (countryInput.isNotBlank()) {
                                            viewModel.addCountry(countryInput)
                                            countryInput = ""
                                        }
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (countryInput.isNotBlank()) {
                                        viewModel.addCountry(countryInput)
                                        countryInput = ""
                                    }
                                },
                                enabled = countryInput.length == 2
                            ) {
                                Text("添加")
                            }
                        }
                    }
                }

                items(state.allowedCountries.toList(), key = { it }) { countryCode ->
                    RuleEntryCard(
                        text = countryCode,
                        onDelete = { viewModel.removeCountry(countryCode) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.saveRule() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("保存配置", fontSize = 16.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun RuleEntryCard(
    text: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
