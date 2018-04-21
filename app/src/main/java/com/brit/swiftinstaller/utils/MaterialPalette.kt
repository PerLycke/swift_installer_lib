package com.brit.swiftinstaller.utils

class MaterialPalette {
    var backgroundColor = 0
    var cardBackgroud = 0
    var floatingBackground = 0
    var darkBackgroundColor = 0
    var darkerBackgroundColor = 0
    var lighterBackgroundColor = 0
    var buttonBackground = 0
    var otherBackground = 0

    companion object {
        fun createPalette(color: Int, palette: Boolean): MaterialPalette {
            val p = MaterialPalette()
            if (palette) {
                p.backgroundColor = color
                p.cardBackgroud = (color - 0xf7f7f8)
                p.floatingBackground = (color - 0xfcfcfd)
                p.darkBackgroundColor = (color - 0x050505)
                p.darkerBackgroundColor = (color - 0x0a0a0a)
                p.lighterBackgroundColor = (color - 0xebebec)
                p.buttonBackground = (color - 0xefeff0)
                p.otherBackground = (color - 0xe8e8e9)
            } else {
                p.backgroundColor = color
                if (Integer.toHexString(color).substring(2) == "000000") {
                    p.cardBackgroud = (color - 0xebebec)
                } else {
                    p.cardBackgroud = (color - 0xf7f7f8)
                }
                p.floatingBackground = color
                p.darkBackgroundColor = color
                p.darkerBackgroundColor = color
                p.lighterBackgroundColor = color
                p.buttonBackground = color
                p.otherBackground = color
            }
            return p
        }
    }

    override fun toString(): String {
        return "backgroundColor - ${Integer.toHexString(backgroundColor)}\n " +
                "cardBackground - ${Integer.toHexString(cardBackgroud)}\n" +
                "floatingBackground - ${Integer.toHexString(floatingBackground)}\n" +
                "darkBackground - ${Integer.toHexString(darkBackgroundColor)}\n" +
                "darkerBackground - ${Integer.toHexString(darkerBackgroundColor)}\n" +
                "lighterBackground - ${Integer.toHexString(lighterBackgroundColor)}\n" +
                "buttonBackgruond - ${Integer.toHexString(buttonBackground)}\n" +
                "otherBackground - ${Integer.toHexString(otherBackground)}"
    }
}