package com.yiting.concollection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import com.yiting.collection.MIterator;
import com.yiting.collection.MAbstractQueue;
import com.yiting.collection.MCollection;
import com.yiting.concurrent.locks.MCondition;
import com.yiting.concurrent.locks.MReentrantLock;

public class MArrayBlockingQueue<E> extends MAbstractQueue<E> implements
		MBlockingQueue<E>, Serializable {

	private static final long serialVersionUID = -2491321054704311753L;
	final Object[] items;
	int takeIndex;
	int putIndex;
	int count;

	final MReentrantLock lock;
	private final MCondition notEmpty;
	private final MCondition notFull;

	public MArrayBlockingQueue(int capacity) {
		this(capacity, false);
	}

	public MArrayBlockingQueue(int capacity, boolean fair) {
		if (capacity < 0) {
			throw new IllegalArgumentException("capacity" + capacity);
		}
		this.items = new Object[capacity];
		lock = new MReentrantLock(fair);
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();
	}

	public MArrayBlockingQueue(int capacity, boolean fair,
			MCollection<? extends E> c) {
		this(capacity, fair);
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			int i = 0;
			try {
				for (E e : c) {
					checkNotNull(e);
					items[i++] = e;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new IllegalArgumentException(e);
			}
			count = i;
			putIndex = (i == capacity) ? 0 : i;
		} finally {
			lock.unLock();
		}
	}

	final int inc(int i) {
		return (++i == items.length) ? 0 : 1;
	}

	final int dec(int i) {
		return (i == 0) ? items.length - 1 : i - 1;
	}

	@SuppressWarnings("unchecked")
	static <E> E cast(Object item) {
		return (E) item;
	}

	/**
	 * final E itemAt(int i) { return this.<E>cast(items[i]); }
	 * 
	 * @param i
	 * @return
	 */

	final E itemAt(int i) {
		return cast(items[i]);
	}

	private static void checkNotNull(Object v) {
		if (v == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Inserts element at current put position, advances, and signals. Call only
	 * when holding lock.
	 */
	private void insert(E x) {
		items[putIndex] = x;
		putIndex = inc(putIndex);
		++count;
		notEmpty.signal();
	}

	/**
	 * Extracts element at current take position, advances, and signals. Call
	 * only when holding lock.
	 */
	private E extract() {
		final Object[] items = this.items;
		E x = cast(items[takeIndex]);
		items[takeIndex] = null;
		takeIndex = inc(takeIndex);
		--count;
		notFull.signal();
		return x;
	}

	public boolean add(E e) {
		return super.add(e);
	}

	/**
	 * 属于立即失败型，当数组中不存在元素时 立即失败
	 * 
	 * @return
	 */
	@Override
	public E poll() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count == 0) {
				return null;
			}
			return extract();
		} finally {
			lock.unLock();
		}
	}

	@Override
	public E peek() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (count == 0) ? null : itemAt(takeIndex);
		} finally {
			lock.unLock();
		}
	}

	/**
	 * Inserts the specified element at the tail of this queue if it is possible
	 * to do so immediately without exceeding the queue's capacity, returning
	 * {@code true} upon success and {@code false} if this queue is full. /
	 */
	@Override
	public boolean offer(E e) {
		checkNotNull(e);
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count == items.length) {
				return false;
			} else {
				insert(e);
				return true;
			}
		} finally {
			lock.unLock();
		}
	}

	/**
	 * Inserts the specified element at the tail of this queue, waiting for
	 * space to become available if the queue is full.
	 */
	@Override
	public void put(E e) throws InterruptedException {
		checkNotNull(e);
		final MReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == items.length) {
				notFull.await(); // 必须用循环 因为await后不一定当前线程能抢到锁
			}
			insert(e);
		} finally {
			lock.unLock();
		}

	}

	/**
	 * Inserts the specified element at the tail of this queue, waiting up to
	 * the specified wait time for space to become available if the queue is
	 * full.
	 */
	@Override
	public boolean offer(E e, long timeout, TimeUnit unit)
			throws InterruptedException {

		checkNotNull(e);
		long nanos = unit.toNanos(timeout);
		final MReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == items.length) {
				if (nanos < 0) {
					return false;
				}
				nanos = notFull.awaitNanos(nanos); // 获取每次的等待时间，从而计算出剩余时间
			}
			insert(e);
			return true;
		} finally {
			lock.unLock();
		}
	}

	@Override
	public E take() throws InterruptedException {
		final MReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == 0) {
				notEmpty.await();
			}
			return extract();
		} finally {
			lock.unLock();
		}

	}

	/**
	 * 从数组中弹出元素，当等待一定时间队列还是满的情况下 返回null
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final MReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			while (count == 0) {
				if (nanos < 0) {
					return null;
				}
				nanos = notEmpty.awaitNanos(nanos);
			}
			return extract();
		} finally {
			lock.unLock();
		}
	}

	@Override
	public int remainingCapacity() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			return items.length - count;
		} finally {
			lock.unLock();
		}
	}

	/**
	 * Deletes item at position i. Utility for remove and iterator.remove. Call
	 * only when holding lock.
	 */
	void removeAt(int i) {
		final Object[] items = this.items;
		if (i == takeIndex) {
			items[takeIndex] = null;
			takeIndex = inc(takeIndex);
		} else {
			for (;;) {
				int nexti = inc(i);
				if (nexti != putIndex) {
					items[i] = items[nexti];
					i = nexti;
				} else {
					items[i] = null;
					putIndex = i;
					break;
				}
			}
		}
		--count;
		notFull.signal();
	}

	/**
	 * Removes a single instance of the specified element from this queue, if it
	 * is present. More formally, removes an element {@code e} such that
	 * {@code o.equals(e)}, if this queue contains one or more such elements.
	 */
	public boolean remove(Object o) {
		checkNotNull(o);
		final MReentrantLock lock = this.lock;
		Object[] items = this.items;
		lock.lock();
		try {
			for (int i = takeIndex, k = count; k > 0; i = inc(i), k--) {
				if (o.equals(items[i])) {
					removeAt(i);
					return true;
				}
			}
			return false;
		} finally {
			lock.unLock();
		}

	}

	@Override
	public boolean contains(Object o) {
		checkNotNull(o);
		Object[] items = this.items;
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			for (int i = takeIndex, k = count; k > 0; i = inc(i), k--) {
				if (o.equals(items[i])) {
					return true;
				}
			}
			return false;
		} finally {
			lock.unLock();
		}
	}

	@Override
	public Object[] toArray() {
		final MReentrantLock lock = this.lock;
		Object[] items = this.items;
		lock.lock();
		try {
			Object[] ret = new Object[count];
			for (int i = takeIndex, k = 0; k < count; i = inc(i), k++) {
				ret[k] = items[i];
			}
			return ret;
		} finally {
			lock.unLock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		final MReentrantLock lock = this.lock;
		Object[] items = this.items;
		lock.lock();
		try {
			int count = this.count;
			int length = a.length;
			if (count > length) {
				a = (T[]) Array.newInstance(a.getClass().getComponentType(),
						count);
			}
			for (int i = takeIndex, k = 0; k < count; i = inc(i), k++) {
				a[k] = (T) items[i];
			}
			if (length > count) {
				a[count] = null;
			}
			return a;
		} finally {
			lock.unLock();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public int drainTo(MCollection<? super E> c) {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			int k = 0;
			for (int i = takeIndex; k < count; k++, i = inc(i)) {
				c.add((E) cast(items[i]));
				items[i] = null;
			}
			if (k > 0) {
				count = 0;
				takeIndex = 0;
				putIndex = 0;
				notFull.signalAll();
			}
			return k;
		} finally {
			lock.unLock();
		}

	}

	@SuppressWarnings("unchecked")
	public int drainTo(MCollection<? super E> c, int maxElements) {
		checkNotNull(c);
		if (c == this)
			throw new IllegalArgumentException();
		if (maxElements <= 0)
			return 0;
		final Object[] items = this.items;
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			int i = takeIndex;
			int n = 0;
			int max = (maxElements < count) ? maxElements : count;
			while (n < max) {
				c.add((E) cast(items[i]));
				items[i] = null;
				i = inc(i);
				++n;
			}
			if (n > 0) {
				count -= n;
				takeIndex = i;
				notFull.signalAll();
			}
			return n;
		} finally {
			lock.unLock();
		}
	}

	@Override
	public int size() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			return count;
		} finally {
			lock.unLock();
		}
	}

	@Override
	public MIterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			int k = count;
			if (k == 0)
				return "[]";

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (int i = takeIndex;; i = inc(i)) {
				Object e = items[i];
				sb.append(e == this ? "(this Collection)" : e);
				if (--k == 0)
					return sb.append(']').toString();
				sb.append(',').append(' ');
			}
		} finally {
			lock.unLock();
		}
	}

	public void clear() {
		final Object[] items = this.items;
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			for (int i = takeIndex, k = count; k > 0; i = inc(i), k--)
				items[i] = null;
			count = 0;
			putIndex = 0;
			takeIndex = 0;
			notFull.signalAll();
		} finally {
			lock.unLock();
		}
	}

	private class Itr implements MIterator<E> {
		private int remaining;
		private int nextIndex;
		private E nextItem;
		private E lastItem;
		private int lastRet;

		public Itr() {
			final MReentrantLock lock = MArrayBlockingQueue.this.lock;
			lock.lock();
			try {
				lastRet = -1;
				if ((remaining = count) > 0) {
					nextIndex = takeIndex;
					nextItem = itemAt(nextIndex);
				}
			} finally {
				lock.unLock();
			}
		}

		@Override
		public boolean hasNext() {
			return remaining > 0;
		}

		/**
		 * 处于并发的考虑 当用户构造完遍历子Itr，此时数组已经发生变化， 该方法强制返回旧的nextItem，然后跳过所有的空值，
		 * 同时用户每次遍历一次 数组都可能发生了变化
		 */
		@Override
		public E next() {
			final MReentrantLock lock = MArrayBlockingQueue.this.lock;
			lock.lock();
			try {
				if (remaining < 0) {
					throw new NoSuchElementException();
				}
				lastRet = nextIndex;
				E x = itemAt(nextIndex);
				if (x == null) {
					x = nextItem; // we are forced to report old value
					lastItem = null;
				} else {
					lastItem = x;
				}
				while (--remaining > 0
						&& (nextItem = itemAt(nextIndex = inc(nextIndex))) == null) {
					;
				}
				return x;
			} finally {
				lock.unLock();
			}

		}

		@Override
		public void remove() {
			final MReentrantLock lock = MArrayBlockingQueue.this.lock;
			lock.lock();
			try {
				int i = lastRet;
				if (i == -1)
					throw new IllegalStateException();
				lastRet = -1;
				E x = lastItem;
				lastItem = null;
				// only remove if item still at index
				if (x != null && x == items[i]) {
					boolean removingHead = (i == takeIndex);
					removeAt(i);
					if (!removingHead)
						nextIndex = dec(nextIndex);
				}
			} finally {
				lock.unLock();
			}
		}

	}
}
