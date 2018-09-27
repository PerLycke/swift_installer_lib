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
        if (containsKey(key)) {
            return super.get(key)
        }
        for (cat in this) {
            for (option in cat.options) {
                if (option.isSliderOption) {
                    return cat
                }
                if (option.subOptionKey.isNotEmpty()) {
                    return cat
                }
            }
        }
        return null
    }
}
