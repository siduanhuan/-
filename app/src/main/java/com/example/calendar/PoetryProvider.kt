package com.example.calendar

/**
 * 逢时 - 东方诗词文案库
 * 精选契合生日、纪念日、吉日节庆、长远祈愿的古典文学名句，用于生成高留白极简分享卡片。
 */
object PoetryProvider {

    data class Poem(
        val content: String,
        val source: String,
        val category: String // BIRTHDAY, ANNIVERSARY, FRIENDSHIP, FESTIVAL, TARGET, GENERAL
    )

    val POEMS = listOf(
        // ================= 【金石良缘 / 纪念日】 =================
        Poem("与君初相识，犹如故人归。", "《国风·唐风》", "ANNIVERSARY"),
        Poem("愿得一人心，白首不相离。", "卓文君《白头吟》", "ANNIVERSARY"),
        Poem("玲珑骰子安红豆，入骨相思知不知。", "温庭筠《南歌子》", "ANNIVERSARY"),
        Poem("一日不思量，也攒眉千度。", "柳永《昼夜乐》", "ANNIVERSARY"),
        Poem("桃之夭夭，灼灼其华。之子于归，宜其室家。", "《诗经·周南》", "ANNIVERSARY"),
        Poem("山无棱，江水为竭，冬雷震震，夏雨雪，天地合，乃敢与君绝。", "《汉乐府·上邪》", "ANNIVERSARY"),
        Poem("死生契阔，与子成说。执子之手，与子偕老。", "《诗经·邶风·击鼓》", "ANNIVERSARY"),
        Poem("结发为夫妻，恩爱两不疑。", "苏武《留别妻》", "ANNIVERSARY"),
        Poem("入我相思门，知我相思苦。长相思兮长相忆，短相思兮无穷极。", "李白《三五七言》", "ANNIVERSARY"),
        Poem("身无彩凤双飞翼，心有灵犀一点通。", "李商隐《无题》", "ANNIVERSARY"),
        Poem("平生不会相思，才会相思，便害相思。", "徐再思《折桂令》", "ANNIVERSARY"),
        Poem("金风玉露一相逢，便胜却人间无数。", "秦观《鹊桥仙》", "ANNIVERSARY"),
        Poem("两情若是久长时，又岂在朝朝暮暮。", "秦观《鹊桥仙》", "ANNIVERSARY"),
        Poem("何当共剪西窗烛，却话巴山夜雨时。", "李商隐《夜雨寄北》", "ANNIVERSARY"),
        Poem("宜言饮酒，与子偕老。琴瑟在御，莫不静好。", "《诗经·郑风》", "ANNIVERSARY"),

        // ================= 【生辰寿诞 / 华诞祝颂】 =================
        Poem("岁岁常欢愉，万事皆胜意。", "《逢时祝颂》", "BIRTHDAY"),
        Poem("愿保兹善，千载为常。欢笑尽娱，乐哉未央。", "曹丕《善哉行》", "BIRTHDAY"),
        Poem("松龄长岁月，鹤语记春秋。", "《寿言对联》", "BIRTHDAY"),
        Poem("如月之恒，如日之升。如南山之寿，不骞不崩。", "《诗经·小雅·天保》", "BIRTHDAY"),
        Poem("春祺夏安，秋绥冬禧。所行化坦途，所求皆所愿。", "《古典尺牍寄语》", "BIRTHDAY"),
        Poem("且向花间留晚照，长生未老乐无涯。", "苏轼《浣溪沙》", "BIRTHDAY"),
        Poem("愿尔如松柏，千秋永岁青。", "元稹《遣病》", "BIRTHDAY"),
        Poem("称觞奏乐迎佳节，华堂春暖寿星欢。", "《寿词纪事》", "BIRTHDAY"),
        Poem("四序春为首，万机寿为先。", "白居易《喜老自嘲》", "BIRTHDAY"),
        Poem("愿从今日始，岁岁有今朝。南山共松柏，福泽自滔滔。", "《逢时祝寿词》", "BIRTHDAY"),
        Poem("愿长康吉，身无疾疫，心无萦怀，百岁光阴得自在。", "《唐人写经祈愿》", "BIRTHDAY"),
        Poem("但愿年年春草绿，共君长醉不言归。", "《祝寿集》", "BIRTHDAY"),
        Poem("九如天保，万寿无疆。", "《诗经》", "BIRTHDAY"),
        Poem("天增岁月人增寿，春满乾坤福满门。", "《春祺寿语》", "BIRTHDAY"),
        Poem("愿借春风为长命，吹向君前作寿筹。", "《古诗祝生》", "BIRTHDAY"),

        // ================= 【友谊长青 / 知己同心】 =================
        Poem("海内存知己，天涯若比邻。", "王勃《送杜少府之任蜀州》", "FRIENDSHIP"),
        Poem("江南无所有，聊赠一枝春。", "陆凯《赠范晔诗》", "FRIENDSHIP"),
        Poem("相逢意气为君饮，系马高楼垂柳边。", "王维《少年行》", "FRIENDSHIP"),
        Poem("莫愁前路无知己，天下谁人不识君。", "高适《别董大》", "FRIENDSHIP"),
        Poem("同声自相应，同心自相知。", "晋·傅玄《拟青青河畔草》", "FRIENDSHIP"),
        Poem("人生所贵在知己，四海相逢骨肉亲。", "《增广贤文》", "FRIENDSHIP"),
        Poem("落日见秋草，暮年逢故人。", "李端《喜见外弟又言别》", "FRIENDSHIP"),
        Poem("青山一道同云雨，明月何曾是两乡。", "王昌龄《送柴侍御》", "FRIENDSHIP"),
        Poem("问姓惊初见，称名忆旧容。", "李益《喜见外弟又言别》", "FRIENDSHIP"),

        // ================= 【岁时节令 / 良辰美景】 =================
        Poem("良辰美景奈何天，赏心乐事谁家院。", "汤显祖《牡丹亭》", "FESTIVAL"),
        Poem("但愿人长久，千里共婵娟。", "苏轼《水调歌头》", "FESTIVAL"),
        Poem("逢时遇节，自然成就。", "《景德传灯录》", "FESTIVAL"),
        Poem("海上生明月，天涯共此时。", "张九龄《望月怀远》", "FESTIVAL"),
        Poem("爆竹声中一岁除，春风送暖入屠苏。", "王安石《元日》", "FESTIVAL"),
        Poem("千门万户曈曈日，总把新桃换旧符。", "王安石《元日》", "FESTIVAL"),
        Poem("清明时节雨纷纷，路上行人欲断魂。", "杜牧《清明》", "FESTIVAL"),
        Poem("彩线轻缠红玉臂，小符斜挂绿云鬟。佳人相见一千年。", "苏轼《浣溪沙·端午》", "FESTIVAL"),
        Poem("中秋月，月到中秋偏皎洁。偏皎洁，知他多少阴晴圆缺。", "徐有贞《中宪大夫》", "FESTIVAL"),
        Poem("独在异乡为异客，每逢佳节倍思亲。", "王维《九月九日忆山东兄弟》", "FESTIVAL"),
        Poem("天街小雨润如酥，草色遥看近却无。", "韩愈《早春呈水部张十八员外》", "FESTIVAL"),
        Poem("风雨送春归，飞雪迎春到。", "《卜算子·咏梅》", "FESTIVAL"),

        // ================= 【长远励志 / 岁序更迭】 =================
        Poem("人生天地之间，若白驹过隙，忽然而已。", "《庄子·知北游》", "TARGET"),
        Poem("路漫漫其修远兮，吾将上下而求索。", "屈原《离骚》", "TARGET"),
        Poem("博观而约取，厚积而薄发。", "苏轼《稼说送张琥》", "TARGET"),
        Poem("长风破浪会有时，直挂云帆济沧海。", "李白《行路难》", "TARGET"),
        Poem("且挨过三冬四夏，暂受些此苦彼酸，逢时遇节，自得芬芳。", "《四时随笔》", "TARGET"),
        Poem("莫道桑榆晚，为霞尚满天。", "刘禹锡《酬乐天咏老见示》", "TARGET"),
        Poem("千淘万漉虽辛苦，吹尽狂沙始到金。", "刘禹锡《浪淘沙》", "TARGET"),
        Poem("星光不问赶路人，岁月不负有心人。", "《逢时寄语》", "TARGET"),
        Poem("愿君历尽千帆，归来仍是少年。", "《逢时赠言》", "TARGET"),
        Poem("不积跬步，无以至千里；不积小流，无以成江海。", "荀子《劝学》", "TARGET"),
        Poem("沉舟侧畔千帆过，病树前头万木春。", "刘禹锡《酬乐天扬州初逢席上见赠》", "TARGET")
    )

    fun getPoemsForCategory(category: String): List<Poem> {
        val targetCat = when (category.uppercase()) {
            "BIRTHDAY", "生日" -> "BIRTHDAY"
            "ANNIVERSARY", "纪念日" -> "ANNIVERSARY"
            "FRIENDSHIP", "朋友", "友谊" -> "FRIENDSHIP"
            "FESTIVAL", "节日" -> "FESTIVAL"
            "TARGET", "目标", "长远目标" -> "TARGET"
            else -> null
        }
        val list = if (targetCat != null) POEMS.filter { it.category == targetCat } else emptyList()
        return if (list.isNotEmpty()) list else POEMS
    }

    fun getRandomPoem(category: String? = null): Poem {
        val candidates = if (!category.isNullOrEmpty()) {
            getPoemsForCategory(category)
        } else {
            POEMS
        }
        return (if (candidates.isNotEmpty()) candidates else POEMS).random()
    }
}

