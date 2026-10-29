package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderDark
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.OledBlack
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VermillionRed
import com.example.i18n.LanguageManager
import com.example.i18n.i18n
import com.example.i18n.tr
import com.example.ui.theme.FengShiTheme
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsAboutDialog(
    viewModel: EventViewModel,
    onDismiss: () -> Unit,
    onReplayOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showJsonImportDialog by remember { mutableStateOf(false) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var showAiTemplateDialog by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteSampleConfirm by remember { mutableStateOf(false) }
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("settings_about_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREFERENCES & DATA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = i18n("设置与数据备份", "設置與數據備份"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Philosophy Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(8.dp))
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = i18n("「逢时」· 极简东方吉日记录", "「逢時」· 極簡東方吉日記錄"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FengShiTheme.colors.textPrimary,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = i18n(
                            "遵循 Local-First（本地优先）设计哲学。零账号体系、零云端依赖、零冗余后台。内置百年离线公农历算法，纯黑 OLED 留白画报美学，静候光阴荏苒。",
                            "遵循 Local-First（本地優先）設計哲學。零賬號體系、零雲端依賴、零冗餘後台。內置百年離線公農曆算法，純黑 OLED 留白畫報美學，靜候光陰荏苒。"
                        ),
                        fontSize = 12.sp,
                        color = FengShiTheme.colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: 数据备份与迁移
                Text(
                    text = i18n("BACKUP & TRANSFER 数据迁移", "BACKUP & TRANSFER 數據遷移"),
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 1. JSON 导出与复制
                SettingsActionRow(
                    icon = Icons.Default.FileUpload,
                    title = i18n("导出 JSON 全量备份", "導出 JSON 全量備份"),
                    subtitle = i18n("复制完整数据至剪贴板或分享", "複製完整數據至剪貼板或分享"),
                    onClick = {
                        scope.launch {
                            val json = viewModel.getJsonExport()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("逢时JSON数据备份", json))
                            Toast.makeText(context, if (LanguageManager.isTraditional()) "已複製全量 JSON 備份至剪貼板" else "已复制全量 JSON 备份至剪贴板", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 2. JSON 导入
                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = i18n("导入 JSON 数据", "導入 JSON 數據"),
                    subtitle = i18n("从剪贴板粘贴或文本恢复事件", "從剪貼板粘貼或文本恢復事件"),
                    onClick = { showJsonImportDialog = true }
                )

                // 3. CSV 导出
                SettingsActionRow(
                    icon = Icons.Default.TableChart,
                    title = i18n("导出标准 CSV 模板", "導出標準 CSV 模板"),
                    subtitle = i18n("便于在 Excel/Numbers 中管理", "便於在 Excel/Numbers 中管理"),
                    onClick = {
                        scope.launch {
                            val csv = viewModel.getCsvExport()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("逢时CSV数据", csv))
                            Toast.makeText(context, if (LanguageManager.isTraditional()) "已複製 CSV 文本至剪貼板" else "已复制 CSV 文本至剪贴板", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 4. CSV 导入
                SettingsActionRow(
                    icon = Icons.Default.FileDownload,
                    title = i18n("批量导入 CSV 记录", "批量導入 CSV 記錄"),
                    subtitle = i18n("粘贴标准表格文本完成批量录入", "粘貼標準表格文本完成批量錄入"),
                    onClick = { showCsvImportDialog = true }
                )

                // 5. AI 智能提示词模板
                SettingsActionRow(
                    icon = Icons.Default.ContentCopy,
                    title = i18n("AI 识别与导入提示词模板", "AI 識別與導入提示詞模板"),
                    subtitle = i18n("一键复制标准 Prompt 给 Gemini/ChatGPT 整理", "一鍵複製標準 Prompt 給 Gemini/ChatGPT 整理"),
                    onClick = { showAiTemplateDialog = true }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Section: 语言偏好设置 (Language Preference)
                Text(
                    text = i18n("LANGUAGE 语言偏好设置", "LANGUAGE 語言偏好設置"),
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentLanguageMode by viewModel.languageMode.collectAsStateWithLifecycle()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        LanguageManager.LANG_ZH_CN to ("简体中文" to "簡體中文"),
                        LanguageManager.LANG_ZH_TW to ("繁體中文" to "繁體中文"),
                        LanguageManager.LANG_SYSTEM to ("跟随系统" to "跟隨系統")
                    )

                    languages.forEach { (langCode, labelPair) ->
                        val isSelected = currentLanguageMode == langCode
                        val label = if (LanguageManager.isTraditional()) labelPair.second else labelPair.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) FengShiTheme.colors.textPrimary else FengShiTheme.colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) FengShiTheme.colors.textPrimary else FengShiTheme.colors.border,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.setLanguageMode(langCode)
                                }
                                .padding(vertical = 10.dp)
                                .testTag("lang_option_$langCode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FengShiTheme.colors.background else FengShiTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section: 主题外观选择 (Theme & Aesthetic)
                Text(
                    text = i18n("THEME & AESTHETIC 外观风格", "THEME & AESTHETIC 外觀風格"),
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        ThemeManager.THEME_SYSTEM to ("系统跟随" to "系統跟隨"),
                        ThemeManager.THEME_DARK to ("玄曜纯黑" to "玄曜純黑"),
                        ThemeManager.THEME_LIGHT to ("宣纸水墨" to "宣紙水墨")
                    )
                    themes.forEach { (mode, labelPair) ->
                        val isSelected = currentThemeMode == mode
                        val label = if (LanguageManager.isTraditional()) labelPair.second else labelPair.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) FengShiTheme.colors.textPrimary else FengShiTheme.colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) FengShiTheme.colors.textPrimary else FengShiTheme.colors.border,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(vertical = 10.dp)
                                .testTag("theme_option_$mode"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FengShiTheme.colors.background else FengShiTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section: 缓存与指引
                Text(
                    text = i18n("系统与维护", "系統與維護"),
                    fontSize = 13.sp,
                    color = FengShiTheme.colors.accentGold,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsActionRow(
                    icon = Icons.Default.Edit,
                    title = i18n("自定义分类标签", "自定義分類標籤"),
                    subtitle = i18n("管理节日、生日、纪念日、长远目标等标签", "管理節日、生日、紀念日、長遠目標等標籤"),
                    onClick = { showCategoryDialog = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.Delete,
                    title = i18n("一键删除预置示例项目", "一鍵刪除預置示例項目"),
                    subtitle = i18n("清除系统初始演示吉日，保留自建项目", "清除系統初始演示吉日，保留自建項目"),
                    isWarning = true,
                    onClick = { showDeleteSampleConfirm = true }
                )

                SettingsActionRow(
                    icon = Icons.Default.CleaningServices,
                    title = i18n("清理本地生成卡片缓存", "清理本地生成卡片緩存"),
                    subtitle = i18n("清理离屏渲染分享卡片临时文件", "清理離屏渲染分享卡片臨時文件"),
                    onClick = {
                        val cacheDir = File(context.cacheDir, "cards")
                        if (cacheDir.exists()) {
                            cacheDir.deleteRecursively()
                        }
                        Toast.makeText(context, if (LanguageManager.isTraditional()) "已清理本地圖片緩存" else "已清理本地图片缓存", Toast.LENGTH_SHORT).show()
                    }
                )

                SettingsActionRow(
                    icon = Icons.Default.RestartAlt,
                    title = i18n("重温新手向导", "重溫新手嚮導"),
                    subtitle = i18n("查看公农历与小组件使用指引", "查看公農曆與小組件使用指引"),
                    onClick = {
                        onDismiss()
                        onReplayOnboarding()
                    }
                )

                SettingsActionRow(
                    icon = Icons.Default.Info,
                    title = i18n("清空所有本地数据", "清空所有本地數據"),
                    subtitle = i18n("重置数据库至初态", "重置數據庫至初態"),
                    isWarning = true,
                    onClick = { showClearDataConfirm = true }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Section: 作者与联系
                Text(
                    text = i18n("设计师与开发者", "設計師與開發者"),
                    fontSize = 13.sp,
                    color = FengShiTheme.colors.accentGold,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Email Contact Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:xushuai662@Gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "【逢时 App 用户反馈与建议】")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("开发者邮箱", "xushuai662@Gmail.com"))
                                Toast.makeText(context, "已复制邮箱至剪贴板：xushuai662@Gmail.com", Toast.LENGTH_LONG).show()
                            }
                        }
                        .padding(14.dp)
                        .testTag("contact_author_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = i18n("联系作者", "聯繫作者"),
                        tint = FengShiTheme.colors.accentGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = i18n("联系开发者反馈", "聯繫開發者反饋"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = FengShiTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "xushuai662@Gmail.com",
                            fontSize = 12.sp,
                            color = FengShiTheme.colors.accentGold
                        )
                    }
                    Text(
                        text = i18n("点击发信", "點擊發信"),
                        fontSize = 12.sp,
                        color = FengShiTheme.colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "版本 1.0.0 · Local-First Pure Native",
                    fontSize = 11.sp,
                    color = FengShiTheme.colors.textMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    // JSON 导入弹窗
    if (showJsonImportDialog) {
        ImportTextDialog(
            title = "从 JSON 导入数据",
            placeholder = "在此粘贴标准 JSON 备份内容...",
            onDismiss = { showJsonImportDialog = false },
            onConfirm = { text ->
                viewModel.importFromJson(
                    text,
                    onSuccess = { count ->
                        Toast.makeText(context, "成功导入 $count 条吉日记录", Toast.LENGTH_SHORT).show()
                        showJsonImportDialog = false
                    },
                    onError = { msg ->
                        Toast.makeText(context, "导入失败: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // CSV 导入弹窗
    if (showCsvImportDialog) {
        ImportTextDialog(
            title = "批量导入 CSV 记录",
            placeholder = "在此粘贴 CSV 文本内容 (第一行为表头)...\n示例: 立春,2026-02-04,公历,倒数,节日,是,DAY_OF,万物复苏",
            onDismiss = { showCsvImportDialog = false },
            onConfirm = { text ->
                viewModel.importFromCsv(
                    text,
                    onSuccess = { count ->
                        Toast.makeText(context, "成功批量解析导入 $count 条记录", Toast.LENGTH_SHORT).show()
                        showCsvImportDialog = false
                    },
                    onError = { msg ->
                        Toast.makeText(context, "导入失败: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // 清空数据二次确认
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            containerColor = FengShiTheme.colors.surface,
            title = { Text(i18n("确认清空所有数据？", "確認清空所有數據？"), color = FengShiTheme.colors.accentVermillion, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
            text = { Text(i18n("此操作将清空本地所有吉日与纪念日记录，且不可撤销。", "此操作將清空本地所有吉日與紀念日記錄，且不可撤銷。"), color = FengShiTheme.colors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData {
                            Toast.makeText(context, tr("所有本地数据已清空", "所有本地數據已清空"), Toast.LENGTH_SHORT).show()
                            showClearDataConfirm = false
                        }
                    }
                ) {
                    Text(i18n("确认清空", "確認清空"), color = FengShiTheme.colors.accentVermillion, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text(i18n("取消", "取消"), color = FengShiTheme.colors.textMuted)
                }
            }
        )
    }

    // 一键删除预置示例二次确认
    if (showDeleteSampleConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteSampleConfirm = false },
            containerColor = FengShiTheme.colors.surface,
            title = { Text(i18n("确认一键删除预置示例？", "確認一鍵刪除預置示例？"), color = FengShiTheme.colors.accentVermillion, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
            text = { Text(i18n("将一键清除「春风如期 / 知己生辰 / 相识相守」3 个系统初始示例。您自己录入的事件将不受任何影响。", "將一鍵清除「春風如期 / 知己生辰 / 相識相守」3 個系統初始示例。您自己錄入的事件將不受任何影響。"), color = FengShiTheme.colors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSampleEvents {
                            Toast.makeText(context, tr("已一键清空预置示例，仅保留自建项目", "已一鍵清空預置示例，僅保留自建項目"), Toast.LENGTH_SHORT).show()
                            showDeleteSampleConfirm = false
                        }
                    }
                ) {
                    Text(i18n("一键删除", "一鍵刪除"), color = FengShiTheme.colors.accentVermillion, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSampleConfirm = false }) {
                    Text(i18n("取消", "取消"), color = FengShiTheme.colors.textMuted)
                }
            }
        )
    }

    // 分类管理对话框
    if (showCategoryDialog) {
        CategoryManagementDialog(
            viewModel = viewModel,
            onDismiss = { showCategoryDialog = false }
        )
    }

    // AI 识别与导入提示词模板弹窗
    if (showAiTemplateDialog) {
        AiTemplateDialog(onDismiss = { showAiTemplateDialog = false })
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isWarning: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isWarning) FengShiTheme.colors.accentVermillion else FengShiTheme.colors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isWarning) FengShiTheme.colors.accentVermillion else FengShiTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = FengShiTheme.colors.textMuted
            )
        }
    }
}

@Composable
fun ImportTextDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp)),
            color = FengShiTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = FengShiTheme.colors.textPrimary)
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                text = clip.getItemAt(0).text?.toString() ?: ""
                            }
                        }
                    ) {
                        Text(i18n("从剪贴板粘贴", "從剪貼板粘貼"), fontSize = 12.sp, color = FengShiTheme.colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder, color = FengShiTheme.colors.textMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FengShiTheme.colors.textPrimary,
                        unfocusedBorderColor = FengShiTheme.colors.border,
                        focusedTextColor = FengShiTheme.colors.textPrimary,
                        unfocusedTextColor = FengShiTheme.colors.textPrimary,
                        cursorColor = FengShiTheme.colors.textPrimary,
                        focusedContainerColor = FengShiTheme.colors.surfaceVariant,
                        unfocusedContainerColor = FengShiTheme.colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(i18n("取消", "取消"), color = FengShiTheme.colors.textMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onConfirm(text) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(i18n("开始解析导入", "開始解析導入"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * AI 智能识别与导入标准规范提示词模板 (可直接复制发给 Gemini / ChatGPT)
 */
@Composable
fun AiTemplateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: JSON, 1: CSV

    val jsonPrompt = remember {
        """
请帮我将以下生活中记录的吉日、纪念日、生日和倒数事件，整理并直接输出为符合「逢时」App 标准的 JSON 数组格式（不要包含任何 markdown 代码块外部的多余废话，直接输出 JSON 数组）：

[
  {
    "title": "母亲生日",
    "dateString": "1972-05-18",
    "isLunar": true,
    "calendarType": "LUNAR",
    "celebrateBy": "LUNAR",
    "repeatRule": "ANNUAL",
    "eventType": "BIRTHDAY",
    "category": "生日",
    "categoryColorHex": "#D4AF37",
    "isPinned": true,
    "notes": "按农历每年庆祝"
  },
  {
    "title": "结婚纪念日",
    "dateString": "2020-10-01",
    "isLunar": false,
    "calendarType": "SOLAR",
    "celebrateBy": "SOLAR",
    "repeatRule": "ANNUAL",
    "eventType": "ANNIVERSARY",
    "category": "纪念日",
    "categoryColorHex": "#C84B31",
    "isPinned": false,
    "notes": "公历庆祝，岁月如歌"
  }
]

【规范字段定义与说明】：
1. title: 事件名称 (如 "爸爸生辰", "相识纪念日")
2. dateString: 日期格式统一为 "YYYY-MM-DD"（公历或农历的年月日数字，如 1995-08-15）
3. isLunar: 农历填 true，公历填 false
4. calendarType: "LUNAR" (农历) 或 "SOLAR" (公历)
5. celebrateBy: "LUNAR" (依农历循环过生日/节日) 或 "SOLAR" (依公历过)
6. repeatRule: "ANNUAL" (每年重复倒数) 或 "NONE" (单次/累计)
7. eventType: 
   - "BIRTHDAY" (生辰岁数推算)
   - "ANNIVERSARY" (纪念日已逝天数累计)
   - "COUNTDOWN" (重要节点未来倒数)
   - "TARGET" (长远规划目标吉日)
8. category: "节日", "生日", "纪念日", "长远目标", "工作" 等分类名
9. isPinned: 是否置顶 (true / false)
10. notes: 备忘寄语 (可选)

请将以下我的事件整理输出为上述 JSON：
（在此处粘贴你的原始文字，如：我爸1968年农历十月十五生辰、我和女朋友2021年5月20日在一起等）
        """.trimIndent()
    }

    val csvPrompt = remember {
        """
请帮我将以下纪念日、生日和吉日信息，整理并输出为符合「逢时」App 导入规范的 CSV 纯文本（第一行为标准表头，每行一条）：

标题,日期,历法,事件类型,分类,是否置顶,提醒模式,备忘
母亲生辰,1972-05-18,农历,生日,生日,是,DAY_OF,备酒庆贺
结婚纪念日,2020-10-01,公历,纪念日,纪念日,是,BEFORE_1,岁月沉香
立春吉日,2026-02-04,公历,倒数,节日,否,NONE,万物生长

【参数取值规范】：
- 历法：公历 或 农历
- 事件类型：生日 / 纪念日 / 倒数 / 目标
- 是否置顶：是 或 否
- 提醒模式：NONE(不提醒) / DAY_OF(当天) / BEFORE_1(提前1天) / BEFORE_3(提前3天) / BEFORE_7(提前7天)

请将以下我的事件转换为上述 CSV 文本：
（在此处粘贴你的原始文字）
        """.trimIndent()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("ai_template_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI PROMPT TEMPLATES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = i18n("AI 识别与导入模板", "AI 識別與導入模板"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = i18n("关闭", "關閉"), tint = FengShiTheme.colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = i18n("无需手动一条条录入。直接一键复制下方完整提示词发给 Gemini、ChatGPT 或 Claude，让 AI 自动识别您的零散日期并格式化，生成后直接在「导入」中粘贴即可完成全量录入！", "無需手動一條條錄入。直接一鍵複製下方完整提示詞發給 Gemini、ChatGPT 或 Claude，讓 AI 自動識別您的零散日期並格式化，生成後直接在「導入」中粘貼即可完成全量錄入！"),
                    fontSize = 12.sp,
                    color = FengShiTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Format Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(i18n("JSON 规范提示词", "JSON 規範提示詞"), i18n("CSV 规范提示词", "CSV 規範提示詞"))
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) (if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary) else FengShiTheme.colors.surfaceVariant)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) (if (FengShiTheme.colors.isDark) Color.Black else Color.White) else FengShiTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val currentContent = if (selectedTab == 0) jsonPrompt else csvPrompt

                // Code Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FengShiTheme.colors.surfaceVariant)
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentContent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = FengShiTheme.colors.textPrimary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FengShiTheme.colors.textPrimary)
                    ) {
                        Text(i18n("返回", "返回"))
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AI导入规范提示词", currentContent))
                            Toast.makeText(context, tr("已复制完整 AI 提示词与规范模板！可直接发给 AI 助手", "已複製完整 AI 提示詞與規範模板！可直接發給 AI 助手"), Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(i18n("一键复制提示词", "一鍵複製提示詞"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
