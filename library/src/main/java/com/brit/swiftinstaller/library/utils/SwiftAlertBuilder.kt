package com.brit.swiftinstaller.library.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.brit.swiftinstaller.library.R
import org.jetbrains.anko.AlertBuilder
import org.jetbrains.anko.internals.AnkoInternals
import org.jetbrains.anko.internals.AnkoInternals.NO_GETTER
import kotlin.DeprecationLevel.ERROR
import android.text.method.LinkMovementMethod
import android.widget.ListAdapter
import android.widget.TextView



class SwiftAlertBuilder(override val ctx: Context) : AlertBuilder<AlertDialog> {
    private val builder = AlertDialog.Builder(ctx, R.style.AppTheme_AlertDialog)

    override var title: CharSequence
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setTitle(value)
        }

    override var titleResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setTitle(value)
        }

    override var message: CharSequence
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setMessage(value)
        }

    override var messageResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setMessage(value)
        }

    override var icon: Drawable
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setIcon(value)
        }

    override var iconResource: Int
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setIcon(value)
        }

    override var customTitle: View
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setCustomTitle(value)
        }

    override var customView: View
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setView(value)
        }

    override var isCancelable: Boolean
        @Deprecated(NO_GETTER, level = ERROR) get() = AnkoInternals.noGetter()
        set(value) {
            builder.setCancelable(value)
        }

    override fun onCancelled(handler: (DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    override fun onKeyPressed(
            handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
        builder.setOnKeyListener(handler)
    }

    override fun positiveButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun positiveButton(buttonTextResource: Int,
                                onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonTextResource: Int,
                                onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonTextResource: Int,
                                onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun items(items: List<CharSequence>,
                       onItemSelected: (dialog: DialogInterface, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, which)
        }
    }

    override fun <T> items(items: List<T>,
                           onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, items[which], which)
        }
    }

    fun adapter(adapter: ListAdapter, onItemSelected: (dialog: DialogInterface, index: Int) -> Unit) {
        builder.setAdapter(adapter) { dialog, which ->
            onItemSelected(dialog, which)
        }

    }

    override fun build(): AlertDialog {
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(
                    android.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ctx.swift.selection.accentColor)
            dialog.getButton(
                    android.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ctx.swift.selection.accentColor)
            val dialogBg = ctx.getDrawable(R.drawable.dialog_bg) as LayerDrawable
            dialogBg.findDrawableByLayerId(R.id.dialog_bg).setTint(ctx.swift.selection.backgroundColor)
            dialog.window?.setBackgroundDrawable(dialogBg)
            dialog.findViewById<TextView>(android.R.id.message)?.movementMethod =
                    LinkMovementMethod.getInstance()

        }
        return dialog
    }

    override fun show(): AlertDialog {
        val dialog = build()
        dialog.show()
        return dialog
    }
}