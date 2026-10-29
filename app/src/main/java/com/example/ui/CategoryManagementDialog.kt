package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EventEntity
import com.example.i18n.i18n
import com.example.ui.theme.EditorialVermillion
import com.example.ui.theme.FengShiTheme

/**
 * 分类标签管理弹窗 - 支持自定义修改、增添、重命名与删除 "节日/生日/纪念日" 等分类
 */
@Composable
fun CategoryManagementDialog(
    viewModel: EventViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.userCategories.collectAsStateWithLifecycle()
    val allEvents by viewModel.allEventsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var newCategoryText by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("category_management_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CATEGORY SETTINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = i18n("分类标签管理", "分類標籤管理"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_category_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = i18n(
                        "自由添加或更改节日、生日、纪念日等标签，修改后历史吉日将自动同步更新。",
                        "自由添加或更改節日、生日、紀念日等標籤，修改後歷史吉日將自動同步更新。"
                    ),
                    fontSize = 12.sp,
                    color = FengShiTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input new category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        placeholder = {
                            Text(
                                text = i18n("新分类 (如：还款、考研、旅行)...", "新分類 (如：還款、考研、旅行)..."),
                                color = FengShiTheme.colors.textMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_category_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FengShiTheme.colors.accentGold,
                            unfocusedBorderColor = FengShiTheme.colors.border,
                            focusedTextColor = FengShiTheme.colors.textPrimary,
                            unfocusedTextColor = FengShiTheme.colors.textPrimary,
                            cursorColor = FengShiTheme.colors.accentGold,
                            focusedContainerColor = FengShiTheme.colors.surfaceVariant,
                            unfocusedContainerColor = FengShiTheme.colors.surfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val trimmed = newCategoryText.trim()
                            if (trimmed.isNotBlank()) {
                                if (viewModel.addCategory(trimmed)) {
                                    newCategoryText = ""
                                    Toast.makeText(context, if (com.example.i18n.LanguageManager.isTraditional()) "已新增「$trimmed」分類" else "已新增「$trimmed」分类", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, if (com.example.i18n.LanguageManager.isTraditional()) "分類已存在或名稱無效" else "分类已存在或名称无效", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("add_category_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = i18n("添录", "添錄"),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = i18n("添录", "添錄"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of categories
                Text(
                    text = "${i18n("EXISTING CATEGORIES 当前分类", "EXISTING CATEGORIES 當前分類")} (${categories.size})",
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = FengShiTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(240.dp)
                ) {
                    items(categories, key = { it }) { cat ->
                        val count = allEvents.count { it.category == cat }
                        val isEditingThis = editingCategory == cat

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FengShiTheme.colors.surfaceVariant)
                                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (isEditingThis) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = renameText,
                                        onValueChange = { renameText = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FengShiTheme.colors.accentGold,
                                            unfocusedBorderColor = FengShiTheme.colors.border,
                                            focusedTextColor = FengShiTheme.colors.textPrimary,
                                            unfocusedTextColor = FengShiTheme.colors.textPrimary,
                                            cursorColor = FengShiTheme.colors.accentGold,
                                            focusedContainerColor = FengShiTheme.colors.surface,
                                            unfocusedContainerColor = FengShiTheme.colors.surface
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (renameText.isNotBlank() && renameText.trim() != cat) {
                                                viewModel.renameCategory(cat, renameText.trim())
                                                Toast.makeText(context, if (com.example.i18n.LanguageManager.isTraditional()) "已重命名為「${renameText.trim()}」" else "已重命名为「${renameText.trim()}」", Toast.LENGTH_SHORT).show()
                                            }
                                            editingCategory = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = i18n("保存", "保存"),
                                            tint = FengShiTheme.colors.textPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { editingCategory = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = i18n("取消", "取消"),
                                            tint = FengShiTheme.colors.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = FengShiTheme.colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(FengShiTheme.colors.tagBg, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$count ${i18n("件", "件")}",
                                                fontSize = 11.sp,
                                                color = FengShiTheme.colors.tagText
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                editingCategory = cat
                                                renameText = cat
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = i18n("重命名", "重命名"),
                                                tint = FengShiTheme.colors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { categoryToDelete = cat },
                                            enabled = categories.size > 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = i18n("删除", "刪除"),
                                                tint = if (categories.size > 1) FengShiTheme.colors.accentVermillion else FengShiTheme.colors.textMuted.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom actions: Reset & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.resetCategories()
                            Toast.makeText(context, if (com.example.i18n.LanguageManager.isTraditional()) "已恢復初始默認分類" else "已恢复初始默认分类", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = i18n("恢复初始", "恢復初始"),
                            tint = FengShiTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = i18n("重置为默认", "重置為默認"),
                            fontSize = 12.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (FengShiTheme.colors.isDark) Color.White else FengShiTheme.colors.textPrimary,
                            contentColor = if (FengShiTheme.colors.isDark) Color.Black else Color.White
                        )
                    ) {
                        Text(text = i18n("完成", "完成"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (categoryToDelete != null) {
        val targetCat = categoryToDelete!!
        val count = allEvents.count { it.category == targetCat }
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = FengShiTheme.colors.surface,
            title = {
                Text(
                    text = i18n("确认删除分类「$targetCat」？", "確認刪除分類「$targetCat」？"),
                    color = FengShiTheme.colors.accentVermillion,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            },
            text = {
                Text(
                    text = if (count > 0) {
                        i18n(
                            "当前有 $count 件吉日正在使用此标签。删除后该分类标签将被移除，相关吉日不会被删除。",
                            "當前有 $count 件吉日正在使用此標籤。刪除後該分類標籤將被移除，相關吉日不會被刪除。"
                        )
                    } else {
                        i18n("确定从分类标签库中移除「$targetCat」吗？", "確定從分類標籤庫中移除「$targetCat」嗎？")
                    },
                    color = FengShiTheme.colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(targetCat)
                        Toast.makeText(context, if (com.example.i18n.LanguageManager.isTraditional()) "已刪除「$targetCat」分類" else "已删除「$targetCat」分类", Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                    }
                ) {
                    Text(i18n("删除", "刪除"), color = FengShiTheme.colors.accentVermillion, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text(i18n("取消", "取消"), color = FengShiTheme.colors.textSecondary)
                }
            }
        )
    }
}
