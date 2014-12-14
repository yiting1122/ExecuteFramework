package com.yiting.concurrent.locks;

public interface MReadWriteLock {
	/**
	 * 
	 * @return the lock uesd for read
	 */
	MLock readLock();
	
	/**
	 * 
	 * @return the lock used for write
	 */
	MLock writeLock();
}
