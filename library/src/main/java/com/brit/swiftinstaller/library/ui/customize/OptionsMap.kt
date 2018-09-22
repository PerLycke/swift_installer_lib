package com.brit.swiftinstaller.library.ui.customize

import java.util.function.Consumer

class OptionsMap : HashMap<String, Option>(), Iterable<Option> {
    override fun iterator(): Iterator<Option> {
        return object : Iterator<Option> {
            private var current = 0
            override fun hasNext(): Boolean {
                return current != size
            }

            override fun next(): Option {
                return get(keys.toTypedArray()[current++])!!
            }

        }
    }

    fun forEachOption(action: (Option) -> Unit) {
            values.forEach { action.invoke(it) }
    }

    fun add(option: Option) {
        put(option.value, option)
    }
}