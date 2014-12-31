/**
 * this  write in 2014/5/15
 * @author  youyou
 * #############表示 有不懂的地方
 **/
package com.yiting.collection;

public interface MQueue<E> extends MCollection<E> {
	/**
	 * 在不超过队列容量的前提下向队列中插入一个特定的元素
	 * 如果插入成功则返回true,否则，抛出异常。
	 * **/
	public boolean add(E e);
	
	 /**
	  *  * 在不超过队列容量的前提下向队列中插入一个特定的元素
	 * 如果插入成功则返回true,否则，抛出异常。
	 * 这个方法与add函数类似
	  */
	public boolean offer(E e);	
	
	/**
	  * 这个方法与poll()不同
	  * 获取和删除队列中的头元素，如果此队列为空，就会抛出异常
	  * @return 返回这个队列的头元素
	  * @throws 如果此队列为空，抛出NoSuchElementException异常
	  * **/
	public E remove();
	
	/**
	 * ###################这个E代表是什么意思？Queue<E>也代表什么意思？##############################
	 * 获取和删除队列中的头元素，如果此队列为空，返回空值NULL
	 * @return 返回这个队列的头元素，或者返回空值null(如果此队列为空)
	 */
	public E poll();
	
	/**
	 * ####################此方法与peek不同，peek是在link.java里面，可是找不到它属于哪个包##################################
	 * 获取，但不删除，此队列中的头元素
	 * @return 返回这个队列的头元素
	 * @throw  如果此队列为空，抛出NoSuchElementException异常
	 */
	public E element();
	
	/**
	 *获取，但不删除，此队列的头元素，如果此队列为空，返回空值null
	 *@return 返回这个队列的头元素，或者返回空值null(如果此队列为空)
	 */
	public E peek();
	
	
	
	
	
	
}
