package com.yiting.concurrent.locks;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class MReentrantReadWriteLock implements MReadWriteLock, Serializable {

	private static final long serialVersionUID = -3500774438959349277L;

	private final MReentrantReadWriteLock.ReadLock readerLock;
	private final MReentrantReadWriteLock.WriteLock writerLock;
	final Sync sync;

	public MReentrantReadWriteLock() {
		this(false);
	}

	public MReentrantReadWriteLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
		readerLock = new ReadLock(this);
		writerLock = new WriteLock(this);
	}
	
	@Override
	public MLock readLock() {
		return readLock();
	}

	@Override
	public MLock writeLock() {
		return writeLock();
	}

	abstract static class Sync extends MAbstractQueuedSynchronizer {
		private static final long serialVersionUID = -3893973583845301112L;
		static final int SHARED_SHIFT = 16;
		static final int SHARED_UNIT = (1 << SHARED_SHIFT);
		static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
		static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

		static int sharedCount(int c) {
			return c >>> SHARED_SHIFT;
		}

		static int exclusiveCount(int c) {
			return c & EXCLUSIVE_MASK;
		}

		static final class HolderCounter {
			int count = 0;
			final long tid = Thread.currentThread().getId();
		}

		static final class ThreadLocalHoldCounter extends
				ThreadLocal<HolderCounter> {
			public HolderCounter initialValue() {
				return new HolderCounter();
			}
		}

		private transient ThreadLocalHoldCounter readHolds;
		private transient HolderCounter cacheHolderCounter;
		private transient Thread firstReader = null;
		private transient int firstReaderHoldCount;

		Sync() {
			readHolds = new ThreadLocalHoldCounter();
			setState(getState());
		}

		abstract boolean readerShouldBlock();

		abstract boolean writerShouldBlock();

		protected final boolean tryRelease(int release) {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			int nextc = getState() - release;
			boolean free = exclusiveCount(nextc) == 0;
			if (free) {
				setExclusiveOwnerThread(null);
			}
			setState(nextc);
			return free;
		}

		protected final boolean tryAcquire(int acquires) {
			Thread current = Thread.currentThread();
			int c = getState();
			int w = exclusiveCount(c);
			if (c != 0) {
				// (Note: if c != 0 and w == 0 then shared count != 0)
				if (w == 0 || current != getExclusiveOwnerThread()) {
					return false;
				}
				if (w + exclusiveCount(acquires) > MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
				setState(c + acquires);
				return true;
			}
			if (writerShouldBlock() || !compareAndSetState(c, c + acquires)) {
				return false;
			}
			setExclusiveOwnerThread(current);
			return true;

		}

		/**
		 * 这个参数没有使用
		 */
		protected final boolean tryReleaseShared(int unused) {
			Thread current = Thread.currentThread();
			if (firstReader == current) {
				if (firstReaderHoldCount == 1) {
					firstReader = null;
				} else {
					firstReaderHoldCount--;
				}
			} else {
				HolderCounter rh = cacheHolderCounter;
				if (rh == null || rh.tid != current.getId()) {
					rh = readHolds.get();
				}
				int count = rh.count;
				if (count <= 1) { // 当只持有一把锁时把hold删除，如果小于等于0，而此时是不可能出现在threadlocal中，出现则是异常
					readHolds.remove();
					if (count < 0) {
						throw unmatchedUnlockException();
					}
				}
				--rh.count;
			}
			for (;;) {
				int c = getState();
				int nextc = c - SHARED_UNIT;
				if (compareAndSetState(c, nextc)) {
					return nextc == 0;
				}
			}
		}

		protected final int tryAcquireShared(int unused) {
			Thread current = Thread.currentThread();
			int c = getState();
			if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) {
				return -1;
			}
			int r = sharedCount(c);
			if (!readerShouldBlock() && r < MAX_COUNT
					&& compareAndSetState(c, c + SHARED_UNIT)) {
				if (r == 0) {
					firstReader = current;
					firstReaderHoldCount = 1;
				} else if (firstReader == current) {
					firstReaderHoldCount++;
				} else {
					HolderCounter rh = cacheHolderCounter;
					if (rh == null || rh.tid != current.getId()) {
						cacheHolderCounter = rh = readHolds.get();
					} else if (rh.count == 0) {
						readHolds.set(rh);
					}
					rh.count++;
				}
				return 1;
			}
			return fullTryAcquireShared(current);
		}

		final int fullTryAcquireShared(Thread current) {
			HolderCounter rh = null;
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0) {
					if (getExclusiveOwnerThread() != current) {
						return -1;
					}
				} else if (readerShouldBlock()) {
					if (firstReader == current) {
						// assert firstReaderHoldCount > 0;
					} else {
						if (rh == null) {
							rh = cacheHolderCounter;
							if (rh == null || rh.tid != current.getId()) {
								rh = readHolds.get();
								if (rh.count == 0) {
									readHolds.remove();
								}
							}
						}
						if (rh.count == 0) {
							return -1;
						}
					}
				}

				if (sharedCount(c) == MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (sharedCount(c) == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (firstReader == current) {
						firstReaderHoldCount++;
					} else {
						if (rh == null) {
							rh = cacheHolderCounter;
						}
						if (rh == null || rh.tid != current.getId()) {
							rh = readHolds.get();
						} else if (rh.count == 0) {
							readHolds.set(rh);
						}
						rh.count++;
						cacheHolderCounter = rh;
					}
					return 1;
				}

			}
		}

		/**
		 * 写锁只有一把 不需要去处理缓存 缓存是用于读锁的管理
		 * 
		 * @return
		 */
		final boolean tryWriteLock() {
			Thread current = Thread.currentThread();
			int c = getState();
			if (c != 0) {
				int w = exclusiveCount(c);
				if (w == 0 || current != getExclusiveOwnerThread()) {
					return false;
				}
				if (w == MAX_COUNT) {
					throw new Error("Maximum lock count exceeded");
				}
			}
			if (!compareAndSetState(c, c + 1)) {
				return false;
			}
			setExclusiveOwnerThread(current);
			return true;
		}

		final boolean tryReadLock() {
			Thread current = Thread.currentThread();
			for (;;) {
				int c = getState();
				if (exclusiveCount(c) != 0
						&& getExclusiveOwnerThread() != current) {
					return false;
				}
				int r = sharedCount(c);
				if (r == MAX_COUNT) {
					throw new Error("maximum lock count exceeded");
				}
				if (compareAndSetState(c, c + SHARED_UNIT)) {
					if (r == 0) {
						firstReader = current;
						firstReaderHoldCount = 1;
					} else if (current == firstReader) {
						firstReaderHoldCount++;
					} else {
						HolderCounter rh = cacheHolderCounter;
						if (rh == null || rh.tid != current.getId()) {
							cacheHolderCounter = rh = readHolds.get();
						} else if (rh.count == 0) {
							readHolds.set(rh);
						}
						rh.count++;
					}
					return true;
				}
			}
		}

		protected final boolean isHeldExclusively() {
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		final Thread getOwner() {
			return ((exclusiveCount(getState()) == 0 ? null
					: getExclusiveOwnerThread()));
		}

		final int getReadLockCount() {
			return sharedCount(getState());
		}

		final boolean isWriteLocked() {
			return exclusiveCount(getState()) != 0;
		}

		final int getWriteHoldCount() {
			return isHeldExclusively() ? exclusiveCount(getState()) : 0;
		}

		final int getReadHoldCount() {
			if (getReadLockCount() == 0) {
				return 0;
			}
			Thread current = Thread.currentThread();
			if (firstReader == current) {
				return firstReaderHoldCount;
			}
			HolderCounter rh = cacheHolderCounter;
			if (rh != null && rh.tid == current.getId()) {
				return rh.count;
			}
			int count = readHolds.get().count;
			if (count == 0) {
				readHolds.remove();
			}
			return count;
		}

		private void readObject(java.io.ObjectInputStream s)
				throws IOException, ClassNotFoundException {
			s.defaultReadObject();
			readHolds = new ThreadLocalHoldCounter();
			setState(0); // reset to unlocked state
		}

		final int getCount() {
			return getState();
		}

		private IllegalMonitorStateException unmatchedUnlockException() {
			return new IllegalMonitorStateException(
					"attempt to unlock read lock, not locked by current thread");
		}

	}

	/**
	 * 这个非公平策略的同步器是写锁优先的，申请写锁时总是不阻塞。
	 */
	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -8159625535654395037L;

		final boolean writerShouldBlock() {
			return false; // 写线程总是可以突入
		}

		final boolean readerShouldBlock() {
			/*
			 * 作为一个启发用于避免写线程饥饿，如果线程临时出现在等待队列的头部则阻塞， 如果存在这样的，则是写线程。
			 */
			return apparentlyFirstQueuedIsExclusive();
		}
	}

	/**
	 * 公平的 Sync，它的策略是：如果线程准备获取锁时， 同步队列里有等待线程，则阻塞获取锁，不管是否是重入
	 * 这也就需要tryAcqire、tryAcquireShared方法进行处理。
	 */
	static final class FairSync extends Sync {
		private static final long serialVersionUID = -2274990926593161451L;

		final boolean writerShouldBlock() {
			return hasQueuedPredecessors();
		}

		final boolean readerShouldBlock() {
			return hasQueuedPredecessors();
		}
	}

	public static class ReadLock implements MLock, Serializable {
		private static final long serialVersionUID = 992630721005575159L;
		private final Sync sync;

		protected ReadLock(MReentrantReadWriteLock lock) {
			sync = lock.sync;
		}

		@Override
		public void lock() {
			sync.acquireShared(1);
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}

		@Override
		public boolean tryLock() {
			return sync.tryReadLock();
		}

		@Override
		public boolean tryLock(long timeout, TimeUnit unit)
				throws InterruptedException {
			return sync.tryAcquireNanos(1, unit.toNanos(timeout));
		}

		@Override
		public void unLock() {
			sync.release(1);

		}

		@Override
		public MCondition newCondition() {
			throw new UnsupportedOperationException();
		}

		public String toString() {
			int r = sync.getReadLockCount();
			return super.toString() + "[Read locks = " + r + "]";
		}
	}

	public static class WriteLock implements MLock, Serializable {
		private static final long serialVersionUID = 883265991712375279L;

		private final Sync sync;

		protected WriteLock(MReentrantReadWriteLock lock) {
			this.sync = lock.sync;
		}

		@Override
		public void lock() {
			sync.acquire(1);
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			sync.acquireInterruptibly(1);
		}

		@Override
		public boolean tryLock() {
			return sync.tryWriteLock();
		}

		@Override
		public boolean tryLock(long timeout, TimeUnit unit)
				throws InterruptedException {
			return sync.tryAcquireNanos(1, unit.toNanos(timeout));
		}

		@Override
		public void unLock() {
			sync.release(1);
		}

		@Override
		public MCondition newCondition() {
			return sync.newCondition();
		}

		@Override
		public String toString() {
			Thread o = sync.getOwner();
			return super.toString()
					+ ((o == null) ? "[Unlocked]" : "[Locked by thread "
							+ o.getName() + "]");
		}

		public boolean isHeldByCurrentThread() {
			return sync.isHeldExclusively();
		}

		public int getHoldCount() {
			return sync.getWriteHoldCount();
		}

	}

	public final boolean isFair() {
		return sync instanceof FairSync;
	}

	protected Thread getOwner() {
		return sync.getOwner();
	}

	public int getReadLockCount() {
		return sync.getReadLockCount();
	}

	public boolean isWriteLocked() {
		return sync.isWriteLocked();
	}

	public boolean isWriteLockedByCurrentThread() {
		return sync.isHeldExclusively();
	}

	public int getWriteHoldCount() {
		return sync.getWriteHoldCount();
	}

	public int getReadHoldCount() {
		return sync.getReadHoldCount();
	}

	protected Collection<Thread> getQueuedWriterThreads() {
		return sync.getExclusiveQueuedThreads();
	}

	protected Collection<Thread> getQueuedReaderThreads() {
		return sync.getSharedQueuedThreads();
	}

	public final boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	public final boolean hasQueuedThread(Thread thread) {
		return sync.isQueued(thread);
	}

	public final int getQueueLength() {
		return sync.getQueueLength();
	}

	protected Collection<Thread> getQueuedThreads() {
		return sync.getQueuedThreads();
	}

	public boolean hasWaiters(MCondition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof MAbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync
				.hasWaiters((MAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public int getWaitQueueLength(MCondition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof MAbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync
				.getWaitQueueLength((MAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	protected Collection<Thread> getWaitingThreads(MCondition condition) {
		if (condition == null)
			throw new NullPointerException();
		if (!(condition instanceof MAbstractQueuedSynchronizer.ConditionObject))
			throw new IllegalArgumentException("not owner");
		return sync
				.getWaitingThreads((MAbstractQueuedSynchronizer.ConditionObject) condition);
	}

	public String toString() {
		int c = sync.getCount();
		int w = Sync.exclusiveCount(c);
		int r = Sync.sharedCount(c);

		return super.toString() + "[Write locks = " + w + ", Read locks = " + r
				+ "]";
	}

	

}
