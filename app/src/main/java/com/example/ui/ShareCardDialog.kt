package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.calendar.LunarCalendar
import com.example.calendar.PoetryProvider
import com.example.data.EventEntity
import com.example.i18n.i18n
import com.example.i18n.tr
import com.example.ui.theme.FengShiTheme
import com.example.ui.components.FengShiCapsuleButton
import com.example.ui.components.FengShiPrimaryButton
import com.example.ui.components.FengShiSecondaryButton
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EditorialVermillion
import com.example.ui.theme.OledBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@Composable
fun ShareCardDialog(
    event: EventEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isNineBySixteen by remember { mutableStateOf(true) } // true: 9:16, false: 1:1
    var selectedPoem by remember {
        mutableStateOf(PoetryProvider.getRandomPoem(event.eventType))
    }
    var customBgUri by remember { mutableStateOf<Uri?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customBgUri = uri
        }
    }

    val today = remember { LocalDate.now() }
    val days = remember(event) { event.computeDays(today) }
    val statusDesc = remember(event) { event.getStatusDescription(today) }
    val lunar = remember(event) {
        val orig = try { LocalDate.parse(event.dateString) } catch (e: Exception) { today }
        LunarCalendar.solarToLunar(orig)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                .testTag("share_card_dialog"),
            color = FengShiTheme.colors.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EDITORIAL CARD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            color = FengShiTheme.colors.textMuted
                        )
                        Text(
                            text = i18n("诗意画报卡片", "詩意畫報卡片"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = FengShiTheme.colors.textPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_share_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("关闭", "關閉"),
                            tint = FengShiTheme.colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Preview Container (自适应 9:16 与 1:1 容器尺寸，确保底部安全内边距)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isNineBySixteen) 0.92f else 0.88f)
                        .aspectRatio(if (isNineBySixteen) 9f / 16f else 1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .testTag("card_preview_container")
                ) {
                    // Custom photo background if selected
                    if (customBgUri != null) {
                        AsyncImage(
                            model = customBgUri,
                            contentDescription = "背景图",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Dark gradient overlay for high contrast readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xCC000000),
                                            Color(0x99000000),
                                            Color(0xF0000000)
                                        )
                                    )
                                )
                        )
                    }

                    // Card Content (自适应安全高度内边距，彻底阻断 1:1 底部截断)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = if (isNineBySixteen) 22.dp else 16.dp,
                                vertical = if (isNineBySixteen) 20.dp else 12.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Brand / Lunar Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "FENG SHI",
                                    fontSize = 8.sp,
                                    letterSpacing = 2.sp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "逢时",
                                    fontSize = if (isNineBySixteen) 15.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${lunar.cyclicalYear} · ${lunar.zodiac}年",
                                fontSize = if (isNineBySixteen) 11.sp else 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        // Center Block: Days + Target
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = event.title,
                                fontSize = if (isNineBySixteen) 22.sp else 16.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = statusDesc,
                                fontSize = if (isNineBySixteen) 12.sp else 10.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(if (isNineBySixteen) 16.dp else 4.dp))

                            // Large Editorial Numeral
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = days.toString(),
                                    fontSize = if (isNineBySixteen) 64.sp else 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif,
                                    letterSpacing = if (isNineBySixteen) (-3).sp else (-1).sp,
                                    lineHeight = if (isNineBySixteen) 64.sp else 38.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.padding(bottom = if (isNineBySixteen) 6.dp else 2.dp)) {
                                    Text(
                                        text = "DAYS",
                                        fontSize = if (isNineBySixteen) 10.sp else 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = if (event.eventType == EventEntity.TYPE_ANNIVERSARY) "PAST" else "LEFT",
                                        fontSize = if (isNineBySixteen) 10.sp else 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = event.getDisplayDateText(),
                                fontSize = if (isNineBySixteen) 11.sp else 9.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }

                        // Bottom Block: Classical Poetry (完全自适应字号与上下安全留白，严禁截断)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (isNineBySixteen) 2.dp else 4.dp)
                        ) {
                            Text(
                                text = "“${selectedPoem.content}”",
                                fontSize = if (isNineBySixteen) 13.sp else 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                lineHeight = if (isNineBySixteen) 20.sp else 15.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "—— ${selectedPoem.source}",
                                fontSize = if (isNineBySixteen) 11.sp else 10.sp,
                                color = EditorialVermillion,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customization Controls
                // 1. Ratio switch (使用规范化胶囊按钮)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = i18n("RATIO 比例", "RATIO 比例"),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = FengShiTheme.colors.textMuted
                    )
                    Row(
                        modifier = Modifier
                            .background(FengShiTheme.colors.surfaceVariant, RoundedCornerShape(15.dp))
                            .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(15.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FengShiCapsuleButton(
                            selected = isNineBySixteen,
                            onClick = { isNineBySixteen = true }
                        ) {
                            Text(
                                text = i18n("9:16 故事", "9:16 故事"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        FengShiCapsuleButton(
                            selected = !isNineBySixteen,
                            onClick = { isNineBySixteen = false }
                        ) {
                            Text(
                                text = i18n("1:1 方形", "1:1 方形"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Poetry & Background controls (使用规范化 38dp Secondary Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FengShiSecondaryButton(
                        onClick = {
                            selectedPoem = PoetryProvider.getRandomPoem(event.eventType)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = i18n("换诗句", "換詩句"),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = i18n("换一句诗", "換一句詩"), fontSize = 12.sp)
                    }

                    FengShiSecondaryButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = i18n("选壁纸", "選壁紙"),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (customBgUri == null) i18n("相册背景", "相冊背景") else i18n("重选背景", "重選背景"),
                            fontSize = 12.sp
                        )
                    }

                    if (customBgUri != null) {
                        IconButton(
                            onClick = { customBgUri = null },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FengShiTheme.colors.surfaceVariant)
                                .border(1.dp, FengShiTheme.colors.border, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = i18n("清除背景", "清除背景"),
                                tint = FengShiTheme.colors.accentVermillion,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // One-click Lossless Render and Share Button (使用规范化 48dp Primary Button)
                FengShiPrimaryButton(
                    onClick = {
                        if (isGenerating) return@FengShiPrimaryButton
                        isGenerating = true
                        scope.launch {
                            val uri = renderAndSaveCardBitmap(
                                context = context,
                                event = event,
                                days = days,
                                statusDesc = statusDesc,
                                poem = selectedPoem,
                                isNineBySixteen = isNineBySixteen,
                                bgUri = customBgUri
                            )
                            isGenerating = false
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "分享「逢时」吉日卡片")
                                )
                            } else {
                                Toast.makeText(context, "卡片生成失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("render_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGenerating) i18n("正在无损渲染...", "正在無損渲染...") else i18n("生成并分享精美卡片", "生成並分享精美卡片"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 离屏高清无损渲染生成卡片 Bitmap (精确匹配弹窗预览流式排版，彻底杜绝文字重叠与视觉脱节)
 */
suspend fun renderAndSaveCardBitmap(
    context: Context,
    event: EventEntity,
    days: Int,
    statusDesc: String,
    poem: PoetryProvider.Poem,
    isNineBySixteen: Boolean,
    bgUri: Uri?
): Uri? = withContext(Dispatchers.IO) {
    try {
        val width = 1080
        val height = if (isNineBySixteen) 1920 else 1080

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 纯黑背景
        canvas.drawColor(android.graphics.Color.BLACK)

        // 用户背景图绘制
        if (bgUri != null) {
            try {
                context.contentResolver.openInputStream(bgUri)?.use { stream ->
                    val bgBmp = BitmapFactory.decodeStream(stream)
                    if (bgBmp != null) {
                        val srcRect = Rect(0, 0, bgBmp.width, bgBmp.height)
                        val dstRect = Rect(0, 0, width, height)
                        canvas.drawBitmap(bgBmp, srcRect, dstRect, null)

                        val overlayPaint = Paint().apply {
                            shader = LinearGradient(
                                0f, 0f, 0f, height.toFloat(),
                                intArrayOf(0xDD000000.toInt(), 0x99000000.toInt(), 0xF0000000.toInt()),
                                floatArrayOf(0f, 0.5f, 1f),
                                Shader.TileMode.CLAMP
                            )
                        }
                        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
                    }
                }
            } catch (e: Exception) {
                // Ignore bg read error
            }
        }

        // 外围与内衬边距
        val margin = if (isNineBySixteen) 56f else 40f
        val paddingHorizontal = if (isNineBySixteen) 80f else 64f
        val textWidth = (width - paddingHorizontal * 2f).toInt()

        // 东方雅致边框线 (对应预览的 RoundedCornerShape(16.dp))
        val borderPaint = Paint().apply {
            color = 0xFF2E2C28.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(margin, margin, width - margin, height - margin),
            32f, 32f, borderPaint
        )

        val orig = try { LocalDate.parse(event.dateString) } catch (e: Exception) { LocalDate.now() }
        val lunar = LunarCalendar.solarToLunar(orig)

        // ==================== 1. 顶部 Header (BRAND + LUNAR TAG) ====================
        val headerTopY = if (isNineBySixteen) margin + 60f else margin + 40f
        
        // 左侧英文副标
        val enBrandPaint = Paint().apply {
            color = 0x66FFFFFF.toInt()
            textSize = if (isNineBySixteen) 22f else 18f
            letterSpacing = 0.2f
            isAntiAlias = true
        }
        canvas.drawText("FENG SHI", paddingHorizontal, headerTopY, enBrandPaint)

        // 左侧主标「逢时」
        val zhBrandPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = if (isNineBySixteen) 42f else 34f
            isFakeBoldText = true
            isAntiAlias = true
            typeface = Typeface.SERIF
        }
        val zhBrandBaseline = headerTopY + (if (isNineBySixteen) 48f else 38f)
        canvas.drawText("逢时", paddingHorizontal, zhBrandBaseline, zhBrandPaint)

        // 右侧农历标签
        val lunarTag = "${lunar.cyclicalYear} · ${lunar.zodiac}年"
        val lunarPaint = Paint().apply {
            color = 0x99FFFFFF.toInt()
            textSize = if (isNineBySixteen) 28f else 22f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(lunarTag, width - paddingHorizontal, zhBrandBaseline - 4f, lunarPaint)

        // ==================== 2. 底部诗词与来源预先计算高度 ====================
        val poemPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6FFFFFF.toInt()
            textSize = if (isNineBySixteen) 36f else 27f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        val formattedPoem = "“${poem.content}”"
        val poemLayout = android.text.StaticLayout.Builder.obtain(
            formattedPoem, 0, formattedPoem.length, poemPaint, textWidth
        ).setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.3f)
            .setIncludePad(false)
            .build()

        val poemSourcePaint = Paint().apply {
            color = 0xFFC9372E.toInt() // EditorialVermillion 对应朱砂印章色
            textSize = if (isNineBySixteen) 28f else 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
        }
        val poemSourceGap = if (isNineBySixteen) 18f else 12f
        val poemTotalHeight = poemLayout.height + poemSourceGap + poemSourcePaint.textSize

        val bottomPadding = if (isNineBySixteen) margin + 70f else margin + 48f
        val poemStartY = height - bottomPadding - poemTotalHeight

        // ==================== 3. 中间区域流式排版 (严格线性累加，绝不重叠) ====================
        // 中间可用区域高度
        val middleTopBoundary = zhBrandBaseline + (if (isNineBySixteen) 60f else 36f)
        val middleBottomBoundary = poemStartY - (if (isNineBySixteen) 50f else 30f)
        val availableMiddleHeight = middleBottomBoundary - middleTopBoundary

        // 3.1 事件标题 StaticLayout
        val titlePaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = if (isNineBySixteen) 58f else 42f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        if (event.title.length > 12) {
            titlePaint.textSize = if (isNineBySixteen) 46f else 34f
        }
        val titleLayout = android.text.StaticLayout.Builder.obtain(
            event.title, 0, event.title.length, titlePaint, textWidth
        ).setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()

        // 3.2 状态描述与公历日期
        val statusPaint = Paint().apply {
            color = 0x80FFFFFF.toInt()
            textSize = if (isNineBySixteen) 30f else 23f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val statusTextHeight = statusPaint.textSize

        // 3.3 巨大天数及 DAYS LEFT / PAST
        val heroNumPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = if (isNineBySixteen) 160f else 100f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = -0.04f
        }
        val daysString = days.toString()
        val numBounds = Rect()
        heroNumPaint.getTextBounds(daysString, 0, daysString.length, numBounds)
        val numWidth = heroNumPaint.measureText(daysString)
        val numHeight = numBounds.height().toFloat()

        val unitSubPaint = Paint().apply {
            color = 0x66FFFFFF.toInt()
            textSize = if (isNineBySixteen) 24f else 18f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        val unitBottomText = if (event.eventType == EventEntity.TYPE_ANNIVERSARY) "PAST" else "LEFT"

        // 3.4 底部日期
        val dateDisplayPaint = Paint().apply {
            color = 0x66FFFFFF.toInt()
            textSize = if (isNineBySixteen) 26f else 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val dateDisplayText = event.getDisplayDateText()

        // 测算中间内容总高度
        val gapTitleToStatus = if (isNineBySixteen) 18f else 10f
        val gapStatusToHero = if (isNineBySixteen) 48f else 22f
        val gapHeroToDate = if (isNineBySixteen) 32f else 16f

        val middleContentHeight = titleLayout.height + gapTitleToStatus + statusTextHeight +
                gapStatusToHero + numHeight + gapHeroToDate + dateDisplayPaint.textSize

        // 垂直居中起始点 (居中对齐中区空间)
        var cursorY = middleTopBoundary + ((availableMiddleHeight - middleContentHeight) / 2f).coerceAtLeast(0f)

        // 绘制标题
        canvas.save()
        canvas.translate((width - textWidth) / 2f, cursorY)
        titleLayout.draw(canvas)
        canvas.restore()
        cursorY += titleLayout.height + gapTitleToStatus

        // 绘制状态描述
        val statusBaseline = cursorY + statusTextHeight
        canvas.drawText(statusDesc, width / 2f, statusBaseline, statusPaint)
        cursorY = statusBaseline + gapStatusToHero

        // 绘制巨大天数及紧随其后的 DAYS / LEFT
        val heroBaseline = cursorY + numHeight
        val unitGap = if (isNineBySixteen) 14f else 10f
        val totalHeroBlockWidth = numWidth + unitGap + unitSubPaint.measureText(unitBottomText).coerceAtLeast(unitSubPaint.measureText("DAYS"))
        val heroStartX = (width - totalHeroBlockWidth) / 2f

        canvas.drawText(daysString, heroStartX, heroBaseline, heroNumPaint)

        val unitX = heroStartX + numWidth + unitGap
        val daysLabelBaseline = heroBaseline - (if (isNineBySixteen) 42f else 26f)
        val stateLabelBaseline = heroBaseline - (if (isNineBySixteen) 10f else 6f)
        canvas.drawText("DAYS", unitX, daysLabelBaseline, unitSubPaint)
        canvas.drawText(unitBottomText, unitX, stateLabelBaseline, unitSubPaint)

        cursorY = heroBaseline + gapHeroToDate

        // 绘制日期文本
        val dateBaseline = cursorY + dateDisplayPaint.textSize
        canvas.drawText(dateDisplayText, width / 2f, dateBaseline, dateDisplayPaint)

        // ==================== 4. 绘制底部经典诗词及出处 ====================
        canvas.save()
        canvas.translate((width - textWidth) / 2f, poemStartY)
        poemLayout.draw(canvas)
        canvas.restore()

        val sourceBaseline = poemStartY + poemLayout.height + poemSourceGap + poemSourcePaint.textSize
        canvas.drawText("—— ${poem.source}", width / 2f, sourceBaseline, poemSourcePaint)

        // 保存至 Cache 并通过 FileProvider 提供 Uri
        val cacheDir = File(context.cacheDir, "cards").apply { mkdirs() }
        val cardFile = File(cacheDir, "fengshi_card_${System.currentTimeMillis()}.png")
        FileOutputStream(cardFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cardFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
