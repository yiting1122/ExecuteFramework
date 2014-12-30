package com.yiting.atomic;

import java.io.Serializable;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

public class MAtomicInteger extends Number implements Serializable {
	private static final long serialVersionUID = -8092305366777404513L;

	private static final Unsafe unsafe ;
	private static final long valueOffset;
	static {
		try {
			unsafe=getUnsafe();
			valueOffset = unsafe.objectFieldOffset(MAtomicInteger.class
					.getDeclaredField("value"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	private volatile int value;

	public static final Unsafe getUnsafe() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = Unsafe.class.getField("theUnsafe");
		field.setAccessible(true);
		return (Unsafe) field.get(null);

	}

	private MAtomicInteger(int initValue) {
		this.value = initValue;
	}

	public MAtomicInteger() {
	}

	public final int get() {
		return value;
	}

	public final void set(int newValue) {
		value = newValue;
	}

	public final void lazSet(int newValue) {
		unsafe.putOrderedInt(this, valueOffset, newValue);
	}

	public final int getAndSet(int newValue) {
		for (;;) {
			int current = get();
			if (compareAndSet(current, newValue)) {
				return current;
			}
		}
	}

	public final boolean compareAndSet(int expect, int update) {
		return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
	}

	/**
	 * Atomically sets the value to the given updated value if the current value
	 * {@code ==} the expected value.
	 * 
	 * <p>
	 * May <a href="package-summary.html#Spurious">fail spuriously</a> and does
	 * not provide ordering guarantees, so is only rarely an appropriate
	 * alternative to {@code compareAndSet}.
	 * 
	 * @param expect
	 *            the expected value
	 * @param update
	 *            the new value
	 * @return true if successful.
	 */
	public final boolean weakCompareAndSet(int expect, int update) {
		return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
	}

	public final int getAndIncrement() {
		for (;;) {
			int current = get();
			if (compareAndSet(current, current + 1)) {
				return current;
			}
		}
	}

	public final int getAndDecrement() {
		for (;;) {
			int current = get();
			if (compareAndSet(current, current - 1)) {
				return current;
			}
		}
	}

	public final int incrementAndGet() {
		for (;;) {
			int current = get();
			int next = current + 1;
			if (compareAndSet(current, next)) {
				return next;
			}
		}
	}

	public final int secrementAndGet() {
		for (;;) {
			int current = get();
			int next = current - 1;
			if (compareAndSet(current, next)) {
				return next;
			}
		}
	}

	public final int addAndGet(int delta) {
		for (;;) {
			int current = get();
			int next = current + delta;
			if (compareAndSet(current, next)) {
				return next;
			}
		}
	}

	@Override
	public String toString() {
		return Integer.toString(get());
	}

	@Override
	public int intValue() {
		return get();
	}

	@Override
	public long longValue() {
		return (long) get();
	}

	@Override
	public float floatValue() {
		return (float) get();
	}

	@Override
	public double doubleValue() {
		return (double) get();
	}

}
