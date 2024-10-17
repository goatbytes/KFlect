/*
 * Copyright (c) 2024 GoatBytes.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.goatbytes.kflect.cache

import io.goatbytes.kflect.DEBUG
import io.goatbytes.kflect.Reflective
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * A thread-safe cache for storing and retrieving reflection members based on their [CacheKey].
 */
open class Cache<T : Reflective> : ConcurrentMap<CacheKey, T> {

  /**
   * Companion object containing the default values for the cache.
   */
  companion object {
    /** The default TTL for the cache. */
    const val DEFAULT_TTL = 5 * 60 * 1000L

    /** The default maximum size of the cache. */
    const val DEFAULT_MAX_SIZE = 1000
  }

  // Internal cache storage
  private val _cache = ConcurrentHashMap<CacheKey, CacheEntry>()

  private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  private var ttl = DEFAULT_TTL
  private var maxSize = DEFAULT_MAX_SIZE

  init {
    // Schedule cache cleanup every minute
    executor.scheduleAtFixedRate(::cleanup, 1, 1, TimeUnit.MINUTES)
  }

  /** entries A mutable set of key-value pairs contained in the cache. **/
  override val entries: MutableSet<MutableMap.MutableEntry<CacheKey, T>>
    get() = _cache.entries.map { (key, entry) ->
      object : MutableMap.MutableEntry<CacheKey, T> {
        private var _value = entry.get()
        override val key = key
        override val value get() = _value
        override fun setValue(newValue: T): T {
          val previous = _value
          _value = newValue
          return previous
        }
      }
    }.toMutableSet()

  /** A mutable set of keys contained in the cache. */
  override val keys: MutableSet<CacheKey> get() = _cache.keys

  /** A mutable collection of values contained in the cache. */
  override val values: MutableCollection<T>
    get() = _cache.values.map { entry -> entry.get() }.toMutableList()

  /** The number of key-value pairs in the cache. */
  override val size: Int get() = _cache.size

  /**
   * Clears the cache, removing all entries.
   */
  override fun clear() = _cache.clear()

  /**
   * Returns `true` if the cache contains no entries, `false` otherwise.
   */
  override fun isEmpty(): Boolean = _cache.isEmpty()

  /**
   * Replaces the entry for the specified key only if it is currently mapped to some value.
   *
   * @param key The key whose associated value is to be replaced.
   * @param value The value to be associated with the key.
   * @return The previous value associated with the key, or `null` if there was no mapping.
   */
  override fun replace(key: CacheKey, value: T): T? =
    _cache.replace(key, CacheEntry(value, System.currentTimeMillis()))?.get()

  /**
   * Replaces the entry for the specified key only if it is currently mapped to the
   * specified old value.
   *
   * @param key The key whose associated value is to be replaced.
   * @param oldValue The old value expected to be associated with the key.
   * @param newValue The new value to be associated with the key.
   * @return `true` if the value was replaced, `false` otherwise.
   */
  override fun replace(key: CacheKey, oldValue: T, newValue: T) =
    _cache.replace(key, CacheEntry(oldValue), CacheEntry(newValue))

  /**
   * If the specified key is not already associated with a value, associate it with the given value.
   *
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the key.
   * @return The previous value associated with the key, or `null` if there was no mapping..
   */
  override fun putIfAbsent(key: CacheKey, value: T): T? =
    _cache.putIfAbsent(key, CacheEntry(value))?.get()

  /**
   * Returns the value to which the specified key is mapped, or `null` if the cache contains no
   * mapping for the key.
   *
   * @param key The key whose associated value is to be returned.
   * @return The value associated with the key, or `null` if there is no mapping for the key.
   */
  override fun get(key: CacheKey): T? = _cache[key]?.get()

  /**
   * Returns `true` if the cache contains one or more mappings to the specified value.
   *
   * @param value The value whose presence in the cache is to be tested.
   * @return `true` if the cache contains one or more mappings for the value.
   */
  override fun containsValue(value: T): Boolean = _cache.any { it.value.value == value }

  /**
   * Returns `true` if the cache contains a mapping for the specified key.
   *
   * @param key The key whose presence in the cache is to be tested.
   * @return `true` if the cache contains a mapping for the key.
   */
  override fun containsKey(key: CacheKey): Boolean = _cache.containsKey(key)

  /**
   * Removes the mapping for the specified key from the cache if present.
   *
   * @param key The key whose mapping is to be removed from the cache.
   * @return The previous value associated with the key, or `null` if there was no mapping.
   */
  override fun remove(key: CacheKey): T? = _cache.remove(key)?.get()

  /**
   * Removes the entry for the specified key only if it is currently mapped to the specified value.
   *
   * @param key The key whose entry is to be removed from the cache.
   * @param value The value expected to be associated with the key.
   * @return `true` if the entry was removed, `false` otherwise.
   */
  override fun remove(key: CacheKey, value: T): Boolean =
    _cache[key]?.get() == value && _cache.remove(key) != null

  /**
   * Copies all the mappings from the specified map to the cache.
   *
   * @param from The map from which to copy mappings.
   */
  override fun putAll(from: Map<out CacheKey, T>) =
    _cache.putAll(from.map { (key, member) -> key to CacheEntry(member) }.toList())

  /**
   * Associates the specified value with the specified key in the cache.
   *
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the key.
   * @return The previous value associated with the key, or `null` if there was no
   *         mapping for the key.
   */
  override fun put(key: CacheKey, value: T): T? {
    if (_cache.size >= maxSize) evictOldestEntry()
    return _cache.put(key, CacheEntry(value))?.get()
  }

  /**
   * Sets the time-to-live (TTL) for cache entries. Entries older than the TTL will be evicted
   * during cleanup.
   *
   * @param ttl The time-to-live in milliseconds.
   * @return The [Cache] object itself for chaining.
   */
  fun setTTL(ttl: Long) = apply { this.ttl = ttl }

  /**
   * Sets the maximum size of the cache. Once the cache reaches this size, the oldest entry will
   * be evicted.
   *
   * @param maxSize The maximum number of entries the cache can hold.
   * @return The [Cache] object itself for chaining.
   */
  fun setMaxSize(maxSize: Int) = apply { this.maxSize = maxSize }

  /**
   * Cleans up the cache by removing entries that have exceeded their time-to-live (TTL).
   */
  internal fun cleanup() {
    val now = System.currentTimeMillis()
    if (DEBUG) println("Cleaning up cache at $now, evicting expired entries.")
    _cache.entries.removeIf { (_, entry) -> now - entry.timestamp > ttl }
  }

  private fun evictOldestEntry() {
    val oldestEntry = _cache.minByOrNull { it.value.timestamp }
    oldestEntry?.let {
      if (DEBUG) println("Evicting oldest cache entry: ${oldestEntry.key}")
      _cache.remove(it.key)
    }
  }

  /**
   * Cleans up the cache and shuts down the executor to evict the cache.
   */
  fun shutdown() {
    cleanup()
    if (!executor.isShutdown) {
      executor.shutdown()
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun CacheEntry.get(): T = requireNotNull(value as? T) { "Invalid cached value type" }
}
