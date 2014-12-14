package com.yiting.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class MReentrantLock implements MLock, Serializable {
	private static final long serialVersionUID = 108405511781254683L;
	private final Sync sync;

	abstract static class Sync extends MAbstractQueuedSynchronizer {
		private static final long serialVersionUID = -3120099556774616054L;

		abstract void lock();

		/**
		 * 该方法会首先判断当前状态，如果c==0说明没有线程正在竞争该锁，如果不c !=0 说明有线程正拥有了该锁。
		 * 如果发现c==0，则通过CAS设置该状态值为acquires
		 * ,acquires的初始调用值为1，每次线程重入该锁都会+1，每次unlock都会
		 * -1，但为0时释放锁。如果CAS设置成功，则可以预计其他任何线程调用CAS都不会再成功
		 * ，也就认为当前线程得到了该锁，也作为Running线程，很显然这个Running线程并未进入等待队列。 如果c !=0
		 * 但发现自己已经拥有锁，只是简单地++acquires，并修改status值，但因为没有竞争，所以通过setStatus修改，而非CAS，
		 * 也就是说这段代码实现了偏向锁的功能，并且实现的非常漂亮。
		 * 
		 * @param acquires
		 * @return
		 */
		final boolean nonfairTryAcquire(int acquires) {
			final Thread t = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (compareAndSetState(0, acquires)) {
					setExclusiveOwnerThread(t);
					return true;
				}
			} else if (t == getExclusiveOwnerThread()) {
				int nextc = c + acquires;
				if (nextc < 0) {
					throw new Error("maximum lock count exceeded");
				}
				setState(nextc);
				return true;
			}
			return false;
		}

		protected final boolean tryRelease(int releases) {
			int c = getState() - releases;
			if (Thread.currentThread() != getExclusiveOwnerThread()) {
				throw new IllegalMonitorStateException();
			}
			boolean free = false;
			if (c == 0) {
				free = true;
				setExclusiveOwnerThread(null);
			}
			setState(c);
			return free;
		}

		protected final boolean isHeldExclusively() {
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		final ConditionObject newCondition() {
			return new ConditionObject();
		}

		final Thread getOwner() {
			return getState() == 0 ? null : getExclusiveOwnerThread();
		}

		final int getHoldCount() {
			return isHeldExclusively() ? getState() : 0;
		}

		final boolean isLocked() {
			return getState() != 0;
		}

		private void readObject(ObjectInputStream s)
				throws ClassNotFoundException, IOException {
			s.defaultReadObject();
			setState(0);
		}

	}

	static final class NonfairSync extends Sync {
		private static final long serialVersionUID = -5148821338545954050L;
		private NonfairSync(){
			
		}
		@Override
		final void lock() {
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
			} else {
				acquire(1);
			}
		}

		protected final boolean tryAcquire(int acquires) {
			return nonfairTryAcquire(acquires);
		}

	}

	static final class FairSync extends Sync {

		private static final long serialVersionUID = 5804014761870286686L;

		@Override
		void lock() {
			acquire(1);
		}

		protected final boolean tryAcquire(int acquires) {
			final Thread current = Thread.currentThread();
			int c = getState();
			if (c == 0) {
				if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
					setExclusiveOwnerThread(current);
					return true;
				}
			} else if (current == getExclusiveOwnerThread()) {
				int nextc = c + acquires;
				if (nextc < 0) {
					throw new Error("Maximum lock count exceeded");
				}
				setState(nextc);
				return true;
			}
			return false;
		}
	}

	public MReentrantLock() {
		sync = new NonfairSync();
	}

	public MReentrantLock(boolean fair) {
		sync = fair ? new FairSync() : new NonfairSync();
	}

	@Override
	public void lock() {
		sync.lock();

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);

	}

	@Override
	public boolean tryLock() {

		return sync.nonfairTryAcquire(1);
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

	public int getHoldCount() {
		return sync.getHoldCount();
	}

	public boolean isHeldByCurrentThread() {
		return sync.isHeldExclusively();
	}

	public boolean isLocked() {
		return sync.isLocked();
	}

	public final boolean isFair() {
		return sync instanceof FairSync;
	}

	protected Thread getOwner() {
		return sync.getOwner();
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
		Thread o = sync.getOwner();
		return super.toString()
				+ ((o == null) ? "[Unlocked]" : "[Locked by thread "
						+ o.getName() + "]");
	}

}
