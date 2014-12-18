package com.yiting.concurrent.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.yiting.concurrent.locks.MAbstractQueuedSynchronizer.Node;

public interface MCondition {

	/**
	 * Causes the current thread to wait until it is signalled or
	 * {@linkplain Thread#interrupt interrupted}.
	 */
	public void await() throws InterruptedException;

	/**
	 * Causes the current thread to wait until it is signalled.
	 */
	public void awaitUninterruptibly();
	 
	/**
	 * Causes the current thread to wait until it is signalled or interrupted,
	 * or the specified waiting time elapses.
	 */
	public long awaitNanos(long timeout) throws InterruptedException;

	/**
	 * Causes the current thread to wait until it is signalled or interrupted,
	 * or the specified waiting time elapses. This method is behaviorally
	 * equivalent to:<br>
	 * 
	 * <pre>
	 * awaitNanos(unit.toNanos(time)) &gt; 0
	 */
	public boolean await(long time, TimeUnit unit) throws InterruptedException;

	/**
	 * Causes the current thread to wait until it is signalled or interrupted,
	 * or the specified deadline elapses.
	 */
	boolean awaitUntil(Date deadline) throws InterruptedException;

	/**
	 * Wakes up one waiting thread.
	 */
	public void signal();

	/**
	 * Wakes up all waiting threads.
	 */
	public void signalAll();

}
