package com.yiting.concollection;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.yiting.collection.Iterator;
import com.yiting.collection.MAbstractQueue;
import com.yiting.collection.MCollection;
import com.yiting.concurrent.locks.MCondition;
import com.yiting.concurrent.locks.MReentrantLock;

public class MArrayBlockingQueue<E> extends MAbstractQueue<E> implements
		MBlockingQueue<E>, Serializable {

	private static final long serialVersionUID = -2491321054704311753L;
	final Object[] items;
	int takeIndex;
	int putindex;
	int count;

	final MReentrantLock lock;
	private final MCondition notEmpty;
	private final MCondition notfull;

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
		notfull = lock.newCondition();
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
			putindex = (i == capacity) ? 0 : i;
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
		items[putindex] = x;
		putindex = inc(putindex);
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
		notfull.signal();
		return x;
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
				if (nexti != putindex) {
					items[i] = items[nexti];
					i = nexti;
				} else {
					items[i] = null;
					putindex = i;
					break;
				}
			}
		}
		--count;
		notfull.signal();
	}

	public boolean add(E e) {
		return super.add(e);
	}

	@Override
	public E poll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E peek() {
		// TODO Auto-generated method stub
		return null;
	}

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

	@Override
	public void put(E e) throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E take() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int remainingCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean remove(Object o) {
		return false;
	}

	@Override
	public int drainTo(MCollection<? super E> c) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int drainTo(MCollection<? super E> c, int maxElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}
