/**
 * this was writen in 2014/5/15
 * @author yiting
 * 这个接口继承了Iterator接口的方法，但是又提供了其独特的方法，实现这个接口的集合
 * 可以改变遍历的方向，其可以向前也可以向后，主要用于满足对list集合的遍历需求
 * 同时该接口也提供了在遍历的时候进行增加，更新 删除等功能
 */
package com.yiting.collection;

public interface ListIterator<E> extends Iterator<E> {
	
	

	public boolean hasNext();
	public E next();
	public void remove();
	
	
	/**
	 * 判断当前位置元素是否有前驱，主要用于当list集合反转遍历方向的时候
	 * @return 返回true代表当前元素拥有前驱，其前卖弄还有节点
	 */
	
	public boolean hasPrevious();
	
	
	
	/**
	 * 返回前驱节点的位置index，
	 * 其与previous（）连用
	 * @return  返回index，当现在处于集合的起始位置的时候，返回-1
	 */
	public int previousIndex();
	
	/**
	 * 返回当前节点的前驱节点
	 * @return 前驱节点
	 * @throws exception NoSuchElementException 当没有前驱节点的时候
	 */
	
	public E previous();
	
	
	
	
	/**
	 * 返回后继节点的位置index
	 * 与next连用
	 * 当list达到最后一个元素时，可知其当前位置为n-1,那么nextindex就返回n，就是list的长度
	 * @return
	 */
	
	public int nextIndex();
	
	
	/**
	 * 设置刚刚通过next返回的那个element位置的节点为e，其必须在next后面调用，如果在add或者remove后调用
	 * 就会产生异常。该方法即实现了对象的修改。
	 * @param e 
	 * @throws ClassCastException 类型转化异常
	 * @throws IllegalStateException 在调用了add 或者remove函数后使用该方法，抛出异常
	 * @return
	 */
	public void set(E e);
	
	
	
	
	/**
	 * 插入元素，元素插入的位置在next返回元素的前面，或者previous返回元素的后面
	 * @param e
	 */
	public void add(E e);
	

}
