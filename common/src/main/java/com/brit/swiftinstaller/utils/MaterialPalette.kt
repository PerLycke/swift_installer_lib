package com.brit.swiftinstaller.utils

import android.content.Context
import com.brit.swiftinstaller.utils.ColorUtils.handleColor

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
        fun get(context: Context): MaterialPalette {
            return createPalette(getBackgroundColor(context), useBackgroundPalette(context))
        }

        fun createPalette(color: Int, palette: Boolean): MaterialPalette {
            val p = MaterialPalette()
            if (palette) {
                p.backgroundColor = color
                p.cardBackgroud = handleColor(color, 8)
                p.floatingBackground = handleColor(color, 3)
                p.darkBackgroundColor = handleColor(color, -5)
                p.darkerBackgroundColor = handleColor(color, -10)
                p.lighterBackgroundColor = handleColor(color, 20)
                p.buttonBackground = handleColor(color, 16)
                p.otherBackground = handleColor(color, 23)
            } else {
                p.backgroundColor = color
                p.cardBackgroud = color
                p.floatingBackground = color
                p.darkBackgroundColor = color
                p.darkerBackgroundColor = color
                p.lighterBackgroundColor = color
                p.buttonBackground = handleColor(color, 20)
                p.otherBackground = handleColor(color, 33)
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