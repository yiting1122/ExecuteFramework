package com.yiting.collection;
/**
 * 
 * @wirte by youyou 2014/5/15 in the evening
 * #############表示 有不懂的地方
 * 
 */
/**
 * 双队列、双端队列
 * @param e
 */
public interface Deque<E> extends Queue<E> {

	/**
	 
	 * 在双端队列的前面插入元素。
	 * @param elem要插入的元素
	 *  @throw 如果因为超出容量的限制而不能插入元素， 则抛出illegalStateException
	 * @throw 如果类中特定的元素阻止其被加入到双端队列中，则抛出classCastException
	 * @throw 如果这个插入的元素是空值且此双端队列不允许空元素，则抛出NullPointerException
	 * @throw 如果这个插入元素的一些特性使其不能插入到这个双端队列中，则爬出illegalArgumentException
	 **/
		
	public void addFirst(E e);	
	
	/**
	 * 在不超出队列容量的前提下，在这个双队列的队尾插入一个特定的元素
	 * 当使用一个有容量限制的双队列，它通常更好地使用link 里面的offerLast方法//
	 * @param elem要插入的元素
	 *
	 */
	public void addLast(E e);
	
	
	/**
	 * 在双端队列的队头插入特定的元素
	 * @param e 要插入的元素
	* @return 如果元素成功加入到这个双端队列中，就返回true,否则false
	 */
	public boolean offerFirst(E e);
	
	
	/**
	 * 在双端队列的队尾插入特定的元素
	 * @param e 要插入的元素
	* @return 如果元素成功加入到这个双端队列中，就返回true,否则false
	 */
	public boolean offerLast(E e);
	
	
	/**
	 * 获取和删除这个双端队列中的第一个元素
	 * public boolean offerLast(E e){
	 * 	addLast(e);
	 *  return true;
	 * }
	 * @return 返回这个双端队列的头
	 * @throw 如果队列为空，则抛出noSuchElementException
	 */	
	public E removeFirst();
	
	/**
	 * 获取和删除这个双端队列的最后一个元素
	 * ############@return the tail of this deque##########这个怎么翻译比较好？
	 * @return 返回这个双端队列的尾
	 * @throw 如果队列为空，则抛出noSuchElementException
	 */
	
	public E removeLast();	
	
	/**
	 * 获取和删除这个双端队列的第一个元素
	 * @return 返回这个双端队列的头，或者如果此队列为空，返回NULL
	 */
	
	public E pollFirst();
	
	/**
	 * 获取和删除这个双端队列的最后一个元素
	 * @return 返回这个双端队列的尾，或者如果此队列为空，返回NULL
	 */
	public E pollLast();
	
	/**
	 * 获取（但不删除）此双端队列的第一个元素；
	 * @return 返回这个双端队列的头
	 * @throw 如果队列为空，则抛出noSuchElementException
	 */
	
	public E getFirst();
	
	
	/**
	 * 获取（但不删除）此双端队列的最后一个元素；
	 * @return 返回这个双端队列的尾
	 * @throw 如果队列为空，则抛出noSuchElementException
	 */
	
	public E getLast();
	
	
	/**
	 * 获取（但不删除）此双端队列的第一个元素；
	 * @return 返回这个双端队列的头
	 * 如果队列为空，则返回Null
	 */
	public E peekFirst();
	
	
	/**
	 * 获取（但不删除）此双端队列的最后一个元素；
	 * @return 返回这个双端队列的尾
	 * 如果队列为空，则返回Null
	 */
	
	public E peekLast();
	
	/**
	 * 删除第一次出现在从这双端队列中的指定元素
	 * 如果这个队列中不包含这个元素，那么就不做改变
	 * @Param o指定的对象元素，从这个双端队列中从删除，假设对象存在
	 * @return 如果一个元素被删除是这个调用的结果，则返回true
	 */
	
	public boolean removeFirstOccurrence(Object o);
	
	
	
	/**
	 * 删除最后一次出现在从这双端队列中的指定元素
	 * 如果这个队列中不包含这个元素，那么就不做改变
	 * @Param o指定的对象元素，从这个双端队列中从删除，假设对象存在
	 * @return 如果一个元素被删除是这个调用的结果，则返回true
	 */
	public boolean removeLastOccurrence(Object o);
	
	/**
	 *获取，（但不删除）这队列中的头元素
	 *如果队列为空，则返回null
	 */
	
	public E peek();
	
	/**
	 * 将一个元素压入栈，（换句话说，即在这个双端队列的头部）
	 * 这个方法等价于addFirst函数
	 */
	
	public void push(E e);
	
	
	/**
	 * 从栈中弹出一个元素，（换句话说，删除并返回这个队列的第一个元素）
	 * 这个方法等价于removeFirsth函数
	 * @return 返回这个队列的前面元素（栈顶元素）
	 */
	public E pop();
	

}
