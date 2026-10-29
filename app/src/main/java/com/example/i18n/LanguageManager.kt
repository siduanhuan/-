package com.example.i18n

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 逢时 - 多语言与简繁体即时切换中心 (Local-First i18n Engine)
 * 支持 简体中文 (ZH_CN)、繁體中文 (ZH_TW) 与 跟随系统 (SYSTEM)。
 * 切换时全局即时响应，无缝重绘，无需用户重启 App。
 */
object LanguageManager {

    const val LANG_SYSTEM = "SYSTEM"
    const val LANG_ZH_CN = "ZH_CN"
    const val LANG_ZH_TW = "ZH_TW"

    private const val PREFS_NAME = "fengshi_prefs"
    private const val KEY_LANG_PREF = "key_language_pref"

    private val _currentLanguage = MutableStateFlow(LANG_SYSTEM)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANG_PREF, LANG_SYSTEM) ?: LANG_SYSTEM
        _currentLanguage.value = savedLang
        initialized = true
    }

    fun setLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG_PREF, lang).apply()
        _currentLanguage.value = lang
    }

    fun isTraditional(context: Context? = null): Boolean {
        return when (_currentLanguage.value) {
            LANG_ZH_TW -> true
            LANG_ZH_CN -> false
            else -> {
                val locale = if (context != null) {
                    context.resources.configuration.locales[0]
                } else {
                    Locale.getDefault()
                }
                val lang = locale.language.lowercase()
                val script = locale.script.lowercase()
                val country = locale.country.uppercase()
                script == "hant" || country in listOf("TW", "HK", "MO")
            }
        }
    }

    fun getLocale(lang: String = _currentLanguage.value): Locale {
        return when (lang) {
            LANG_ZH_CN -> Locale.SIMPLIFIED_CHINESE
            LANG_ZH_TW -> Locale.TRADITIONAL_CHINESE
            else -> Locale.getDefault()
        }
    }

    fun applyLocaleToContext(context: Context, lang: String = _currentLanguage.value): Context {
        val locale = getLocale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    // 核心词汇映射表 (常用界面词汇与历法词汇)
    private val PHRASE_MAP = mapOf(
        "逢时" to "逢時",
        "岁时吉历" to "歲時吉曆",
        "极简东方岁时吉日与倒数纪念" to "極簡東方歲時吉日與倒數紀念",
        "极简东方吉日记录" to "極簡東方吉日記錄",
        "遵循 Local-First（本地优先）设计哲学。零账号体系、零云端依赖、零冗余后台。内置百年离线公农历算法，纯黑 OLED 留白画报美学，静候光阴荏苒。" to
            "遵循 Local-First（本地優先）設計哲學。零帳號體系、零雲端依賴、零冗餘後台。內置百年離線公農曆算法，純黑 OLED 留白畫報美學，靜候光陰荏苒。",
        "全部" to "全部",
        "置顶" to "置頂",
        "农历" to "農曆",
        "公历" to "公曆",
        "国历" to "國曆",
        "天将至" to "天將至",
        "天已过" to "天已過",
        "天已逝" to "天已逝",
        "今日" to "今日",
        "今天" to "今天",
        "天后" to "天後",
        "已过" to "已過",
        "岁" to "歲",
        "周年" to "週年",
        "生辰" to "生辰",
        "生日" to "生日",
        "节日" to "節日",
        "纪念日" to "紀念日",
        "目标" to "目標",
        "生活" to "生活",
        "亲友" to "親友",
        "工作" to "工作",
        "学业" to "學業",
        "健康" to "健康",
        "财务" to "財務",
        "添加吉日" to "添錄吉日",
        "添录吉日" to "添錄吉日",
        "添录新佳期" to "添錄新佳期",
        "添录第一条佳期" to "添錄第一條佳期",
        "修改吉日" to "修改吉日",
        "编辑吉日" to "編輯吉日",
        "删除吉日" to "刪除吉日",
        "批量管理" to "批量管理",
        "批量删除" to "批量刪除",
        "全选" to "全選",
        "取消全选" to "取消全選",
        "退出多选" to "退出多選",
        "已选中" to "已選中",
        "已选择" to "已選擇",
        "设置与数据备份" to "設置與數據備份",
        "语言偏好设置" to "語言偏好設置",
        "语言设置" to "語言設置",
        "外观风格" to "外觀風格",
        "系统跟随" to "系統跟隨",
        "玄曜纯黑" to "玄曜純黑",
        "宣纸水墨" to "宣紙水墨",
        "简体中文" to "簡體中文",
        "繁體中文" to "繁體中文",
        "跟随系统" to "跟隨系統",
        "标记色彩" to "標記色彩",
        "分类标签" to "分類標籤",
        "新分类" to "新分類",
        "管理分类" to "管理分類",
        "事件标题" to "事件標題",
        "倒数日" to "倒數日",
        "累计天数" to "累計天數",
        "单次目标" to "單次目標",
        "每年重复" to "每年重複",
        "确认" to "確認",
        "取消" to "取消",
        "保存" to "保存",
        "完成" to "完成",
        "删除" to "刪除",
        "搜索" to "搜尋",
        "搜索吉日、节气、分类标签..." to "搜尋吉日、節氣、分類標籤...",
        "搜索吉日、纪念日、分类或备忘..." to "搜尋吉日、紀念日、分類或備忘...",
        "更多选项" to "更多選項",
        "收起选项" to "收起選項",
        "导出 JSON 全量备份" to "導出 JSON 全量備份",
        "导入 JSON 数据" to "導入 JSON 數據",
        "导出标准 CSV 模板" to "導出標準 CSV 範本",
        "批量导入 CSV 记录" to "批量導入 CSV 記錄",
        "AI 识别与导入提示词模板" to "AI 識別與導入提示詞範本",
        "自定义分类标签" to "自訂分類標籤",
        "一键删除预置示例项目" to "一鍵刪除預置範例項目",
        "一键清空示例" to "一鍵清空示例",
        "清理本地生成卡片缓存" to "清理本地生成卡片快取",
        "重温新手向导" to "重溫新手引導",
        "清空所有本地数据" to "清空所有本地數據",
        "设计师与开发者" to "設計師與開發者",
        "复制" to "複製",
        "分享" to "分享",
        "分享吉日" to "分享吉日",
        "生成分享长卡片" to "生成分享長卡片",
        "诗意画报卡片" to "詩意畫報卡片",
        "临近天数" to "臨近天數",
        "创建时间" to "創建時間",
        "置顶优先" to "置頂優先",
        "收录" to "收錄",
        "共收录" to "共收錄",
        "件佳期" to "件佳期",
        "项" to "項",
        "正是良辰今日" to "正是良辰今日",
        "距目标吉日还有" to "距目標吉日還有",
        "目标吉日已过去" to "目標吉日已過去",
        "良辰吉日，静候记录" to "良辰吉日，靜候記錄",
        "人生忽如寄，逢时倍相惜。\n点击下方添录生活中的纪念日与生日。" to
            "人生忽如寄，逢時倍相惜。\n點擊下方添錄生活中的紀念日與生日。",
        "岁时如诗 · 吉日相伴" to "歲時如詩 · 吉日相伴",
        "闰" to "閏",
        "腊月" to "臘月",
        "冬月" to "冬月",
        "正月" to "正月",
        "龙" to "龍",
        "马" to "馬",
        "鸡" to "雞",
        "猪" to "豬",
        "惊蛰" to "驚蟄",
        "谷雨" to "穀雨",
        "小满" to "小滿",
        "芒种" to "芒種",
        "处暑" to "處暑"
    )

    // 常用单字简繁映射 (覆盖界面、历法、生肖、干支与经典古诗词常用字)
    private val CHAR_MAP = mapOf(
        '个' to '個', '与' to '與', '么' to '麼', '义' to '義', '乐' to '樂',
        '云' to '雲', '传' to '傳', '伪' to '偽', '体' to '體', '余' to '餘',
        '来' to '來', '两' to '兩', '净' to '淨', '动' to '動', '包' to '包',
        '区' to '區', '历' to '曆', '厌' to '厭', '参' to '參', '双' to '雙',
        '发' to '發', '变' to '變', '后' to '後', '听' to '聽', '启' to '啓',
        '响' to '響', '图' to '圖', '圆' to '圓', '圣' to '聖', '场' to '場',
        '块' to '塊', '声' to '聲', '处' to '處', '复' to '複', '头' to '頭',
        '妇' to '婦', '学' to '學', '实' to '實', '写' to '寫', '对' to '對',
        '导' to '導', '岁' to '歲', '广' to '廣', '庄' to '莊', '应' to '應',
        '开' to '開', '异' to '異', '归' to '歸', '录' to '錄', '总' to '總',
        '恋' to '戀', '恶' to '惡', '惊' to '驚', '战' to '戰', '戏' to '戲',
        '户' to '戶', '执' to '執', '扩' to '擴', '摆' to '擺', '数' to '數',
        '断' to '斷', '旧' to '舊', '时' to '時', '晋' to '晉', '晚' to '晚',
        '显' to '顯', '晓' to '曉', '晕' to '暈', '书' to '書', '术' to '術',
        '机' to '機', '极' to '極', '条' to '條', '标' to '標', '样' to '樣',
        '检' to '檢', '欢' to '歡', '残' to '殘', '气' to '氣', '汉' to '漢',
        '洒' to '灑', '测' to '測', '济' to '濟', '浓' to '濃', '点' to '點',
        '烛' to '燭', '热' to '熱', '爱' to '愛', '状' to '狀', '独' to '獨',
        '现' to '現', '电' to '電', '画' to '畫', '痴' to '癡', '盘' to '盤',
        '码' to '碼', '确' to '確', '礼' to '禮', '神' to '神', '祥' to '祥',
        '禅' to '禪', '离' to '離', '稳' to '穩', '简' to '簡', '管' to '管',
        '筹' to '籌', '类' to '類', '细' to '細', '组' to '組', '终' to '終',
        '经' to '經', '结' to '結', '绘' to '繪', '给' to '給', '绝' to '絕',
        '统' to '統', '续' to '續', '维' to '維', '网' to '網', '缀' to '綴',
        '练' to '練', '县' to '縣', '罢' to '罷', '翻' to '翻', '聪' to '聰',
        '联' to '聯', '胜' to '勝', '脸' to '臉', '临' to '臨', '台' to '臺',
        '致' to '致', '舍' to '舍', '舒' to '舒', '艺' to '藝', '节' to '節',
        '苏' to '蘇', '范' to '範', '药' to '藥', '获' to '獲', '虚' to '虛',
        '规' to '規', '视' to '視', '览' to '覽', '觉' to '覺', '计' to '計',
        '认' to '認', '订' to '訂', '设' to '設', '访' to '訪', '证' to '證',
        '评' to '評', '识' to '識', '诉' to '訴', '词' to '詞', '译' to '譯',
        '试' to '試', '诗' to '詩', '诚' to '誠', '话' to '話', '诞' to '誕',
        '语' to '語', '误' to '誤', '说' to '說', '请' to '請', '诸' to '諸',
        '诺' to '諾', '读' to '讀', '课' to '課', '调' to '調', '谈' to '談',
        '谱' to '譜', '谷' to '穀', '责' to '責', '贤' to '賢', '货' to '貨',
        '质' to '質', '账' to '賬', '购' to '購', '赏' to '賞', '赋' to '賦',
        '赛' to '賽', '赞' to '贊', '赠' to '贈', '赢' to '贏', '赵' to '趙',
        '趋' to '趨', '跃' to '躍', '身' to '身', '车' to '車', '转' to '轉',
        '轮' to '輪', '软' to '軟', '轻' to '輕', '载' to '載', '辉' to '輝',
        '辈' to '輩', '边' to '邊', '达' to '達', '迁' to '遷', '过' to '過',
        '迈' to '邁', '迎' to '迎', '运' to '運', '连' to '連', '进' to '進',
        '远' to '遠', '违' to '違', '迟' to '遲', '选' to '選', '遗' to '遺',
        '释' to '釋', '里' to '裡', '金' to '金', '针' to '針', '钟' to '鐘',
        '钱' to '錢', '铁' to '鐵', '铃' to '鈴', '铅' to '鉛', '银' to '銀',
        '铸' to '鑄', '铺' to '鋪', '链' to '鏈', '锁' to '鎖', '锦' to '錦',
        '镜' to '鏡', '长' to '長', '门' to '門', '闭' to '閉', '问' to '問',
        '闰' to '閏', '闲' to '閑', '间' to '間', '闵' to '閔', '闹' to '鬧',
        '阁' to '閣', '阔' to '闊', '关' to '關', '阳' to '陽', '阴' to '陰',
        '阵' to '陣', '阶' to '階', '际' to '際', '陆' to '陸', '随' to '隨',
        '隐' to '隱', '隔' to '隔', '障' to '障', '难' to '難', '零' to '零',
        '雷' to '雷', '霜' to '霜', '霸' to '霸', '靓' to '靚', '静' to '靜',
        '韵' to '韻', '项' to '項', '顺' to '順', '须' to '須', '预' to '預',
        '题' to '題', '颜' to '顏', '额' to '額', '风' to '風', '飙' to '飆',
        '飞' to '飛', '饰' to '飾', '馆' to '館', '首' to '首', '香' to '香',
        '马' to '馬', '驭' to '馭', '驱' to '驅', '驶' to '駛', '驻' to '駐',
        '验' to '驗', '骑' to '騎', '骚' to '騷', '骤' to '驟', '鲁' to '魯',
        '鲜' to '鮮', '鸟' to '鳥', '鸣' to '鳴', '鸿' to '鴻', '鹤' to '鶴',
        '麦' to '麥', '黄' to '黃', '黑' to '黑', '默' to '默', '齿' to '齒',
        '齐' to '齊', '龙' to '龍', '龟' to '龜', '腊' to '臘', '鸡' to '雞',
        '猪' to '豬', '择' to '擇', '记' to '記', '醒' to '醒', '贴' to '貼',
        '签' to '籤', '属' to '屬', '栏' to '欄', '屏' to '屏', '适' to '適',
        '配' to '配', '查' to '查', '库' to '庫', '编' to '編', '辑' to '輯',
        '藏' to '藏', '缓' to '緩', '存' to '存', '温' to '溫', '习' to '習',
        '反' to '反', '馈' to '饋', '邮' to '郵', '箱' to '箱', '剪' to '剪',
        '板' to '板', '清' to '清'
    )

    fun toTraditional(text: String): String {
        // 先查短语
        PHRASE_MAP[text]?.let { return it }
        // 逐字替换
        val sb = StringBuilder(text.length)
        for (ch in text) {
            sb.append(CHAR_MAP[ch] ?: ch)
        }
        return sb.toString()
    }

    fun toSimplified(text: String): String {
        // 反向查短语
        for ((cn, tw) in PHRASE_MAP) {
            if (text == tw) return cn
        }
        val reverseCharMap = CHAR_MAP.entries.associate { (k, v) -> v to k }
        val sb = StringBuilder(text.length)
        for (ch in text) {
            sb.append(reverseCharMap[ch] ?: ch)
        }
        return sb.toString()
    }
}

val LocalLanguage = staticCompositionLocalOf { LanguageManager.LANG_SYSTEM }

/**
 * Compose 快捷国际化文本获取
 */
@Composable
@ReadOnlyComposable
fun i18n(textCn: String, textTw: String? = null): String {
    val lang = LocalLanguage.current
    val isHant = when (lang) {
        LanguageManager.LANG_ZH_TW -> true
        LanguageManager.LANG_ZH_CN -> false
        else -> {
            val config = LocalConfiguration.current
            val locale = config.locales[0]
            val script = locale.script.lowercase()
            val country = locale.country.uppercase()
            script == "hant" || country in listOf("TW", "HK", "MO")
        }
    }
    return if (isHant) {
        textTw ?: LanguageManager.toTraditional(textCn)
    } else {
        textCn
    }
}

/**
 * 非 Composable 上下文（如 Toast、回调事件、通知）快捷国际化文本获取
 */
fun tr(textCn: String, textTw: String? = null): String {
    return if (LanguageManager.isTraditional()) {
        textTw ?: LanguageManager.toTraditional(textCn)
    } else {
        textCn
    }
}

