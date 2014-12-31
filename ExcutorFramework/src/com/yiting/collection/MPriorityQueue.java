package com.yiting.collection;
//package com.util;
//
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.ConcurrentModificationException;
//import java.util.NoSuchElementException;
//
//
//
//
///**
// * @author youyou; write in 2014/5/28
// * 基于优先级堆的无界优先级
// * 优先级队列依靠自然顺序的也不允许插入不可比较的对象
// */
//
//public class PriorityQueue<E> extends AbstractQueue<E> implements java.io.Serializable {
//	private static final long serialVersionUID = -7720805057305804111L;
//	private static final int DEFAULT_INITIAL_CAPACITY = 11;
//	
//	/**
//	 *优先级队列表示为一个平衡二叉堆 ；
//	 *queue[n]的两个孩子分别为：queue[2*n+1]、queue[2*(n+1)]
//	 *优先级队列是由比较器命令或者通过元素的自然顺序来排序，如果比较器为空，为堆中每
//	 *个节点n 和 每个n的后代 d，n <= d；
//	 *最低的元素值在队列queue[0]，假设队列非空；
//	 */
//	private transient Object[] queue;
//	
//	/**
//	 * 在这个有限队列中元素的个数
//	 */
//	private int size = 0;
//	
//	
//	/**
//	 * 比较器，或null如果优先级队列使用元素的自然顺序
//	 */
//	private final Comparator<? super E> comparator;
//	
//	
//	/**
//	 * 这个优先级队列被结构上的修改的次数
//	 */
//	private transient int modCount = 0;
//	
//	/**
//	 * 创建一个默认初始值的队列，队列中元素的顺序根据他们的自然顺序
//	 */
//	public PriorityQueue(){
//		this(DEFAULT_INITIAL_CAPACITY,null);
//	}
//	
//	
//	/**
//	 * 创建一个指定初始容量的队列,队列中元素的顺序根据他们的自然顺序
//	 * @param intialCapacity 优先队列的初始容量
//	 * @throws 如果初始容量少于1，则抛出异常
//	 */
//	public PriorityQueue(int intialCapacity){
//		this (intialCapacity,null);
//	}
//	
//	
//	/**
//	 * 创建一个指定初始容量的队列，队列中元素的顺序根据指定的比较器进行排序(传入一个排序策略，对元素进行比较)
//	 */
//	public PriorityQueue(int initialCapacity, Comparator<? super E>comparator){
//		if(initialCapacity < 1)
//			throw new IllegalArgumentException();
//		this.queue = new Object[initialCapacity];
//		this.comparator = comparator;
//	}
//	
//	/**
//	 * 创建一个队列包括在指定集合中的元素
//	 * @param c
//	 */
//	@SuppressWarnings("unchecked")
//	public PriorityQueue(Collection<? extends E> c){
//		if(c instanceof SortedSet<?>){
//			SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
//			this.comparator = (Comparator<? super E>) ss.comparator();
//			initElementsFromCollection(ss);
//		}
//		else if (c instanceof PriorityQueue<?>){
//			PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
//			this.comparator = (Comparator<? super E>) pq.comparator();
//			initFromPriorityQueue(pq);
//		}
//		else{
//			this.comparator = null;
//			initElementsFromCollection(c);
//		}
//	}
//	
//	
//	/**
//	 *创建一个队列包括在指定队列中的元素
//	 */
//	@SuppressWarnings("unchecked")
//	public PriorityQueue(PriorityQueue<? extends E> c){
//		this.comparator = (Comparator<? super E>) c.comparator();
//		initFromPriorityQueue(c);
//	}
//	
//	
//	/**
//	 *创建一个队列包括在指定排序集合中的元素
//	 */
//	public PriorityQueue(SortedSet<? extends E> c){
//		this.comparator =  (Comparator<? super E> )c.comparator();
//		initElementsFromCollection(c);
//	}
//	
//	private void initFromPriorityQueue(PriorityQueue<? extends E> c){
//		if(c.getClass() == PriorityQueue.class){
//			this.queue = c.toArray();
//			this.size = c.size();
//		}else{
//			initFromCollection(c);
//		}
//	}
//		
//	private void initElementsFromCollection(Collection<? extends E> c){
//		Object[] a = c.toArray();
//		//if c.toArray incorrectly doesn't return Object[],copy it.
//		if(a.getClass() != Object[].class)
//			a = Arrays.copyOf(a,a.length,Object[].class);
//		int len = a.length;
//		if(len == 1 || this.comparator != null)
//			for(int i=0; i<len; i++){
//				if(a[i] == null)
//					throw new NullPointerException();
//			}
//		this.queue = a;
//		this.size = a.length;
//				
//	}
//	
//	/**
//	 *初始化队列的数组的元素是从给定集合中获取的
//	 *@param c 是集合
//	 */
//	private void initFromCollection(Collection<? extends E> c ){
//		initElementsFromCollection(c);
//		heapify();
//	}
//	
//	/** 数组的大小*/
//	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
//	
//	
//	/**
//	 * 增加数组的容量
//	 */
//	private void grow (int minCapacity){
//		int oldCapacity = queue.length;
//		int newCapacity  = oldCapacity + ((oldCapacity < 64)?
//										  (oldCapacity +2) :
//										  (oldCapacity >> 1));
//		if(newCapacity - MAX_ARRAY_SIZE > 0)
//			newCapacity = hugeCapacity(minCapacity);
//		queue = Arrays.copyOf(queue, newCapacity);
//	}
//	
//	private static int hugeCapacity(int minCapacity){
//		if(minCapacity < 0 )//overflow
//			throw new OutOfMemoryError();
//		return (minCapacity > MAX_ARRAY_SIZE) ?
//				Integer.MAX_VALUE:
//				MAX_ARRAY_SIZE;
//	}
//	
//	public boolean add(E e){
//		return offer(e);
//	}
//	
//	@Override
//	/**
//	 * 向这个优先级队列中插入指定的元素
//	 */
//	public boolean offer(E e) {
//		// TODO Auto-generated method stub
//		if(e == null)
//			throw new NullPointerException();
//		modCount++;
//		int i = size;
//		if(i >= queue.length)
//			grow(i + 1);
//		size = i + 1;
//		if(i == 0)
//			queue[0] = e;
//		else
//			siftUp(i,e);
//		return true;
//	}
//
//	@Override
//	public E peek() {
//		// TODO Auto-generated method stub
//		if(size == 0 )
//			return null;
//		return (E) queue[0];
//	}
//	
//	private int indexOf(Object o){
//		if(o != null){
//			for(int i = 0; i < size; i++)
//				if(o.equals(queue[i]))
//					return i;
//		}
//		return -1;
//	}
//
//	
//	/**
//	 * 从这个队列中删除指定元素的单一实例，如果它存在
//	 */
//	public boolean remove(Object o){
//		int i = indexOf(o);
//		if(i == -1)
//			return false;
//		else{
//			removeAt(i);
//			return true;
//		}
//	}
//	
//	/**
//	 * 版本的删除，使用引用平等或不平等
//	 */
//	boolean removeEq(Object o){
//		for (int i= 0; i<size; i++){
//			if(o == queue[i]){
//				removeAt(i);
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	/**
//	 * 如果这个队列包含这个指定的元素，则返回true，否则False
//	 */
//	public boolean contains(Object o){
//		return indexOf(o) != -1;
//	}
//	
//	/**
//	 * 返回一个数组，这个数组包含了此队列中的所有元素。这些元素没有特定的排列顺序
//	 * 这种方法必须分配一个新的数组，因此调用者可以自由地区修改返回的数组。
//	 */
//	public Object[] toArray(){
//		return Arrays.copyOf(queue, size);
//	}
//	
//	
//	/**
//	 * 返回的数组的运行类型是指定数组
//	 * 如果队列符合指定的数组，则它返回；否则，一个新的数组被分配指定数组的运行类型和此队列的大小
//	 */
//	public <T> T[] toArray(T[] a){
//		if(a.length < size)
//			return (T[]) Arrays.copyOf(queue, size,a.getClass());
//		System.arraycopy(queue, 0, a, 0, size);
//		if(a.length > size)
//			a[size] = null;
//		return a;
//	}
//	
//	/**
//	 * Returns an iterator over the elements in this queue. The iterator
//     * does not return the elements in any particular order.
//     * Iterator 是一个遍历对象，通过这个遍历对象可以对collection 对象进行遍历
//	 */
//	@Override
//	public Iterator<E> iterator(){
//		return new Itr();
//	}
//	
//	private final class Itr implements Iterator<E>{
//		
//		private int cursor = 0;
//		
//		private int lastRet = -1;
//		
//		/**
//		 * 队列中的元素从堆中未访问的部分移动到已访问的部分，在迭代的过程中作为“不幸运”的元素移动结果
//		 */
//		private ArrayDeque<E> forgetMeNot = null;
//		
//		private E lastRetElt = null;
//		
//		/**
//		 *modCount值，表示迭代器认为应该支持 队列，如果违反了这一期望，迭代器检测并发修改
//		 */
//		private int exceptedModCount = modCount;
//		
//		public boolean hasNext(){
//			return cursor < size || (forgetMeNot != null && !forgetMeNot.isEmpty());
//		}
//		
//		public E next(){
//			if(exceptedModCount != modCount)
//				throw new ConcurrentModificationException();
//			if(cursor < size)
//				return (E) queue[lastRet = cursor++];
//			if(forgetMeNot != null){
//				lastRet = -1;
//				lastRetElt = forgetMeNot.poll();
//				if(lastRetElt != null)
//					return lastRetElt;
//			}
//			throw new NoSuchElementException();
//		}
//		
//		public void remove(){
//			if(exceptedModCount != modCount)
//				throw new ConcurrentModificationException();
//			if(lastRet != -1){
//				E moved = PriorityQueue.this.removeAt(lastRet);
//				lastRet = -1;
//				if(moved == null)
//					cursor--;
//				else{
//					if (forgetMeNot == null)
//						forgetMeNot = new ArrayDeque<>();
//					forgetMeNot.add(moved);
//				}
//			}else if(lastRetElt != null){
//				PriorityQueue.this.removeEq(lastRetElt);
//				lastRetElt = null;
//			}else{
//				throw new IllegalStateException();
//			}
//			exceptedModCount = modCount;
//		}	
//	}
//	@Override
//	public int size(){
//		return size;
//	}
//	
//	/**
//	 * 删除优先队列中的全部元素
//	 * 当这个调用返回之后，这个队列会为空
//	 */
//	public void clear(){
//		modCount ++;
//		for(int i=0; i<size; i++){
//			queue[i] = null;
//		}
//		size = 0;
//	}
//	
//	public E poll(){
//		if(size == 0)
//			return null;
//		int s = --size;
//		modCount ++;
//		E result = (E) queue[0];
//		E x = (E) queue[s];
//		queue[s] = null;
//		if(s != 0)
//			siftDown(0,x);
//		return result;
//	}
//	
//	/**
//	 * 从队列中删除第i个元素
//	 */
//	private E removeAt(int i){
//		assert i >= 0 && i < size; 
//		modCount++;
//		int s = --size;
//		if(s == i)
//			queue[i] = null;
//		else{
//			E moved = (E) queue[s];
//			queue[s] = null;
//			siftDown(i,moved);
//			if(queue[i] == moved){
//				siftUp(i,moved);
//				if(queue[i] != moved)
//					return moved;
//			}
//		}
//		return null;
//	}
//	
//	/**
//	 * 在位置K上插入X，维持堆不变通过把X往树顶移动，直到它大于或者等于它的父节点或者根节点的时候
//	 * @param k 表示出入的位置
//	 * @param x 待插入的项
//	 */
//	private void siftUp(int k, E x){
//		if(comparator != null)
//			siftUpUsingComparator(k,x);
//		else
//			siftUpComparable(k,x);
//	}
//	
//	private void siftUpComparable(int k,E x){
//		Comparable <? super E> key = (Comparable<? super E>) x;
//		while( k > 0 ){
//			int parent = (k -1) >>> 1;//>>>表示右移一位
//			Object e  = queue[parent];
//			if(key.compareTo((E) e) >= 0)
//				break;
//			queue[k] = e;
//			k = parent;
//		}
//		queue[k] = key;
//	}
//	
//	private void siftUpUsingComparator(int k, E x){
//		while(k > 0){
//			int parent = (k - 1)>>>1;
//			Object e = queue[parent];
//			if(comparator.compare(x, (E) e) >= 0)
//				break;
//			queue[k] = e;
//			k = parent;
//		}
//		queue[k] = x;	
//	}
//	
//	
//	/**
//	 * 在位置K上插入X，维持堆不变通过把X往树底移动，直到它小于或者等于它的孩子节点或者叶子节点的时候
//	 * @param k 表示出入的位置
//	 * @param x 待插入的项
//	 */
//	private void siftDown(int k, E x){
//		if(comparator != null)
//			siftDownUsingComparator(k,x);
//		else
//			siftDownComparable(k,x);
//	}
//	
//	private void siftDownComparable(int k,E x){
//		Comparable<? super E> key = (Comparable<? super E >)x;
//		int half = size >>> 1;//loop while a non-leaf
//		while(k < half){
//			int child = (k << 1) + 1;// assume left chlid is least
//			Object c = queue[child];
//			int right = child + 1;
//			if(right < size && ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0 )
//				c = queue[child = right];
//			if(key.compareTo((E) c) <= 0)
//				break;
//			queue[k] = c;
//			k = child ;
//		}
//		queue[k] = key;
//	}
//	
//	private void siftDownUsingComparator(int k,E x){
//		int half =  size >>> 1;
//		while(k < half){
//			int child  = (k << 1) + 1;
//			Object c = queue[child];
//			int right = child + 1;
//			if(right < size && comparator.compare((E) c, (E) queue[right]) > 0)
//				c= queue[child = right];
//			if(comparator.compare(x, (E) c ) <= 0)
//				break;
//			queue[k] = c;
//			k = child;	
//		}
//		queue[k] = x;
//	}
//	
//	
//	/**
//	 * 在完全树上建立一个不变的堆
//	 * 假设在调用之前与元素的顺序无关
//	 */
//	private void heapify(){
//		for(int i = (size >>> 1) - 1; i >= 0; i--)
//			siftDown(i, (E) queue[i]);
//	}
//	
//	
//	/**
//	 * 比较器是用来对此队列进行排序，或者如果这个队列的排序是根据元素的自然顺序
//	 *@return comparator
// 	 */
//	public Comparator<? super E> comparator(){
//	 return comparator;
//	}
//	
//	
//	/**
//	 * 保存实例的状态流（即序列化）
//	 */
//	private void writeObject(java.io.ObjectOutputStream s)
//		throws java.io.IOException{
//		//write out element count, and any hidden stuff
//		s.defaultWriteObject();
//		
//		//write out array length, for compatibility with 1.5 version
//		s.writeInt(Math.max(2, size + 1));
//		
//		//write out all elements in the "proper order";
//		for(int i = 0; i< size; i++)
//			s.writeObject(queue[i]);
//	}
//	
//	/**
//	 * 从流中重建实例（也就是说，反序列化）
//	 */
//	private void readObject(java.io.ObjectInputStream s)
//		throws java.io.IOException,ClassNotFoundException{
//		//read in size , and any hidden stuff
//		s.defaultReadObject();
//		
//		//read in(and discard) array length
//		s.readInt();
//		
//		queue = new Object[size];
//		
//		//read in all elements
//		for(int i = 0;i < size; i++){
//			queue[i] = s.readObject();
//			
//		//elements are guaranteed to be in "proper order",but the
//		//sepc has never explained what that might be
//		heapify();
//		}
//	}
//	
//	
//	
//
//}
