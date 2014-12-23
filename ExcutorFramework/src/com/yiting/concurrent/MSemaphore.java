package com.yiting.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.yiting.concurrent.locks.MAbstractQueuedSynchronizer;

/**
 * 信号量，控制当前访问线程的个数
 * 
 * @author yiting
 * 
 */

public class MSemaphore implements Serializable {
	private static final long serialVersionUID = -3287838633152854055L;
	private final Sync sync;

	abstract static class Sync extends MAbstractQueuedSynchronizer {
		private static final long serialVersionUID = 5913797436234496349L;

		Sync(int permits) {
			setState(permits);
		}

		final int getPermits() {
			return getState();
		}

		final int nonfairTryAcquireShared(int acquires) {
			for (;;) {
				int available = getState();
				int remaining = available - acquires;
				if (remaining < 0 || compareAndSetState(available, remaining))
					return remaining;
			}
		}

		protected final boolean tryReleaseShared(int releases) {
			for (;;) {
				int current = getState();
				int next = current + releases;
				if (next < current) // overflow
					throw new Error("Maximum permit count exceeded");
				if (compareAndSetState(current, next))
					return true;
			}
		}

		final void reducePermits(int reductions) {
			for (;;) {
				int current = getState();
				int next = current - reductions;
				if (next > current) // underflow
					throw new Error("Permit count underflow");
				if (compareAndSetState(current, next))
					return;
			}
		}

		final int drainPermits() {
			for (;;) {
				int current = getState();
				if (current == 0 || compareAndSetState(current, 0))
					return current;
			}
		}

	}

	/**
	 * NonFair version
	 */
	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -2694183684443567898L;

		NonfairSync(int permits) {
			super(permits);
		}

		protected int tryAcquireShared(int acquires) {
			return nonfairTryAcquireShared(acquires);
		}
	}

	/**
	 * Fair version
	 */
	static final class FairSync extends Sync {
		private static final long serialVersionUID = 2014338818796000944L;

		FairSync(int permits) {
			super(permits);
		}

		protected int tryAcquireShared(int acquires) {
			for (;;) {
				if (hasQueuedPredecessors())
					return -1;
				int available = getState();
				int remaining = available - acquires;
				if (remaining < 0 || compareAndSetState(available, remaining))
					return remaining;
			}
		}
	}

	public MSemaphore(int permits) {
		sync = new NonfairSync(permits);
	}

	public MSemaphore(int permits, boolean fair) {
		sync = fair ? new FairSync(permits) : new NonfairSync(permits);
	}

	
	
	public void acquire() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}

	public void acquireUninterruptibly() {
		sync.acquireShared(1);
	}

	public boolean tryAcquire() {
		return sync.nonfairTryAcquireShared(1) >= 0;
	}

	public boolean tryAcquire(long timeout, TimeUnit unit)
			throws InterruptedException {
		return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
	}

	public void release() {
		sync.releaseShared(1);
	}

	public void acquire(int permits) throws InterruptedException {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.acquireSharedInterruptibly(permits);
	}

	public void acquireUninterruptibly(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.acquireShared(permits);
	}

	public boolean tryAcquire(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		return sync.nonfairTryAcquireShared(permits) >= 0;
	}

	public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (permits < 0)
			throw new IllegalArgumentException();
		return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
	}

	public void release(int permits) {
		if (permits < 0)
			throw new IllegalArgumentException();
		sync.releaseShared(permits);
	}

	public int availablePermits() {
		return sync.getPermits();
	}

	public int drainPermits() {
		return sync.drainPermits();
	}

	protected void reducePermits(int reduction) {
		if (reduction < 0)
			throw new IllegalArgumentException();
		sync.reducePermits(reduction);
	}

	public boolean isFair() {
		return sync instanceof FairSync;
	}

	public final boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	public final int getQueueLength() {
		return sync.getQueueLength();
	}

	protected Collection<Thread> getQueuedThreads() {
		return sync.getQueuedThreads();
	}

	public String toString() {
		return super.toString() + "[Permits = " + sync.getPermits() + "]";
	}

}
