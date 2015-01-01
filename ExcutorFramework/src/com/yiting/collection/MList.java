/**
 * this write in 2014/5/14
 * @author yiting
 * 本接口中没做说明的接口函数均来自于collection接口中，想查看接口函数说明的请转至 collection中
 * 接口函数有做说明的就是当前接口新增加的函数。这样有利于区分，当前接口增加了哪些功能。
 * 这种标示方法并没有采用java的标示方法。
 */
package com.yiting.collection;

public interface MList<E> extends MCollection<E> {

	public int size();

	public boolean isEmpty();

	public boolean contains(Object o);

	public MIterator<E> iterator();

	public Object[] toArray();

	public <T> T[] toArray(T a[]);

	public boolean add(E e);

	public boolean remove(E e);

	public boolean containsAll(MCollection<?> c);

	public boolean addAll(MCollection<? extends E> c);

	public boolean removeAll(MCollection<?> c);

	public boolean retainAll(MCollection<?> c);

	public void clear();

	public boolean equals(Object o);

	public int hashCode();

	/**
	 * 获取一个集合中的一个元素
	 * 
	 * @param index
	 *            元素在集合中的位置，如果index超出了集合的长度，抛出异常
	 * @return 返回集合中指定位置的对象值
	 * @throws IndexOutOfBoundsException
	 */

	public E get(int index);

	/**
	 * 替换index位置的对象
	 * 
	 * @param index 指定位置
	 * @param element 要替换的对象
	 * @return 返回替换掉的对象
	 * @throws ClassCastException ,当该插入元素的类型并不能转化为E类型时候抛出异常
	 * @throws IndexOutOfBoundsException 当index超出了范围
	 * @throws NullPointerException 当element为null时，而当前集合不容许插入null的时候
	 * @throws IndexOutOfBoundsException    
	 */

	public E set(int index, E element);

	/**
	 * 在index位置插入element，相当于数组的插入，后面的元素要后移，腾出位置
	 * 
	 * @param index 插入位置
	 * @param element 要插入的对象
	 * @throws ClassCastException 当该插入元素的类型并不能转化为E类型时候抛出异常
	 * @throws IndexOutOfBoundsException 当index超出了范围
	 * @throws NullPointerException 当element为null时，而当前集合不容许插入null的时候
	 */

	public void add(int index, E element);

	/**
	 * 删除index位置的对象，同时返回该删除的对象，类似数组的删除，删除之后 右边的元素要左移
	 * 
	 * @param index指定位置
	 * @return 删除对象
	 * @throws IndexOutOfBoundsException 当index超出了范围
	 */

	public E remove(int index);

	/**
	 * 返回指定对象在元素中的位置，（第一个位置）
	 * 
	 * @param o 指定对象
	 * @return 返回position，如果list中没有该对象则返回-1；
	 * @throws ClassCastException 当前对象不能转化为E类型的对象
	 * @throws NullPointerException 当前对象o=null，而集合中是不容许出现null元素 抛出异常
	 */
	public int indexOf(Object o);

	/**
	 * 返回与该对象O相等的最后一个位置，集合中可能存在多个相同值
	 * 
	 * @param o 指定对象
	 * @return 返回position，如果list集合中没有该元素则返回-1，那么可以采取从后往前找到第一个就行
	 * @throws ClassCastException 当前对象不能转化为E类型的对象
	 * @throws NullPointerException 当前对象o=null，而集合中是不容许出现null元素 抛出异常
	 */

	public int lastIndexOf(Object o);

	/**
	 * ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！这个方法使用要特别注意
	 * 
	 * 获取当前List对象的一个子集，子集是位置连续的,当fromIndex=toIndex时返回null 它返回原来list的从[fromIndex,
	 * toIndex)之间这一部分的视图，之所以说是视图，是因为实际上，返回的list是靠原来的list支持的。
	 * 所以，你对原来的list和返回的list做的“非结构性修改”(non-structural changes)，都会影响到彼此对方。
	 * 所谓的“非结构性修改”，是指不涉及到list的大小改变的修改。相反，结构性修改，指改变了list大小的修改。
	 * 当对原来的list进行了结构修改，则通过sublist返回的list将不可用 一个使用方法
	 *  list.subList(from,
	 * to).clear();
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @throws IndexOutOfBoundsException 当fromIndex<0 或者toIndex>size;
	 * @throws IllegalArgumentException fromIndex>toIndex
	 * 
	 *             其子类的一个实现  从其实现就可知，该方法其实返回的就是list 只不多设置list的中的几个变量
	 * SubList(AbstractList<E> list, int fromIndex, inttoIndex) { 
	 *           if (fromIndex < 0) throw newIndexOutOfBoundsException("fromIndex = " + fromIndex);
	 *           if(toIndex > list.size()) throw new IndexOutOfBoundsException("toIndex = " + toIndex);
	 *           if(fromIndex > toIndex) throw newIllegalArgumentException("fromIndex(" + fromIndex +
	 *             ") > toIndex(" + toIndex + ")"); 
	 *             l = list; 
	 *             offset = fromIndex;
	 *             size = toIndex - fromIndex;
	 *              expectedModCount =
	 *             l.modCount;
	 *             }
	 */
	
	public MList<E> subList(int fromIndex, int toIndex);
	
	/**
	 * 返回一个ListIterator遍历子，通过该遍历子进行遍历，ListIterator继承与Itarotor，其比Iterator
	 * 增加了更多的方法，即可以实现正向反向遍历 同时可以进行更新增加等功能
	 * @return
	 */
	public MListIterator<E> listIterator();
	
	/**
	 * 返回一个从指定位置开始的ListIterator遍历子
	 * @param index  遍历子的初始位置
	 * @return
	 */
	public MListIterator<E> listIterator(int index);
	
	

}
