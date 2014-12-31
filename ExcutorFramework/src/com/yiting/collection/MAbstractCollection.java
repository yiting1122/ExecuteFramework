/**
 * AbstractCollection.java writed in 2014/5/18
 * @author yiting
 * AbstractCollection抽象类实现了Collection接口，提供了接口中方法的默认实现
 * 同时添加了一个tostring方法的默认实现。
 * 如果继承该抽象类的集合是一个不可以改变的集合，那么该集合只需要实现size（）和Iterator的hasnext方法和next方法即可。
 * 如果继承该类的集合是一个可以改变的集合，那在上述基础上必须重载override add方法和实现Iterator接口方法中的remove方法
 * 
 * 推荐为每个抽象类提供一个protected 无参数的构造方法。这是java的一个规范。
 */


package com.yiting.collection;

import java.util.Arrays;

public abstract class MAbstractCollection<E> implements MCollection<E> {

	/**
	 * 该抽象类的构造函数定义为protected，可以限制只有其子类的构造函数才能调用该方法。
	 */
	protected MAbstractCollection() {
	}

	public abstract int size();

	public abstract Iterator<E> iterator();

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size() == 0;
	}

	@Override
	/**
	 * 判断集合中是否含有对象o
	 * 判断的时候一定要讨论是否o为null，如果o为null是不能采用equal方法进行比较的。
	 * @throws ClassCastException   {@inheritDoc}
	 * @throws NullPointerException {@inheritDoc}
	 */
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		Iterator<E> it = iterator();

		if (o == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				// 此处千万不能使用it.next().equals(o),因为it.next还是可以为空 导致异常
				if (o.equals(it.next())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 通过该方法获取该集合的一个数组，该数组返回的是所有集合中的元素
	 * 当it.hasNext为false时，通过调用arrays.copy从而可以避免导致copy的过程中
	 * 增加了null元素，从而导致错误，arays.copy返回一个数组。 该集合的元素可能是变化的
	 * 可能变多也可能减少,在并发过程中size（）的大小是可能变化的，所以在复制 过程中返回的大小是随着iterator遍历的个数动态决定的
	 * 
	 */

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		Object[] objects = new Object[size()];
		Iterator<E> it = iterator();
		for (int i = 0; i < objects.length; i++) {
			if (!it.hasNext()) {
				return Arrays.copyOf(objects, i); // 压缩数组大小
			}
			objects[i] = it.next();
		}
		return it.hasNext() ? finishToArray(objects, it) : objects;
	}
	
	
	


	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		int size=size();
		T[] r=a.length>size?a:(T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		Iterator<E> it=iterator();
		for(int i=0;i<size;i++){
			if(!it.hasNext()){
				if(a!=r){
					return Arrays.copyOf(r, size);
				}
				r[i]=null;  //在数组的最尾端加入null元素
				return r;
			}
			r[i]=(T) it.next();
		}
		return it.hasNext()?finishToArray(r, it):r;
	}





	/**
	 * 数组分配的最大值，此处为什么要减去8是因为有的虚拟机在数组钱买年保存了一些head words 避免产生outofMemoryError
	 */
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * 该方法实现元素的复制，能够动态的扩展T[]数组的容量，其中i记下当前位置
	 * 通过判断i和cap的值判断是否需要继续扩大，cap的增长速度为cap*1.5+1；最后返回的时候如果
	 * 容量没有满的话进行压缩，保证不会引入null元素
	 * @param r
	 * @param it
	 * @return
	 */
	private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
		int i = r.length;
		while (it.hasNext()) {
			int cap = r.length;
			if (i == cap) {
				int newCap = cap + cap >> 1 + 1;
				if (newCap - MAX_ARRAY_SIZE > 0) {
					newCap = hugeCapacity(cap + 1);
				}
				r = Arrays.copyOf(r, newCap); // 扩大数组大小
			}
			r[i++] = (T) it.next();
		}
		return (i == r.length) ? r : Arrays.copyOf(r, i);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) {
			throw new OutOfMemoryError("required array size too largy");
		}
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE
				: MAX_ARRAY_SIZE;
	}



	@Override
	public  boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public  boolean remove(E e) {
		Iterator<E> it=iterator();
		if(e==null){
			while(it.hasNext()){
				if(it.next()==null){
					it.remove();
					return true;
				}
			}	
		}else{
			while(it.hasNext()){
				if(e.equals(it.next())){
					it.remove();
					return true;
				}
			}
		}
		return false;
		
	}

	@Override
	public boolean containsAll(MCollection<?> c) {
		// TODO Auto-generated method stub
		for(Object e:c){
			if(!contains(e)){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(MCollection<? extends E> c) {
		// TODO Auto-generated method stub
		boolean modified=false;
		for(E e:c){
			if(add(e)){
				modified=true;
			}
		}
		return modified;
	}

	@Override
	public boolean removeAll(MCollection<?> c) {
		// TODO Auto-generated method stub
		Iterator<E> it=iterator();
		boolean modified=false;
		while(it.hasNext()){
			if(c.contains(it.next())){
				it.remove();
				modified=true;
			}
		}
		
		return modified;
	}

	@Override
	/**
	 * 保留当前集合中与C相同的元素
	 */
	public boolean retainAll(MCollection<?> c) {
		// TODO Auto-generated method stub
		Iterator<E> it=iterator();
		boolean modified=false;
		while(it.hasNext()){
			if(!c.contains(it.next())){
				it.remove();
				modified=true;
			}
		}
		
		return modified;
	}

	@Override
	/**
	 * remove跟在next之后 否则有可能报异常。
	 */
	public void clear() {
		Iterator<E> it=iterator();
		while(it.hasNext()){
			it.next();
			it.remove();
		}

	}
	
	@Override
	public String toString(){
		  Iterator<E> it = iterator();
	        if (! it.hasNext())
	            return "[]";

	        StringBuilder sb = new StringBuilder();
	        sb.append('[');
	        for (;;) {
	            E e = it.next();
	            sb.append(e == this ? "(this Collection)" : e);
	            if (! it.hasNext())
	                return sb.append(']').toString();
	            sb.append(',').append(' ');
	        }
	}

}
