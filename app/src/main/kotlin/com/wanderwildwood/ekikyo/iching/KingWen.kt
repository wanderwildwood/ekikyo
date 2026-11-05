package com.wanderwildwood.ekikyo.iching

/**
 * The eight trigrams, numbered by their lines read from the bottom up, a whole (yang) line
 * being 1: Earth 000 is 0, Thunder (whole line at the foot) 001 is 1, Heaven 111 is 7.
 */
enum class Trigram(val bits: Int, val hanzi: String, val pinyin: String) {
    EARTH(0b000, "坤", "Kūn"),
    THUNDER(0b001, "震", "Zhèn"),
    WATER(0b010, "坎", "Kǎn"),
    LAKE(0b011, "兌", "Duì"),
    MOUNTAIN(0b100, "艮", "Gèn"),
    FIRE(0b101, "離", "Lí"),
    WIND(0b110, "巽", "Xùn"),
    HEAVEN(0b111, "乾", "Qián");

    companion object {
        fun of(bits: Int): Trigram = entries.first { it.bits == bits }
    }
}

/** One of the 64, in the King Wen order the book keeps. [number] runs from 1 to 64. */
data class Hexagram(val number: Int, val upper: Trigram, val lower: Trigram, val hanzi: String, val pinyin: String) {
    /** The six lines from the bottom up, a whole line being 1: lower trigram in bits 0–2. */
    val bits: Int get() = (upper.bits shl 3) or lower.bits

    /** Line [i] (0 = the bottom) is whole. */
    fun yang(i: Int): Boolean = (bits shr i) and 1 == 1
}

object KingWen {
    private fun h(n: Int, upper: Trigram, lower: Trigram, hanzi: String, pinyin: String) =
        Hexagram(n, upper, lower, hanzi, pinyin)


    val all: List<Hexagram> = run {
        listOf(
            h(1, Trigram.HEAVEN, Trigram.HEAVEN, "乾", "Qián"),
            h(2, Trigram.EARTH, Trigram.EARTH, "坤", "Kūn"),
            h(3, Trigram.WATER, Trigram.THUNDER, "屯", "Zhūn"),
            h(4, Trigram.MOUNTAIN, Trigram.WATER, "蒙", "Méng"),
            h(5, Trigram.WATER, Trigram.HEAVEN, "需", "Xū"),
            h(6, Trigram.HEAVEN, Trigram.WATER, "訟", "Sòng"),
            h(7, Trigram.EARTH, Trigram.WATER, "師", "Shī"),
            h(8, Trigram.WATER, Trigram.EARTH, "比", "Bǐ"),
            h(9, Trigram.WIND, Trigram.HEAVEN, "小畜", "Xiǎo Chù"),
            h(10, Trigram.HEAVEN, Trigram.LAKE, "履", "Lǚ"),
            h(11, Trigram.EARTH, Trigram.HEAVEN, "泰", "Tài"),
            h(12, Trigram.HEAVEN, Trigram.EARTH, "否", "Pǐ"),
            h(13, Trigram.HEAVEN, Trigram.FIRE, "同人", "Tóng Rén"),
            h(14, Trigram.FIRE, Trigram.HEAVEN, "大有", "Dà Yǒu"),
            h(15, Trigram.EARTH, Trigram.MOUNTAIN, "謙", "Qiān"),
            h(16, Trigram.THUNDER, Trigram.EARTH, "豫", "Yù"),
            h(17, Trigram.LAKE, Trigram.THUNDER, "隨", "Suí"),
            h(18, Trigram.MOUNTAIN, Trigram.WIND, "蠱", "Gǔ"),
            h(19, Trigram.EARTH, Trigram.LAKE, "臨", "Lín"),
            h(20, Trigram.WIND, Trigram.EARTH, "觀", "Guān"),
            h(21, Trigram.FIRE, Trigram.THUNDER, "噬嗑", "Shì Kè"),
            h(22, Trigram.MOUNTAIN, Trigram.FIRE, "賁", "Bì"),
            h(23, Trigram.MOUNTAIN, Trigram.EARTH, "剝", "Bō"),
            h(24, Trigram.EARTH, Trigram.THUNDER, "復", "Fù"),
            h(25, Trigram.HEAVEN, Trigram.THUNDER, "無妄", "Wú Wàng"),
            h(26, Trigram.MOUNTAIN, Trigram.HEAVEN, "大畜", "Dà Chù"),
            h(27, Trigram.MOUNTAIN, Trigram.THUNDER, "頤", "Yí"),
            h(28, Trigram.LAKE, Trigram.WIND, "大過", "Dà Guò"),
            h(29, Trigram.WATER, Trigram.WATER, "坎", "Kǎn"),
            h(30, Trigram.FIRE, Trigram.FIRE, "離", "Lí"),
            h(31, Trigram.LAKE, Trigram.MOUNTAIN, "咸", "Xián"),
            h(32, Trigram.THUNDER, Trigram.WIND, "恆", "Héng"),
            h(33, Trigram.HEAVEN, Trigram.MOUNTAIN, "遯", "Dùn"),
            h(34, Trigram.THUNDER, Trigram.HEAVEN, "大壯", "Dà Zhuàng"),
            h(35, Trigram.FIRE, Trigram.EARTH, "晉", "Jìn"),
            h(36, Trigram.EARTH, Trigram.FIRE, "明夷", "Míng Yí"),
            h(37, Trigram.WIND, Trigram.FIRE, "家人", "Jiā Rén"),
            h(38, Trigram.FIRE, Trigram.LAKE, "睽", "Kuí"),
            h(39, Trigram.WATER, Trigram.MOUNTAIN, "蹇", "Jiǎn"),
            h(40, Trigram.THUNDER, Trigram.WATER, "解", "Xiè"),
            h(41, Trigram.MOUNTAIN, Trigram.LAKE, "損", "Sǔn"),
            h(42, Trigram.WIND, Trigram.THUNDER, "益", "Yì"),
            h(43, Trigram.LAKE, Trigram.HEAVEN, "夬", "Guài"),
            h(44, Trigram.HEAVEN, Trigram.WIND, "姤", "Gòu"),
            h(45, Trigram.LAKE, Trigram.EARTH, "萃", "Cuì"),
            h(46, Trigram.EARTH, Trigram.WIND, "升", "Shēng"),
            h(47, Trigram.LAKE, Trigram.WATER, "困", "Kùn"),
            h(48, Trigram.WATER, Trigram.WIND, "井", "Jǐng"),
            h(49, Trigram.LAKE, Trigram.FIRE, "革", "Gé"),
            h(50, Trigram.FIRE, Trigram.WIND, "鼎", "Dǐng"),
            h(51, Trigram.THUNDER, Trigram.THUNDER, "震", "Zhèn"),
            h(52, Trigram.MOUNTAIN, Trigram.MOUNTAIN, "艮", "Gèn"),
            h(53, Trigram.WIND, Trigram.MOUNTAIN, "漸", "Jiàn"),
            h(54, Trigram.THUNDER, Trigram.LAKE, "歸妹", "Guī Mèi"),
            h(55, Trigram.THUNDER, Trigram.FIRE, "豐", "Fēng"),
            h(56, Trigram.FIRE, Trigram.MOUNTAIN, "旅", "Lǚ"),
            h(57, Trigram.WIND, Trigram.WIND, "巽", "Xùn"),
            h(58, Trigram.LAKE, Trigram.LAKE, "兌", "Duì"),
            h(59, Trigram.WIND, Trigram.WATER, "渙", "Huàn"),
            h(60, Trigram.WATER, Trigram.LAKE, "節", "Jié"),
            h(61, Trigram.WIND, Trigram.LAKE, "中孚", "Zhōng Fú"),
            h(62, Trigram.THUNDER, Trigram.MOUNTAIN, "小過", "Xiǎo Guò"),
            h(63, Trigram.WATER, Trigram.FIRE, "既濟", "Jì Jì"),
            h(64, Trigram.FIRE, Trigram.WATER, "未濟", "Wèi Jì"),
        )
    }

    private val byBits: Map<Int, Hexagram> = all.associateBy { it.bits }

    fun number(n: Int): Hexagram = all[n - 1]

    /** The hexagram whose six lines, from the bottom up, are [bits]. */
    fun ofBits(bits: Int): Hexagram = byBits.getValue(bits and 0b111111)
}
