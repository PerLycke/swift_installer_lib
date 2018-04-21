package com.brit.swiftinstaller.utils

class MaterialPalette {
    var backgroundColor = 0
    var cardBackgroud = 0
    var floatingBackground = 0
    var darkBackgroundColor = 0
    var darkerBackgroundColor = 0
    var ligherBackgroundColor = 0
    companion object {
        fun createPalette(color: Int): MaterialPalette {
            val p = MaterialPalette()
            p.backgroundColor = color
            p.cardBackgroud = (color - 0xf7f7f8)
            p.floatingBackground = (color - 0xfcfcfd)
            p.darkBackgroundColor = (color - 0x050505)
            p.darkerBackgroundColor = (color - 0x0a0a0a)
            p.ligherBackgroundColor = (color - 0xebebec)
            return p
        }
    }

    override fun toString(): String {
        return "backgroundColor - ${Integer.toHexString(backgroundColor)}\n " +
                "cardBackground - ${Integer.toHexString(cardBackgroud)}\n" +
                "floatingBackground - ${Integer.toHexString(floatingBackground)}\n" +
                "darkBackground - ${Integer.toHexString(darkBackgroundColor)}\n" +
                "darkerBackground - ${Integer.toHexString(darkerBackgroundColor)}\n" +
                "lighterBackground - ${Integer.toHexString(ligherBackgroundColor)}\n"
    }
}