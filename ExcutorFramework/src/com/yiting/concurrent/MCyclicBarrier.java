package com.yiting.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.yiting.concurrent.locks.MCondition;
import com.yiting.concurrent.locks.MReentrantLock;

public class MCyclicBarrier {

	private static class Generation {
		boolean broken = false;
	}

	private final MReentrantLock lock = new MReentrantLock();
	private final MCondition trip = lock.newCondition();
	private final int parties;
	private final Runnable barrierCommand;
	private Generation generation = new Generation();

	private int count;

	private void nextGeneration() {
		trip.signalAll();
		count = parties;
		generation = new Generation();
	}

	private void breakBarrier() {
		generation.broken = true;
		count = parties;
		trip.signalAll();
	}

	private int dowait(boolean timed, long nanos) throws InterruptedException,
			BrokenBarrierException, TimeoutException {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			final Generation g = this.generation;
			if (g.broken) {
				throw new BrokenBarrierException();
			}
			if (Thread.interrupted()) {
				breakBarrier();
				throw new InterruptedException();
			}
			int index = --count;
			if (index == 0) {
				boolean ranAction = false;
				try {
					final Runnable command = barrierCommand;
					if (command != null) {
						command.run();
					}
					ranAction = true;
					nextGeneration();
					return 0;
				} finally {
					if (!ranAction) {
						breakBarrier();
					}
				}
			}

			for (;;) {
				try {
					if (!timed) {
						trip.await();
					} else if (nanos > 0L) {
						nanos = trip.awaitNanos(nanos);
					}

				} catch (InterruptedException ie) {
					if (g == generation && g.broken) {
						breakBarrier();
						throw ie;
					} else {
						Thread.currentThread().interrupt();
					}
				}
				if (g.broken) {
					throw new BrokenBarrierException();
				}
				if (g != generation) {
					return index;
				}
				if (timed && nanos <= 0L) {
					breakBarrier();
					throw new TimeoutException();
				}
			}

		} finally {
			lock.lock();
		}
	}

	public MCyclicBarrier(int parties, Runnable barrierAction) {
		if (parties <= 0)
			throw new IllegalArgumentException();
		this.parties = parties;
		this.count = parties;
		this.barrierCommand = barrierAction;
	}

	public MCyclicBarrier(int parties) {
		this(parties, null);
	}

	public int getParties() {
		return parties;
	}

	public int await() throws InterruptedException, BrokenBarrierException {
		try {
			return dowait(false, 0L);
		} catch (TimeoutException toe) {
			throw new Error(toe); // cannot happen;
		}
	}

	public int await(long timeout, TimeUnit unit) throws InterruptedException,
			BrokenBarrierException, TimeoutException {
		return dowait(true, unit.toNanos(timeout));
	}

	public boolean isBroken() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			return generation.broken;
		} finally {
			lock.unLock();
		}
	}

	public int getNumberWaiting() {
		final MReentrantLock lock = this.lock;
		lock.lock();
		try {
			return parties - count;
		} finally {
			lock.unLock();
		}
	}

}
