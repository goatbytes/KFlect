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

package io.goatbytes.kflect.misc

import io.goatbytes.kflect.dsl.kflect
import io.goatbytes.kflect.lazy.SynchronizedLazyKFlect
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.security.ProtectionDomain

/**
 *        /\                 /\
 *       / \'._   (\_/)   _.'/ \
 *      / .''._'--(o.o)--'_.''. \
 *     /.' _' |`'=/ " \='`| '_ `.\
 *    /__.-' |               | `-.__\
 *           |    DRAGONS    |
 *           |   AHEAD! ⚠️   |
 *
 * The `TheUnsafe` object provides a reflection-based access layer to the internal `sun.misc.Unsafe`
 * class, enabling direct interaction with low-level memory manipulation and concurrency primitives.
 * **WARNING: Proceed with caution!**
 *
 * Just as a brave knight would approach a dragon with great care, use `TheUnsafe` wisely. It's a
 * powerful tool, capable of low-level operations such as:
 * - Direct memory allocation, reallocation, and freeing
 * - Atomic compare-and-swap (CAS) operations
 * - Thread parking/un-parking mechanisms
 * - Memory barriers for enforcing memory ordering
 *
 * But beware! Incorrect usage can lead to unpredictable behavior, crashes, or memory corruption.
 * Misuse of `Unsafe` is like waking a dragon—dangerous and potentially catastrophic.
 *
 * **Pro tip**: If you must venture into the world of `Unsafe`, ensure you know what you're doing!
 *
 * **Features**:
 * - Direct memory manipulation (e.g., `allocateMemory`, `freeMemory`, `putObject`, etc.)
 * - Atomic concurrency operations (e.g., `compareAndSwapInt`, `compareAndSwapObject`)
 * - Memory barriers (`fullFence`, `loadFence`, `storeFence`)
 * - Low-level field and object access
 *
 * **Note**: Most of these operations are restricted in production environments, and misuse
 * can cause serious issues in your application.
 *
 * **In short**: Tread carefully, for here be dragons.
 */

object TheUnsafe {

  private const val UNSAFE_CLASS_NAME = "sun.misc.Unsafe"
  private const val UNSAFE_FIELD_NAME = "theUnsafe"
  private const val THE_ONE_FIELD_NAME = "THE_ONE"

  private val unsafe: Any? by SynchronizedLazyKFlect {
    UNSAFE_CLASS_NAME[UNSAFE_FIELD_NAME] ?: UNSAFE_CLASS_NAME[THE_ONE_FIELD_NAME]
  }

  /**
   * Retrieves the singleton instance of `sun.misc.Unsafe`.
   * This method uses reflection to access the internal `Unsafe` instance.
   *
   * @return The `Unsafe` instance.
   * @throws ClassCastException if the retrieved object cannot be cast to `Unsafe`.
   */
  fun get(): Unsafe = unsafe as Unsafe

  /**
   * Gets the offset of the specified static field.
   *
   * @param field The field to get the offset of.
   * @return The offset of the field.
   * @see sun.misc.Unsafe#staticFieldOffset
   */
  fun staticFieldOffset(field: Field): Long = kflect {
    unsafe("staticFieldOffset", field) as Long
  }

  /**
   * Gets the offset of the specified object field.
   *
   * @param field The field to get the offset of.
   * @return The offset of the field.
   * @see sun.misc.Unsafe#objectFieldOffset
   */
  fun objectFieldOffset(field: Field): Long = kflect {
    unsafe("objectFieldOffset", field) as Long
  }

  /**
   * Writes an object to a given object at the specified memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The object to write.
   * @see sun.misc.Unsafe#putObjectVolatile
   */
  fun putObjectVolatile(o: Any?, offset: Long, x: Any?): Unit = kflect {
    unsafe("putObjectVolatile", o, offset, x)
  }

  /**
   * Writes an object to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The object to write.
   * @see sun.misc.Unsafe#putObject
   */
  fun putObject(o: Any?, offset: Long, x: Any?): Unit = kflect {
    unsafe("putObject", o, offset, x)
  }

  /**
   * Returns the offset of the first element in the storage allocation of a given array class.
   *
   * This value is intended to allow access to the array elements via direct memory access.
   *
   * @param clazz The class of the array.
   * @return The offset in bytes to the first element of an array of this class.
   * @see sun.misc.Unsafe#arrayBaseOffset
   */
  fun arrayBaseOffset(clazz: Class<*>): Int = kflect {
    unsafe("arrayBaseOffset", clazz) as Int
  }

  /**
   * Returns the scale factor for addressing elements in the array.
   *
   * This value is the size in bytes of the array element, used to perform direct memory access
   * or indexing into the array.
   *
   * @param clazz The class of the array.
   * @return The scale factor in bytes for an array of this class.
   * @see sun.misc.Unsafe#arrayIndexScale
   */
  fun arrayIndexScale(clazz: Class<*>): Int = kflect {
    unsafe("arrayIndexScale", clazz) as Int
  }

  /**
   * Returns the size in bytes of the native pointer for the platform.
   *
   * This method is used when dealing with raw memory addresses.
   *
   * @return The size in bytes of the platform's address representation.
   * @see sun.misc.Unsafe#addressSize
   */
  fun addressSize(): Int = kflect {
    unsafe("addressSize") as Int
  }

  /**
   * Allocates a block of memory of the specified size.
   *
   * This memory block is not managed by the garbage collector and must be explicitly freed.
   * It is initialized to zero.
   *
   * @param size The size of memory to allocate in bytes.
   * @return The address of the allocated memory block.
   * @see sun.misc.Unsafe#allocateMemory
   */
  fun allocateMemory(size: Long): Long = kflect {
    unsafe("allocateMemory", size) as Long
  }

  /**
   * Reallocates a block of memory to a new size.
   *
   * The content of the memory is preserved up to the size of the original memory block.
   * The newly allocated portion, if any, is initialized to zero.
   *
   * @param address The address of the previously allocated memory block.
   * @param size The new size of memory to allocate in bytes.
   * @return The address of the reallocated memory block.
   * @see sun.misc.Unsafe#reallocateMemory
   */
  fun reallocateMemory(address: Long, size: Long): Long = kflect {
    unsafe("reallocateMemory", address, size) as Long
  }

  /**
   * Frees a block of memory at the given address.
   *
   * This memory block must have been previously allocated with {@link #allocateMemory}.
   * The memory is not automatically garbage-collected and must be manually freed
   * when no longer needed.
   *
   * @param address The address of the memory block to free.
   * @see sun.misc.Unsafe#freeMemory
   */
  fun freeMemory(address: Long): Unit = kflect {
    unsafe("freeMemory", address)
  }

  /**
   * Atomically updates an integer field at a given memory offset with a new value if it currently
   * holds the expected value.
   *
   * This method is typically used to implement lock-free data structures and other concurrent
   * programming techniques.
   *
   * @param o The object containing the field to update.
   * @param offset The memory offset of the field.
   * @param expected The expected current value of the field.
   * @param x The new value to set.
   * @return `true` if the update was successful; `false` otherwise.
   * @see sun.misc.Unsafe#compareAndSwapInt
   */
  fun compareAndSwapInt(o: Any?, offset: Long, expected: Int, x: Int): Boolean = kflect {
    unsafe("compareAndSwapInt", o, offset, expected, x) as Boolean
  }

  /**
   * Atomically updates a long field at a given memory offset with a new value if it currently
   * holds the expected value.
   *
   * This method is typically used to implement lock-free data structures and other concurrent
   * programming techniques.
   *
   * @param o The object containing the field to update.
   * @param offset The memory offset of the field.
   * @param expected The expected current value of the field.
   * @param x The new value to set.
   * @return `true` if the update was successful; `false` otherwise.
   * @see sun.misc.Unsafe#compareAndSwapLong
   */
  fun compareAndSwapLong(o: Any?, offset: Long, expected: Long, x: Long): Boolean = kflect {
    unsafe("compareAndSwapLong", o, offset, expected, x) as Boolean
  }

  /**
   * Atomically updates an object field at a given memory offset with a new value if it currently
   * holds the expected value.
   *
   * This method is typically used to implement lock-free data structures and other concurrent
   * programming techniques.
   *
   * @param o The object containing the field to update.
   * @param offset The memory offset of the field.
   * @param expected The expected current value of the field.
   * @param x The new value to set.
   * @return `true` if the update was successful; `false` otherwise.
   * @see sun.misc.Unsafe#compareAndSwapObject
   */
  fun compareAndSwapObject(o: Any?, offset: Long, expected: Any?, x: Any?): Boolean = kflect {
    unsafe("compareAndSwapObject", o, offset, expected, x) as Boolean
  }

  /**
   * Reads an `int` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `int` value at the specified offset.
   * @see sun.misc.Unsafe#getInt
   */
  fun getInt(o: Any?, offset: Long): Int = kflect {
    unsafe("getInt", o, offset) as Int
  }

  /**
   * Writes an `int` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `int` value to set.
   * @see sun.misc.Unsafe#putInt
   */
  fun putInt(o: Any?, offset: Long, x: Int): Unit = kflect {
    unsafe("putInt", o, offset, x)
  }

  /**
   * Reads an `Object` from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The object at the specified offset.
   * @see sun.misc.Unsafe#getObject
   */
  fun getObject(o: Any?, offset: Long): Any? = kflect {
    unsafe("getObject", o, offset)
  }

  /**
   * Reads a `boolean` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `boolean` value at the specified offset.
   * @see sun.misc.Unsafe#getBoolean
   */
  fun getBoolean(o: Any?, offset: Long): Boolean = kflect {
    unsafe("getBoolean", o, offset) as Boolean
  }

  /**
   * Writes a `boolean` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `boolean` value to set.
   * @see sun.misc.Unsafe#putBoolean
   */
  fun putBoolean(o: Any?, offset: Long, x: Boolean): Unit = kflect {
    unsafe("putBoolean", o, offset, x)
  }

  /**
   * Reads a `byte` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `byte` value at the specified offset.
   * @see sun.misc.Unsafe#getByte
   */
  fun getByte(o: Any?, offset: Long): Byte = kflect {
    unsafe("getByte", o, offset) as Byte
  }

  /**
   * Writes a `byte` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `byte` value to set.
   * @see sun.misc.Unsafe#putByte
   */
  fun putByte(o: Any?, offset: Long, x: Byte): Unit = kflect {
    unsafe("putByte", o, offset, x)
  }

  /**
   * Reads a `short` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `short` value at the specified offset.
   * @see sun.misc.Unsafe#getShort
   */
  fun getShort(o: Any?, offset: Long): Short = kflect {
    unsafe("getShort", o, offset) as Short
  }

  /**
   * Writes a `short` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `short` value to set.
   * @see sun.misc.Unsafe#putShort
   */
  fun putShort(o: Any?, offset: Long, x: Short): Unit = kflect {
    unsafe("putShort", o, offset, x)
  }

  /**
   * Reads a `char` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `char` value at the specified offset.
   * @see sun.misc.Unsafe#getChar
   */
  fun getChar(o: Any?, offset: Long): Char = kflect {
    unsafe("getChar", o, offset) as Char
  }

  /**
   * Writes a `char` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `char` value to set.
   * @see sun.misc.Unsafe#putChar
   */
  fun putChar(o: Any?, offset: Long, x: Char): Unit = kflect {
    unsafe("putChar", o, offset, x)
  }

  /**
   * Reads a `long` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `long` value at the specified offset.
   * @see sun.misc.Unsafe#getLong
   */
  fun getLong(o: Any?, offset: Long): Long = kflect {
    unsafe("getLong", o, offset) as Long
  }

  /**
   * Writes a `long` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `long` value to set.
   * @see sun.misc.Unsafe#putLong
   */
  fun putLong(o: Any?, offset: Long, x: Long): Unit = kflect {
    unsafe("putLong", o, offset, x)
  }

  /**
   * Reads a `float` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `float` value at the specified offset.
   * @see sun.misc.Unsafe#getFloat
   */
  fun getFloat(o: Any?, offset: Long): Float = kflect {
    unsafe("getFloat", o, offset) as Float
  }

  /**
   * Writes a `float` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `float` value to set.
   * @see sun.misc.Unsafe#putFloat
   */
  fun putFloat(o: Any?, offset: Long, x: Float): Unit = kflect {
    unsafe("putFloat", o, offset, x)
  }

  /**
   * Reads a `double` value from a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `double` value at the specified offset.
   * @see sun.misc.Unsafe#getDouble
   */
  fun getDouble(o: Any?, offset: Long): Double = kflect {
    unsafe("getDouble", o, offset) as Double
  }

  /**
   * Writes a `double` value to a given object at the specified memory offset.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `double` value to set.
   * @see sun.misc.Unsafe#putDouble
   */
  fun putDouble(o: Any?, offset: Long, x: Double): Unit = kflect {
    unsafe("putDouble", o, offset, x)
  }

  /**
   * Reads an `Object` from a given object at the specified memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The object at the specified offset.
   * @see sun.misc.Unsafe#getObjectVolatile
   */
  fun getObjectVolatile(o: Any?, offset: Long): Any? = kflect {
    unsafe("getObjectVolatile", o, offset)
  }

  /**
   * Reads an `int` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `int` value at the specified offset.
   * @see sun.misc.Unsafe#getIntVolatile
   */
  fun getIntVolatile(o: Any?, offset: Long): Int = kflect {
    unsafe("getIntVolatile", o, offset) as Int
  }

  /**
   * Writes an `int` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `int` value to set.
   * @see sun.misc.Unsafe#putIntVolatile
   */
  fun putIntVolatile(o: Any?, offset: Long, x: Int): Unit = kflect {
    unsafe("putIntVolatile", o, offset, x)
  }

  /**
   * Reads a `boolean` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `boolean` value at the specified offset.
   * @see sun.misc.Unsafe#getBooleanVolatile
   */
  fun getBooleanVolatile(o: Any?, offset: Long): Boolean = kflect {
    unsafe("getBooleanVolatile", o, offset) as Boolean
  }

  /**
   * Writes a `boolean` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `boolean` value to set.
   * @see sun.misc.Unsafe#putBooleanVolatile
   */
  fun putBooleanVolatile(o: Any?, offset: Long, x: Boolean): Unit = kflect {
    unsafe("putBooleanVolatile", o, offset, x)
  }

  /**
   * Reads a `byte` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `byte` value at the specified offset.
   * @see sun.misc.Unsafe#getByteVolatile
   */
  fun getByteVolatile(o: Any?, offset: Long): Byte = kflect {
    unsafe("getByteVolatile", o, offset) as Byte
  }

  /**
   * Writes a `byte` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `byte` value to set.
   * @see sun.misc.Unsafe#putByteVolatile
   */
  fun putByteVolatile(o: Any?, offset: Long, x: Byte): Unit = kflect {
    unsafe("putByteVolatile", o, offset, x)
  }

  /**
   * Reads a `short` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `short` value at the specified offset.
   * @see sun.misc.Unsafe#getShortVolatile
   */
  fun getShortVolatile(o: Any?, offset: Long): Short = kflect {
    unsafe("getShortVolatile", o, offset) as Short
  }

  /**
   * Writes a `short` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `short` value to set.
   * @see sun.misc.Unsafe#putShortVolatile
   */
  fun putShortVolatile(o: Any?, offset: Long, x: Short): Unit = kflect {
    unsafe("putShortVolatile", o, offset, x)
  }

  /**
   * Reads a `char` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `char` value at the specified offset.
   * @see sun.misc.Unsafe#getCharVolatile
   */
  fun getCharVolatile(o: Any?, offset: Long): Char = kflect {
    unsafe("getCharVolatile", o, offset) as Char
  }

  /**
   * Writes a `char` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `char` value to set.
   * @see sun.misc.Unsafe#putCharVolatile
   */
  fun putCharVolatile(o: Any?, offset: Long, x: Char): Unit = kflect {
    unsafe("putCharVolatile", o, offset, x)
  }

  /**
   * Reads a `long` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `long` value at the specified offset.
   * @see sun.misc.Unsafe#getLongVolatile
   */
  fun getLongVolatile(o: Any?, offset: Long): Long = kflect {
    unsafe("getLongVolatile", o, offset) as Long
  }

  /**
   * Writes a `long` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `long` value to set.
   * @see sun.misc.Unsafe#putLongVolatile
   */
  fun putLongVolatile(o: Any?, offset: Long, x: Long): Unit = kflect {
    unsafe("putLongVolatile", o, offset, x)
  }

  /**
   * Reads a `float` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `float` value at the specified offset.
   * @see sun.misc.Unsafe#getFloatVolatile
   */
  fun getFloatVolatile(o: Any?, offset: Long): Float = kflect {
    unsafe("getFloatVolatile", o, offset) as Float
  }

  /**
   * Writes a `float` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `float` value to set.
   * @see sun.misc.Unsafe#putFloatVolatile
   */
  fun putFloatVolatile(o: Any?, offset: Long, x: Float): Unit = kflect {
    unsafe("putFloatVolatile", o, offset, x)
  }

  /**
   * Reads a `double` value from a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @return The `double` value at the specified offset.
   * @see sun.misc.Unsafe#getDoubleVolatile
   */
  fun getDoubleVolatile(o: Any?, offset: Long): Double = kflect {
    unsafe("getDoubleVolatile", o, offset) as Double
  }

  /**
   * Writes a `double` value to a given object at the specified
   * memory offset using volatile semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `double` value to set.
   * @see sun.misc.Unsafe#putDoubleVolatile
   */
  fun putDoubleVolatile(o: Any?, offset: Long, x: Double): Unit = kflect {
    unsafe("putDoubleVolatile", o, offset, x)
  }

  /**
   * Writes an `Object` to a given object at the specified memory offset using ordered semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The object to set.
   * @see sun.misc.Unsafe#putOrderedObject
   */
  fun putOrderedObject(o: Any?, offset: Long, x: Any?): Unit = kflect {
    unsafe("putOrderedObject", o, offset, x)
  }

  /**
   * Writes an `int` value to a given object at the specified memory offset using ordered semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `int` value to set.
   * @see sun.misc.Unsafe#putOrderedInt
   */
  fun putOrderedInt(o: Any?, offset: Long, x: Int): Unit = kflect {
    unsafe("putOrderedInt", o, offset, x)
  }

  /**
   * Writes a `long` value to a given object at the specified memory offset using ordered semantics.
   *
   * @param o The object containing the field.
   * @param offset The memory offset of the field.
   * @param x The `long` value to set.
   * @see sun.misc.Unsafe#putOrderedLong
   */
  fun putOrderedLong(o: Any?, offset: Long, x: Long): Unit = kflect {
    unsafe("putOrderedLong", o, offset, x)
  }

  /**
   * Reads a `long` value from a given memory address.
   *
   * @param address The memory address to read from.
   * @return The `long` value at the specified address.
   * @see sun.misc.Unsafe#getAddress
   */
  fun getAddress(address: Long): Long = kflect {
    unsafe("getAddress", address) as Long
  }

  /**
   * Writes a `long` value to a given memory address.
   *
   * @param address The memory address to write to.
   * @param x The `long` value to set.
   * @see sun.misc.Unsafe#putAddress
   */
  fun putAddress(address: Long, x: Long): Unit = kflect {
    unsafe("putAddress", address, x)
  }

  /**
   * Parks the current thread.
   *
   * @param isAbsolute  If true, the time value is absolute; otherwise, it's relative.
   * @param time        The amount of time, in nanoseconds or milliseconds
   *                    (depending on `isAbsolute`), to park the thread.
   * @see sun.misc.Unsafe#park
   */
  fun park(isAbsolute: Boolean, time: Long): Unit = kflect {
    unsafe("park", isAbsolute, time)
  }

  /**
   * Unparks the specified thread.
   *
   * @param thread The thread to unpark.
   * @see sun.misc.Unsafe#unpark
   */
  fun unpark(thread: Thread?): Unit = kflect {
    unsafe("unpark", thread)
  }

  /**
   * Gets the system load average.
   *
   * @param loadavg A double array to store the load averages.
   * @param nelems The number of elements to retrieve.
   * @return The number of elements that were successfully retrieved.
   * @see sun.misc.Unsafe#getLoadAverage
   */
  fun getLoadAverage(loadavg: DoubleArray, nelems: Int): Int = kflect {
    unsafe("getLoadAverage", loadavg, nelems) as Int
  }

  /**
   * Throws an exception without requiring the exception to be declared in a method's throws clause.
   *
   * @param e The exception to throw.
   * @see sun.misc.Unsafe#throwException
   */
  fun throwException(e: Throwable): Unit = kflect {
    unsafe("throwException", e)
  }

  /**
   * Acquires a lock on the object monitor of the specified object.
   *
   * @param o The object whose monitor is to be entered.
   * @see sun.misc.Unsafe#monitorEnter
   */
  fun monitorEnter(o: Any?): Unit = kflect {
    unsafe("monitorEnter", o)
  }

  /**
   * Releases a lock on the object monitor of the specified object.
   *
   * @param o The object whose monitor is to be exited.
   * @see sun.misc.Unsafe#monitorExit
   */
  fun monitorExit(o: Any?): Unit = kflect {
    unsafe("monitorExit", o)
  }

  /**
   * Tries to acquire a lock on the object monitor of the specified object.
   *
   * @param o The object whose monitor is to be entered.
   * @return True if the lock was acquired; false otherwise.
   * @see sun.misc.Unsafe#tryMonitorEnter
   */
  fun tryMonitorEnter(o: Any?): Boolean = kflect {
    unsafe("tryMonitorEnter", o) as Boolean
  }

  /**
   * Gets the base object containing the given static field.
   *
   * @param f The field whose base object is required.
   * @return The base object that contains the field.
   * @see sun.misc.Unsafe#staticFieldBase
   */
  fun staticFieldBase(f: Field): Any? = kflect {
    unsafe("staticFieldBase", f)
  }

  /**
   * Defines a new class.
   *
   * @param name The name of the class.
   * @param b The bytecode array for the class.
   * @param off The start offset of the bytecode array.
   * @param len The length of the bytecode array.
   * @param loader The class loader to define the class in.
   * @param protectionDomain The protection domain of the class.
   * @return The defined class.
   * @see sun.misc.Unsafe#defineClass
   */
  fun defineClass(
    name: String,
    b: ByteArray,
    off: Int,
    len: Int,
    loader: ClassLoader?,
    protectionDomain: ProtectionDomain?
  ): Class<*> = kflect {
    unsafe("defineClass", name, b, off, len, loader, protectionDomain) as Class<*>
  }

  /**
   * Ensures that the given class has been initialized.
   *
   * @param c The class to initialize.
   * @see sun.misc.Unsafe#ensureClassInitialized
   */
  fun ensureClassInitialized(c: Class<*>): Unit = kflect {
    unsafe("ensureClassInitialized", c)
  }

  /**
   * Allocates an instance of the specified class without invoking any constructor.
   *
   * @param cls The class to allocate an instance of.
   * @return The newly allocated instance.
   * @see sun.misc.Unsafe#allocateInstance
   */
  fun allocateInstance(cls: Class<*>): Any? = kflect {
    unsafe("allocateInstance", cls)
  }

  /**
   * Establishes a full memory barrier, ensuring that all loads and stores
   * before the fence are completed.
   *
   * @see sun.misc.Unsafe#fullFence
   */
  fun fullFence(): Unit = kflect {
    unsafe("fullFence")
  }

  /**
   * Establishes a load memory barrier, ensuring that all loads before the fence are completed.
   *
   * @see sun.misc.Unsafe#loadFence
   */
  fun loadFence(): Unit = kflect {
    unsafe("loadFence")
  }

  /**
   * Establishes a store memory barrier, ensuring that all stores before the fence are completed.
   *
   * @see sun.misc.Unsafe#storeFence
   */
  fun storeFence(): Unit = kflect {
    unsafe("storeFence")
  }
}
