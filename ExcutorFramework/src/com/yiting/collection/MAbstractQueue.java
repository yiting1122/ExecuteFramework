package com.yiting.collection;
import java.util.*;
/**
 * 
 * @author youyou; write in 2014/5/28
 * 这个类提供了一些主要的实现
 * 通常增加的方法也会被重载
 */
public class MAbstractQueue<E> extends MAbstractCollection<E> implements MQueue<E>{

	/**
	 * 使用子类构造函数
	 */
	protected MAbstractQueue(){	
	}
	
	
	/**
	 * 在不违反队列容量限制的前提下，插入指定的元素
	 * 如果插入成功就返回true,插入失败抛出异常
	 * @param e 插入的元素
	 */
	public boolean add(E e){
		if(offer(e))
			return true;
		else
			throw new IllegalStateException("Queue full");
	}
	
	
	/**
	 * 获取和删除队列中的头
	 * @return 返回队列的头
	 * @throws 如果队列为空，则抛出异常
	 */
	@Override
	public E remove() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * 获取，但并不删除队列中的头元素
	 * @return 返回队列的头元素
	 * @throws 如果队列为空则抛出异常
	 */
	@Override
	public E element() {
		// TODO Auto-generated method stub
		E x = peek();
		if(x != null)
			return x;
		else 
			throw new NoSuchElementException();	
	}
	
	/**
	 * 从此队列中删除所有的元素
	 * 在这个函数调用之后，队列会为空
	 */
	public void clear(){
		while(poll() != null)
			;
	}
	
	/**
	 * 将指定集合的所有元素添加到此队列
	 * 此外，该操作的行为是未定义的操作时，如果指定的集合被修改，同时操作也在进行
	 * 这个实现遍历指定的集合，并将迭代器返回的每一个元素添加到此队列。
	 */
	public boolean addAll(MCollection <? extends E> c){
		if(c == null)
			throw new NullPointerException();
		if(c == this)
			throw new IllegalArgumentException();
		boolean modified = false;
		for(E e : c)
			if(add(e))
				modified = true;
		return modified;
			
	}
	
	@Override
	public boolean offer(E e) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public E poll() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public E peek() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}


}
