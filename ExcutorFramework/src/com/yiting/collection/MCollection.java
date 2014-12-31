/**
 * @auEhor yiting ,collection.java write in 2014/5/13
 * collection 是整个集合的父类，其继承了iterable遍历接口  E(element)代表了集合中存储的元素类型，从此处可以看出一个集合中存储的类型是一样的
 *
 *当前并没有加上异常的说明，如有兴趣者可以加上异常的说明。
 *
 */
package com.yiting.collection;

public interface MCollection<E> extends Iterable<E> {
	
	
	/**
	 * 
	 * @return 返回集合的大小，如果集合的大小超过了Integer.MAX_VALUE(2^32) 则返回Integer.MAX_VALUE
	 */
	
	public int size();
	
	
	
	/**
	 * 判断集合是否为空
	 * @return false 代表集合为空，true代表集合中存在元素
	 */
	
	public boolean isEmpty();
	
	
	
	/**
	 * 判断当前集合中是否存在对象O 成功返回true。
	 * @param o
	 * @return
     * @throws ClassCastException   {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
	 */
	
	public boolean contains(Object o);
	
	
	
	/**？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？ some problems is waiting to undstand.
	 * 返回一个遍历子对象，但是Iterable 中已经有iterator（）这个方法，为什么此处还要定要iterator（）方法 还有待考究，希望后来者能够解答此处问题
	 * answer by yiting:为什么这么写就是为了方便的目的 实现这个借口的类可以很方便的查询到他所有要实现的方法，
	 * 而不必要去个个查询到底要实现了哪些方法，只要查询这个接口就可以了。
	 * 如果说在有开发平台的情况下是可以自动生成，但是假如在没有开发平台下 一个个去查询那是相当繁琐的
	 */
	
	public Iterator<E> iterator();
	
	
	
	/**
	 * 把集合转化为一个objecE数组，其中colleEion的顺序是什么，返回值的顺序和其是一样的
	 * @return
	 */
	
	public Object[] toArray();
	
	
	
	
	/**
	 * 类似与toArray（） 这个主要更加有效的利用了内存，判断集合的大小 如果集合的大小超过了
	 * a[]数组的大小 通过反射机制，重新扩大a数组的容量，如果没有超过a数组的大小，就在a数组的最后面添加null  a[size]=null 这样有利于计算最后返回的数组
	 * 的实际长度（当用户知道 他的集合中没有null元素的时候）
	 * 
	 * @param a一个用于保存返回值的数组  最后函数返回值就是return a；关键是这个a的大小会根据数组的大小和集合的大小做比较动态的变化大小
	 *  if (a.length < size)   
     *      a = (E[])java.lang.reflect.Array.   
     *            newInstance(a.getClass().getComponentType(), size);   
     *       System.arraycopy(elementData, 0, a, 0, size);   
     *   if (a.length > size)   
     *       a[size] = null; 
     *       return a；  
	 * @return a；
	 */
	
	public <T> T[] toArray(T a[]);
	
	
	
	/**
	 * 往集合中添加一个类型为E的对象，必须类型为E　因为集合存储的元素类型是E　所以添加的元素的类型也为E
	 * @param e
	 * @return 添加成功返回true
	 */
	
	public boolean  add(E e);
	
	/**
	 * 删除集合中存在的元素，如果不存在该元素则返回false
	 * @param e
	 * @return 删除成功返回true
	 */
	
	public boolean  remove(E e);
	
	
	
	/**
	 * 判断一个集合中是否存在集合C，即判断C是不是当前集合的一个子集
	 *
	 * @param c c集合中元素的类型是？ ？代表集合元素的类型是动态决定的
	 * @return 如果是子集关系返回为true
	 */
	
	public boolean containsAll(MCollection<?> c);
	
	
	/**
	 * 当前集合中添加一个集合
	 * @param c 中元素的类型是动态决定的，但是元素类型必须是E的子类
	 * @return
	 */
	
	public boolean addAll(MCollection<? extends E> c);
	
	/**
	 * 删除当前集合与C集合的交集，即删除两者中相同的元素，
	 * @param c c集合中元素的类型是动态决定的，
	 * @return
	 */
	public boolean removeAll(MCollection<?> c);
	
	
	/**
	 * 删除出集合C以外的所有元素 即只保留当前集合与C集合的交集 其他元素都删除
	 * @param c
	 * @return
	 */
	public boolean retainAll(MCollection<?> c);
	
	
	
	/**
	 * 清除集合中所有元素
	 */
	
	public void clear();
	
	
	
	/**
	 * 判断两个对象是否相等，最好不要改写这个函数，一般改写的目的就是改变通过引用来判断两个对象相等
	 * 把其改为按对象中的某些值来判断对象相等。
	 * equals拥有几个规则  传递性 自反性 必须满足  x.equals(y)=>y.equals(x)
	 * warning:改写了equals那就必须改写hashcode函数
	 * @param o
	 * @return
	 */
	
	public boolean equals(Object o);
	
	/**
	 * hashcode,是对象存储在内存中的一个散列值，hash值有一个性质，如果x.equals(y)=true 则x.hashcode()=y.hashcode()
	 * 但是反过来是不成立的  即x.hashcode()=y.hashcode() !=>x.equals(y)=true
	 * @return
	 */
	public int hashCode();
	
	
}
