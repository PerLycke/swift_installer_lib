package com.brit.swiftinstaller.library.utils

import java.util.Comparator
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

class SynchronizedArrayList<T>(): ArrayList<T>() {

    constructor(arrayList: ArrayList<T>): this() {
        arrayList.forEach {
            add(it)
        }
        arrayList.clear()
    }

    override val size: Int
        @Synchronized
        get() = super.size

    @Synchronized
    override fun add(element: T): Boolean {
        synchronized(this) {
            return super.add(element)
        }
    }

    @Synchronized
    override fun addAll(elements: Collection<T>): Boolean {
        synchronized(this) {
            return super.addAll(elements)
        }
    }

    @Synchronized
    override fun add(index: Int, element: T) {
        synchronized(this) {
            super.add(index, element)
        }
    }

    @Synchronized
    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        synchronized(this) {
            return super.addAll(index, elements)
        }
    }

    @Synchronized
    override fun clear() {
        synchronized(this) {
            super.clear()
        }
    }

    @Synchronized
    override fun contains(element: T): Boolean {
        synchronized(this) {
            return super.contains(element)
        }
    }

    @Synchronized
    override fun forEach(action: Consumer<in T>) {
        synchronized(this) {
            super.forEach(action)
        }
    }

    @Synchronized
    override fun get(index: Int): T {
        synchronized(this) {
            return super.get(index)
        }
    }

    @Synchronized
    override fun indexOf(element: T): Int {
        synchronized(this) {
            return super.indexOf(element)
        }
    }

    @Synchronized
    override fun isEmpty(): Boolean {
        synchronized(this) {
            return super.isEmpty()
        }
    }

    @Synchronized
    override fun iterator(): MutableIterator<T> {
        synchronized(this) {
            return object : MutableIterator<T> {
                private var index: Int = 0
                private var lastIndex: Int = -1

                @Synchronized
                override fun hasNext(): Boolean {
                    synchronized(this@SynchronizedArrayList) {
                        return index < size
                    }
                }

                @Synchronized
                override fun next(): T {
                    synchronized(this@SynchronizedArrayList) {
                        if (index >= size) throw NoSuchElementException()
                        lastIndex = index++
                        return get(lastIndex)
                    }
                }

                @Synchronized
                override fun remove() {
                    synchronized(this@SynchronizedArrayList) {
                        check(lastIndex != -1) { "Call next() or previous() before removing element from the iterator." }
                        removeAt(lastIndex)
                        index = lastIndex
                        lastIndex = -1
                    }
                }

            }
        }
    }

    @Synchronized
    override fun remove(element: T): Boolean {
        synchronized(this) {
            return super.remove(element)
        }
    }

    @Synchronized
    override fun removeAt(index: Int): T {
        synchronized(this) {
            return super.removeAt(index)
        }
    }

    @Synchronized
    override fun removeIf(filter: Predicate<in T>): Boolean {
        synchronized(this) {
            return super.removeIf(filter)
        }
    }

    @Synchronized
    override fun removeRange(fromIndex: Int, toIndex: Int) {
        synchronized(this) {
            super.removeRange(fromIndex, toIndex)
        }
    }

    @Synchronized
    override fun clone(): Any {
        synchronized(this) {
            return super.clone()
        }
    }

    @Synchronized
    override fun ensureCapacity(minCapacity: Int) {
        synchronized(this) {
            super.ensureCapacity(minCapacity)
        }
    }

    @Synchronized
    override fun lastIndexOf(element: T): Int {
        synchronized(this) {
            return super.lastIndexOf(element)
        }
    }

    @Synchronized
    override fun removeAll(elements: Collection<T>): Boolean {
        synchronized(this) {
            return super.removeAll(elements)
        }
    }

    @Synchronized
    override fun replaceAll(operator: UnaryOperator<T>) {
        synchronized(this) {
            super.replaceAll(operator)
        }
    }

    @Synchronized
    override fun sort(c: Comparator<in T>?) {
        synchronized(this) {
            super.sort(c)
        }
    }
}