package com.yiting.atomic;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import sun.misc.Unsafe;

public class MAtomicReferenceArray<E> implements Serializable {
	private static final long serialVersionUID = 7862455562225529585L;
	public static final Unsafe unsafe;
	public static final int base;
	public static final int shift;
	public static final long objectOffest;
	public final Object[] array;

	static {
		int scale;
		try {
			unsafe = getUnsafe();
			base = unsafe.arrayBaseOffset(Object[].class);
			scale = unsafe.arrayIndexScale(Object[].class);

			objectOffest = unsafe.objectFieldOffset(MAtomicReferenceArray.class
					.getDeclaredField("array"));
		} catch (Exception e) {
			throw new Error(e);
		}
		if ((scale & (scale - 1)) != 0) {
			throw new Error("the scale is not a power of 2");
		}
		shift = 32 - Integer.numberOfLeadingZeros(scale);

	}

	public static final Unsafe getUnsafe() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = Unsafe.class.getField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe) field.get(null);

	}

	public long checkByteOffset(int i) {
		if (i < 0 || i >= array.length) {
			throw new IllegalArgumentException("index: " + i);
		}
		return byteOffset(i);
	}

	public static long byteOffset(int i) {
		return base + ((long) i << shift);
	}

	public MAtomicReferenceArray(int n) {
		array = new Object[n];
	}

	public MAtomicReferenceArray(E[] array) {
		this.array = Arrays.copyOf(array, array.length, Object[].class);
	}

	public int length() {
		return array.length;
	}

	public final E get(int i) {
		return getRaw(checkByteOffset(i));
	}

	private E getRaw(long offset) {
		return (E) unsafe.getObjectVolatile(array, offset);
	}

	public final void set(int i, E newValue) {
		unsafe.putObjectVolatile(array, checkByteOffset(i), newValue);
	}

	public final void lazySet(int i, E newValue) {
		unsafe.putOrderedObject(array, checkByteOffset(i), newValue);
	}

	public final  E getAndSet(int i, E newValue) {
		for (;;) {
			E oldValue = get(i);
			if (compareAndSetRaw(checkByteOffset(i), oldValue, newValue)) {
				return oldValue;
			}

		}
	}

	public final boolean compareAndSet(int i, E expect, E update) {
		return compareAndSetRaw(checkByteOffset(i), expect, update);
	}

	private final boolean compareAndSetRaw(long offset, E expect, E update) {
		return unsafe.compareAndSwapObject(array, offset, expect, update);
	}

	public final boolean weakCompareAndSet(int i, E expect, E update){
		return compareAndSetRaw(checkByteOffset(i), expect, update);
	}
	
	 public String toString() {
	        int iMax = array.length - 1;
	        if (iMax == -1)
	            return "[]";

	        StringBuilder b = new StringBuilder();
	        b.append('[');
	        for (int i = 0; ; i++) {
	            b.append(getRaw(byteOffset(i)));
	            if (i == iMax)
	                return b.append(']').toString();
	            b.append(',').append(' ');
	        }
	    }

	  
	    private void readObject(java.io.ObjectInputStream s)
	        throws java.io.IOException, ClassNotFoundException {
	        // Note: This must be changed if any additional fields are defined
	        Object a = s.readFields().get("array", null);
	        if (a == null || !a.getClass().isArray())
	            throw new java.io.InvalidObjectException("Not array type");
	        if (a.getClass() != Object[].class)
	            a = Arrays.copyOf((Object[])a, Array.getLength(a), Object[].class);
	        unsafe.putObjectVolatile(this, objectOffest, a);
	    }

	
}
