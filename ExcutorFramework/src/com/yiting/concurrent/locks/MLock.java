package com.yiting.concurrent.locks;

import java.util.concurrent.TimeUnit;

public interface MLock {
	/**
	 * Acquire the lock if the lock is not available then the current thread
	 * becomes disabled and wait for the lock
	 */
	public void lock();

	/**
	 * Acquires the lock unless the current thread is
	 * {@linkplain Thread#interrupt interrupted}.
	 * 
	 * <p>
	 * Acquires the lock if it is available and returns immediately.
	 * 
	 * <p>
	 * If the lock is not available then the current thread becomes disabled for
	 * thread scheduling purposes and lies dormant until one of two things
	 * happens:
	 * 
	 * <ul>
	 * <li>The lock is acquired by the current thread; or
	 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the
	 * current thread, and interruption of lock acquisition is supported.
	 * </ul>
	 */
	public void lockInterruptibly() throws InterruptedException;

	/**
	 * Acquires the lock only if it is free at the time of invocation.
	 * 
	 * <p>
	 * Acquires the lock if it is available and returns immediately with the
	 * value {@code true}. If the lock is not available then this method will
	 * return immediately with the value {@code false}.
	 */
	public boolean tryLock();
	
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException;
	
	/**
     * Releases the lock.
     */
	public void unLock();
	
	/**
     * Returns a new {@link Condition} instance that is bound to this
     * {@code Lock} instance.
     */
	public MCondition newCondition();
}
