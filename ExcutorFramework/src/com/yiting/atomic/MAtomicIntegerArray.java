package com.yiting.atomic;

import java.io.Serializable;
import sun.misc.Unsafe;

public class MAtomicIntegerArray implements Serializable {
	private static final long serialVersionUID = -2577153656613777889L;

	private static final Unsafe unsafe = Unsafe.getUnsafe();
	private static final int base = unsafe.arrayBaseOffset(int[].class);
	private static final int shift;
	private final int[] array;
	static {
		//获取数组中一个元素的大小(get size of an element in the array)
		int scale = unsafe.arrayIndexScale(int[].class);
		if ((scale & (scale - 1)) != 0) {
			throw new Error("data type scale not a power of two");
		}
		// floor(log2(x)) = 31 - numberOfLeadingZeros(x)
		shift = 31 - Integer.numberOfLeadingZeros(scale);
	}

	private long checkedByteOffset(int i) {
		if (i < 0 || i >= array.length) {
			throw new IndexOutOfBoundsException("index" + i);
		}
		return byteOffset(i);
	}

	private static long byteOffset(int i) {
		return ((long) i << shift) + base;
	}

	public MAtomicIntegerArray(int length) {
		array = new int[length];
	}

	public MAtomicIntegerArray(int[] array) {
		this.array = array.clone();
	}

	public final int length() {
		return array.length;
	}

	public final int get(int i) {
		return getRaw(checkedByteOffset(i));
	}

	private int getRaw(long offset) {
		return unsafe.getIntVolatile(array, offset);
	}

	public final void set(int i, int newValue) {
		unsafe.putIntVolatile(array, checkedByteOffset(i), newValue);
	}

	public final void lazySet(int i, int newValue) {
		unsafe.putOrderedInt(array, checkedByteOffset(i), newValue);
	}

	public final int getAndSet(int i, int newValue) {
		long offset = checkedByteOffset(i);
		while (true) {
			int current = getRaw(offset);
			if (compareAndSetRaw(offset, current, newValue))
				return current;
		}
	}

	public final boolean compareAndSet(int i, int expect, int update) {
		return compareAndSetRaw(checkedByteOffset(i), expect, update);
	}

	private boolean compareAndSetRaw(long offset, int expect, int update) {
		return unsafe.compareAndSwapInt(array, offset, expect, update);
	}

	public final boolean weakCompareAndSet(int i, int expect, int update) {
		return compareAndSet(i, expect, update);
	}

	public final int getAndIncrement(int i) {
		return getAndAdd(i, 1);
	}

	public final int getAndDecrement(int i) {
		return getAndAdd(i, -1);
	}

	public final int getAndAdd(int i, int delta) {
		long offset = checkedByteOffset(i);
		while (true) {
			int current = getRaw(offset);
			if (compareAndSetRaw(offset, current, current + delta))
				return current;
		}
	}

	public final int incrementAndGet(int i) {
		return addAndGet(i, 1);
	}

	public final int decrementAndGet(int i) {
		return addAndGet(i, -1);
	}

	public final int addAndGet(int i, int delta) {
		long offset = checkedByteOffset(i);
		while (true) {
			int current = getRaw(offset);
			int next = current + delta;
			if (compareAndSetRaw(offset, current, next))
				return next;
		}
	}

	public String toString() {
		int iMax = array.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			b.append(getRaw(byteOffset(i)));
			if (i == iMax)
				return b.append(']').toString();
			b.append(',').append(' ');
		}
	}

}
