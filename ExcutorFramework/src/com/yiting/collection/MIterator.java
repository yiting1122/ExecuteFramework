package com.yiting.collection;


/**
 * 
 * @author Administrator
 *Iterator 是一个遍历对象，通过这个遍历对象可以对collection 对象进行遍历
 * @param <E> E代表集合中保存的数据类型
 */
public interface MIterator<E> extends java.util.Iterator<E>{
	/**
	 * 
	 * hasNext用来判断是否有element，如果没有返回false
	 * 
	 * 
	 */
	public boolean hasNext();
	
	/**
	 * next获取下一个元素，同时把索引往后面移动，从而可以继续访问后面的 类似链表操作
	 * @return
	 */
	public E next();
	
	/**
	 * remove用于删除一个element，但是如果在next函数之前调用remove，在下次next调用时可能产生异常
	 * 一般都是next之后跟一个remove
	 * 这里引入删除的好处就是方便，否则在元素删除时还要进行遍历或者保存该位置。
	 */
	public void remove();
}
