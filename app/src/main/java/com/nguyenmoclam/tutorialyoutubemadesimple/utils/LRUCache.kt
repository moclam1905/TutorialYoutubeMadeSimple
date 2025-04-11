package com.nguyenmoclam.tutorialyoutubemadesimple.utils

import java.util.LinkedHashMap

/**
 * Implementation of LRU (Least Recently Used) cache
 * @param maxSize Maximum size of the cache (in bytes)
 */
class LRUCache<K, V>(private val maxSize: Long) {
    private val cache = object : LinkedHashMap<K, CacheEntry<V>>(100, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
            return currentSize > maxSize
        }
    }

    private var currentSize: Long = 0

    /**
     * Add an item to the cache
     * @param key Key of the item
     * @param value Value of the item
     * @param size Size of the item (in bytes)
     */
    @Synchronized
    fun put(key: K, value: V, size: Long) {
        // Remove old item if exists
        remove(key)

        // Check if new item exceeds maximum size
        if (size > maxSize) {
            return
        }

        // Remove old items until there is enough space
        while (currentSize + size > maxSize && cache.isNotEmpty()) {
            val eldestEntry = cache.entries.first()
            remove(eldestEntry.key)
        }

        // Add new item
        cache[key] = CacheEntry(value, size)
        currentSize += size
    }

    /**
     * Get an item from the cache
     * @param key Key of the item
     * @return Value of the item or null if not exists
     */
    @Synchronized
    fun get(key: K): V? {
        return cache[key]?.value
    }

    /**
     * Remove an item from the cache
     * @param key Key of the item to remove
     */
    @Synchronized
    fun remove(key: K) {
        val entry = cache.remove(key)
        if (entry != null) {
            currentSize -= entry.size
        }
    }

    /**
     * Clear all items in the cache
     */
    @Synchronized
    fun clear() {
        cache.clear()
        currentSize = 0
    }

    /**
     * Get current size of the cache
     * @return Current size (in bytes)
     */
    @Synchronized
    fun getCurrentSize(): Long = currentSize

    /**
     * Check if a key exists in the cache
     * @param key Key to check
     * @return true if key exists, false otherwise
     */
    @Synchronized
    fun contains(key: K): Boolean = cache.containsKey(key)

    /**
     * Get list of all keys in the cache
     * @return Set of keys
     */
    @Synchronized
    fun getKeys(): Set<K> = cache.keys

    /**
     * Wrapper class for cache items
     */
    private data class CacheEntry<V>(
        val value: V,
        val size: Long
    )
}