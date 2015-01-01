package com.yiting.concollection;

import java.util.concurrent.TimeUnit;

import com.yiting.collection.MCollection;
import com.yiting.collection.MQueue;

public interface MBlockingQueue<E> extends MQueue<E> {
	public boolean add(E e);

	public boolean offer(E e);

	/**
	 * Inserts the specified element into this queue, waiting if necessary for
	 * space to become available.
	 * 
	 * @param e
	 * @throws InterruptedException
	 */
	public void put(E e) throws InterruptedException;

	/**
	 * Inserts the specified element into this queue, waiting up to the
	 * specified wait time if necessary for space to become available.
	 * 
	 * * @param e the element to add
	 * 
	 * @param timeout
	 *            how long to wait before giving up, in units of <tt>unit</tt>
	 * @param unit
	 *            a <tt>TimeUnit</tt> determining how to interpret the
	 *            <tt>timeout</tt> parameter
	 * @return <tt>true</tt> if successful, or <tt>false</tt> if the specified
	 *         waiting time elapses before space is available
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this queue
	 * @throws NullPointerException
	 *             if the specified element is null
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this queue
	 */
	public boolean offer(E e, long timeout, TimeUnit unit);

	/**
	 * Retrieves and removes the head of this queue, waiting if necessary until
	 * an element becomes available.
	 */
	public E take() throws InterruptedException;

	/**
	 * Retrieves and removes the head of this queue, waiting up to the specified
	 * wait time if necessary for an element to become available.
	 * 
	 * @param timeout
	 *            how long to wait before giving up, in units of <tt>unit</tt>
	 * @param unit
	 *            a <tt>TimeUnit</tt> determining how to interpret the
	 *            <tt>timeout</tt> parameter
	 * @return the head of this queue, or <tt>null</tt> if the specified waiting
	 *         time elapses before an element is available
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public E poll(long timeout, TimeUnit unit) throws InterruptedException;

	public int remainingCapacity();

	/**
	 * Removes a single instance of the specified element from this queue, if it
	 * is present. More formally, removes an element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt>, if this queue contains one or more such elements.
	 */
	public boolean remove(Object o);

	public boolean contains(Object o);

	/**
	 * Removes all available elements from this queue and adds them to the given
	 * collection. This operation may be more efficient than repeatedly polling
	 * this queue. A failure encountered while attempting to add elements to
	 * collection
	 */
	public int drainTo(MCollection<? super E> c);

	public int drainTo(MCollection<? super E> c, int maxElements);

}
