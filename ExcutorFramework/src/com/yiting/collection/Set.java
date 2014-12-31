package com.yiting.collection;
/**
 * 这个接口是一个集合的成员,继承了Collection
 
	 * 在Collection中，有此接口所有函数的详细解释
	 * 
 * @author youyou
 *  write in 2014/6/1
 *
 * @param <E> 由这个集合组成的元素类型
 */
public interface Set<E> extends Collection<E>{
	//查询操作
	/**
	 * 返回 集合中元素的个数
	 * 如何这个结合所包含的元素超过了最大值，则返回最大值MAX_VALUE
	 */
	 int size();
	
	
	/**
	 *  判断集合是否为空，为空返回True,否则false
	 * @return 如果此集合没有元素，则返回true
	 */
	 boolean isEmpty();
	
	/**
	 * 判断集合中是否含有指定的元素，如果含有则返回Ture
	 * @throws ClassCastException 如果指定元素的类型不能与集合的类型一致，则抛出异常
	 * @throws NullPointerExeception 如果指定元素为空，并且此集合不允许空元素，则抛出异常
	 *
	 */
	 boolean contains(Object o);
	 
	 
	 /**
		 * Returns an iterator over the elements in this queue. The iterator
	     * does not return the elements in any particular order.
	     * Iterator 是一个遍历对象，通过这个遍历对象可以对collection 对象进行遍历
		 */
	Iterator<E> iterator();
	

	 /**
		 * 返回一个数组，这个数组包含了此队列中的所有元素。这些元素没有特定的排列顺序
		 * 这种方法必须分配一个新的数组，因此调用者可以自由地区修改返回的数组。
		 */
	 Object[] toArray();
	 
	 
	 /**
	  * 类似与toArray（） 这个主要更加有效的利用了内存，判断集合的大小 如果集合的大小超过了
	 * a[]数组的大小 通过反射机制，重新扩大a数组的容量，如果没有超过a数组的大小，就在a数组的最后面添加null  a[size]=null 这样有利于计算最后返回的数组
	 * 的实际长度（当用户知道 他的集合中没有null元素的时候）
	  */
	 <T> T[] toArray(T[] a);
	 
	 
		
		/**
		 * 往集合中添加一个类型为E的对象，必须类型为E　因为集合存储的元素类型是E　所以添加的元素的类型也为E
		 * @param e
		 * @return 添加成功返回true
		 */
	 boolean add(E e);
	 
	 
	 /**
		 * 删除集合中存在的元素，如果不存在该元素则返回false
		 * @param e
		 * @return 删除成功返回true
		 */	
	 boolean remove(Object o);
	 
	 
	 /**
		 * 判断一个集合中是否存在集合C，即判断C是不是当前集合的一个子集
		 *
		 * @param c c集合中元素的类型是？ ？代表集合元素的类型是动态决定的
		 * @return 如果是子集关系返回为true
		 */
	 boolean containsAll(Collection<?> c);
		

	 boolean addAll(Collection<? extends E> c);
	 
	 boolean retainAll(Collection<?> c);
	 
	 boolean removeAll(Collection<?> c);
	 
	 void clear();
	 
	 boolean equals (Object o);
	 	 
	 int hashCode();
}
