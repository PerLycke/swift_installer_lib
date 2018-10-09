package com.brit.swiftinstaller.library.ui.customize

class CategoryMap : HashMap<String, CustomizeCategory>(), Iterable<CustomizeCategory> {
    override fun iterator(): Iterator<CustomizeCategory> {
        return object : Iterator<CustomizeCategory> {
            private var current = 0
            override fun hasNext(): Boolean {
                return current != size
            }

            override fun next(): CustomizeCategory {
                return get(keys.toTypedArray()[current++])!!
            }

        }
    }

    fun add(category: CustomizeCategory) {
        put(category.key, category)
    }

    override fun get(key: String): CustomizeCategory? {
        if (super.containsKey(key)) {
            return super.get(key)
        }
        for (cat in this) {
            for (option in cat.options) {
                if (option.isSliderOption) {
                    if (option.value == key) {
                        return cat
                    }
                }
                if (option.subOptionKey.isNotEmpty()) {
                    if (option.subOptionKey == key) {
                        return cat
                    }
                }
            }
        }
        return null
    }

    override fun containsKey(key: String): Boolean {
        if (super.containsKey(key)) {
            return true
        }
        for (cat in this) {
            for (option in cat.options) {
                if (option.isSliderOption) {
                    if (option.value == key) {
                        return true
                    }
                }
                if (option.subOptionKey == key) {
                    return true
                }
            }
        }
        return false
    }
}
