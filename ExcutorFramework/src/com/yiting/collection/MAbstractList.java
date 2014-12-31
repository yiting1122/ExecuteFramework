/**
 * write in 2014/5/21
 * @author yiting
 * AbstractList class 是List的一个抽象实现，里面已经实现了大部分的函数，同时在该内中以实现了两个内部类遍历子接口，Itr和ListItr。
 * 内部内相当于该类的一个函数，所以其可以访问该类的成员方法和变量
 * 以及子链表SubList，该子List并不是返回另外一个list对象，而是跟list操作同个集合，通过offest偏移来进行操作。
 * AbstratList的实现着只要重写get size set remove 函数即可。
 * 如果是不可变集合则只要实现get size方法
 * 
 *  Fail-Fast(快速失败)机制
 *                    仔细观察上述的各个方法，我们在源码中就会发现一个特别的属性modCount，API解释如下：
 *                The number of times this list has been structurally modified. Structural modifications are those
 *                that change the size of the list, or otherwise perturb it in such a fashion that iterations in progress
 *                may yield incorrect results.
 *                   记录修改此列表的次数：包括改变列表的结构，改变列表的大小，打乱列表的顺序等使正在进行
 *              迭代产生错误的结果。Tips:仅仅设置元素的值并不是结构的修改
 *                  我们知道的是ArrayList是线程不安全的，如果在使用迭代器的过程中有其他的线程修改了List就会
 *               抛出ConcurrentModificationException这就是Fail-Fast机制。   
 *                  那么快速失败究竟是个什么意思呢？
 *              在ArrayList类创建迭代器之后，除非通过迭代器自身remove或add对列表结构进行修改，否则在其他
 *               线程中以任何形式对列表进行修改，迭代器马上会抛出异常，快速失败。   
 * 
 * 
 */



package com.yiting.collection;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public abstract class MAbstractList<E> extends MAbstractCollection<E> implements MList<E> {
	
	protected MAbstractList(){
	}
	
	/**
	 * add方法继承AbstractCollection，但是其添加规则必须按list的规则进行添加
	 * 所以其内部实际上调用的是list.add(index,element),这种设计方法达到了最大的解耦
	 * 使得新的集合子类只要定制一个自己的add方法就可以实现自己的集合。
	 * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by this list
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this list
     */
	 
	@Override
	public boolean add(E e){
		add(size(), e);
		return true;
	}
	
	
	/**
	 * @throws indexOutOfBoundsException 
	 */
	@Override
	public abstract E get(int index);
	
	
	/**
	 * 该实现就是抛出一个不支持异常，即当子类没有override该方法时就抛出不支持异常
	 */
	@Override
	public E set(int index,E element){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E remove(int index){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int indexOf(Object o){
		ListIterator<E> it=listIterator();
		if(o==null){
			while(it.hasNext()){
				if(it.next()==null){
					return it.previousIndex();
				}
			}
		}else {
			while(it.hasNext()){
				if(o.equals(it.next())){
					return it.previousIndex();
				}
			}
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o){
		
		ListIterator<E> it=listIterator();
		if(o==null){
			while(it.hasPrevious()){
				if(it.previous()==null){
					return it.nextIndex();
				}
			}
		}else {
			while(it.hasPrevious()){
				if(o.equals(it.previous())){
					return it.nextIndex();
				}
			}
		}
		return -1;
	}
	
	@Override
	public void clear(){
		removeRange(0, size());
	}
	
	/**
	 * 新增加方法，从指定位置获取一个listIterator，然后通过该遍历子 添加一个集合C，调用add（int ，e）
	 * @param index
	 * @param c
	 * @return
	 */
	public boolean addAll(int index,MCollection<? extends E> c){
		rangeCheckForAdd(index);
		boolean modified=false;
		for(E e:c){
			add(index++, e);
			modified=true;
		}
		return modified;
	}
	
	/**
	 * 返回一个Iterator遍历子，该遍历子依赖于list的size函数，get（int）函数，remove（int）函数
	 * 可以参考父类的解释
	 */
	@Override
	public Iterator<E> iterator(){
		return new Itr();
		
	}
	
	/**
	 * 获取一个从0开始的遍历子，该遍历子是list特有的，可以改变遍历方向
	 */
	@Override
	public ListIterator<E> listIterator(){
		return listIterator(0);
	}
	
	@Override
	public ListIterator<E> listIterator(int index){
		rangeCheckForAdd(index);
		return new ListItr(index);
	}
	/**
	 * 通过这个变量可以判断是否有多个线程进入访问
	 */
	protected transient int modCount=0;
	
	
	/**
	 * 这个get/set函数是我添加的 方便于测试，后续可以删除,方便用于输出modCount的值 还不要通过断点，同时可以通过set方法
	 * 来使得一些函数抛出异常，可以模拟并行环境
	 * @return
	 */
	public int getModCount() {
		return modCount;
	}

	public void setModCount(int modCount) {
		this.modCount = modCount;
	}


	/**
	 * 从下面可以看出remove必须和next搭配，不然lastRet就为-1；通过lastRet保证了只有在next之后才可以
	 * 调用remove 不然就是非法操作.同理可知lastRet保存的是最后一次调用next后保留的那个位置
	 * modcount是用于判断当前集合是否发生了结构性的变化，如果产生了变化那就会抛出不同步异常
	 */
	private class Itr implements Iterator<E>{
		int cursor=0;
		int lastRet=-1;
		int expectedModCount=modCount;
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return cursor!=size();
		}

		@Override
		public E next() {
			// TODO Auto-generated method stub
			checkForComodification();
			try {
				int i=cursor;
				E next=get(i);
				lastRet=i;
				cursor=i+1;
				return next;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
			
		}

		
		
		@Override
		public void remove() {
			// TODO Auto-generated method stub
			if(lastRet<0){
				throw new IllegalStateException();
			}
			checkForComodification();
			try {
				MAbstractList.this.remove(lastRet);
				if(lastRet<cursor){   //如果遍历指针和lastRet不相等，那么删除一个元素之后cursor必须减1，这样位置才能正确
					cursor--;          
				}
				lastRet=-1;
				expectedModCount=modCount;
			} catch (IndexOutOfBoundsException e) {    //产生indexoutofBoundsException 的条件就是并行操作
				throw new ConcurrentModificationException();
			}
		}
		
		final void checkForComodification(){
			if(modCount!=expectedModCount){
				throw new ConcurrentModificationException();
			}
		}
		
	}
	
	
	private class ListItr extends Itr implements ListIterator<E>{

		
		public ListItr(int index){
			cursor=index;
		}
		@Override
		public boolean hasPrevious() {
			// TODO Auto-generated method stub
			return cursor!=0;
		}

		@Override
		public int previousIndex() {
			// TODO Auto-generated method stub
			return cursor-1;
		}
		
		@Override
		public int nextIndex() {
			// TODO Auto-generated method stub
			return cursor;
		}
        /**
         * 遍历方向向前
         */
		@Override
		public E previous() {
			// TODO Auto-generated method stub
			checkForComodification();
			try {
				int i=cursor-1;
				E previous=get(i);
				lastRet=cursor=i;
				return previous;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}

		
        /**
         * 更新一个对象
         */
		@Override
		public void set(E e) {
			if(lastRet<0){
				throw new IllegalStateException();
			}
			checkForComodification();  //判断是否有产生结构变化非同步操作
			try {
				MAbstractList.this.set(cursor, e);   //修改对同步没有影响，不产生结构变化
				expectedModCount=modCount;
			} catch (IndexOutOfBoundsException exception) {
				throw new ConcurrentModificationException();
			}
	
			
		}

		 /*
		  * 每次有这种变化操作都要去修改expectModCount，这样可以知道是否某个时候有多个线程进入访问
		  */
		@Override
		public void add(E e) {
			checkForComodification();
			try {
				MAbstractList.this.add(cursor,e);
				cursor++;
				lastRet=-1;
				expectedModCount=modCount; 
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
		}
		
	}
	
	
	
	
	/**
	 * 该抽象类新增函数，主要用于删除元素，通过起始位置和结束位置
	 * @param fromIndex
	 * @param toIndex
	 */
	protected void removeRange(int fromIndex,int toIndex){
		ListIterator<E> it=listIterator(fromIndex);
		int len=toIndex-fromIndex;
		for(int i=0;i<len;i++){   //remove一定要在next之后调用
			it.next();
			it.remove(); 
		}
	}
	
	
/**
 * 判断index是否超出范围
 * @param index
 */
	
	private void rangeCheckForAdd(int index){
		if(index>size()||index<0){
			throw new IndexOutOfBoundsException("outOfBoundsException "+index);
		}
	}
	
	/**
	 * 其实最终都是调用的return  new SubList<E>(this, fromIndex, toIndex)
	 * 只不过有的list实现了随机存取，则可以用RandomAccessSubList<E>
	 */
	public  MList<E> subList (int fromIndex,int toIndex){
		return (this instanceof RandomAccess?new RandomAccessSubList<E>(this, fromIndex, toIndex):
			new SubList<E>(this, fromIndex, toIndex));
		
	}
	
	@Override
	public boolean equals(Object o){
		if(o==this){
			return true;
		}
		if(!(o instanceof MList)){
			return false;
		}
		ListIterator<E> e1=listIterator();
		ListIterator e2=((MList)o).listIterator();
		while(e1.hasNext()&&e2.hasNext()){
			E o1=e1.next();
			Object o2=e2.next();
			if(!(o1==null?o2==null:o1.equals(o2))){
				return false;
			}
		}
		return !(e1.hasNext()||e2.hasNext());
	}
	
	
	/**
	 * hashcode 不会出现溢出？？？？？
	 */
	@Override
	public int hashCode(){
		int hashCode=1;
		for(E e:this){
			hashCode=31*hashCode+(e==null?0:e.hashCode());
		}
		return hashCode;
	}
	
	
	
}

/**
 * 求子list，该子list依赖与原来的list，
 * @author yiting
 *
 * @param <E>
 */

class SubList<E> extends MAbstractList<E>{
	private final MAbstractList<E> l;
	private final int offset;
	private int size;
	public SubList(MAbstractList<E> list,int fromIndex,int toIndex){
		if(fromIndex<0||fromIndex>list.size()){
			throw  new IndexOutOfBoundsException("fromIndex <0 or fromIndex>list.size()");
		}
		if(toIndex<0||toIndex>list.size()){
			throw new IndexOutOfBoundsException("toIndex<0 or toIndex>list.size()");
		}
		if(toIndex<fromIndex){
			throw new IndexOutOfBoundsException("toIndex < fromIndex");
		}
		this.l=list;
		this.offset=fromIndex;
		this.size=toIndex-fromIndex;
		this.modCount=l.modCount;
	}
	@Override
	public void add(int index, E element) {
		rangeCheckForAdd(index);
        checkForComodification();
        l.add(index+offset, element);
        this.modCount = l.modCount;
        size++;
		
	}
	@Override
	public MList<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return new SubList<E>(this, fromIndex, toIndex);
	}
	@Override
	public E get(int index) {
		// TODO Auto-generated method stub
		 rangeCheck(index);
	     checkForComodification();
	     return l.get(index+offset);
	}
	@Override
	public E set(int index, E element) {
		rangeCheck(index);
		checkForComodification();
		return l.set(index+offset, element);
	}
	/**
	 * 通过modecount保证每次的操作都是在一个状态下的操作
	 * @param index
	 * @return
	 */
	@Override
	public E remove(int index) {
		rangeCheck(index);
		checkForComodification();
		E result=l.remove(index+offset);
		this.modCount=l.modCount;
		size--;
		return result;
	}
	@Override
	public boolean addAll(int index, MCollection<? extends E> c) {
		// TODO Auto-generated method stub
		rangeCheck(index);
		//jdk 直接采用的c.size()但是如果c为null的时候是否会报异常呢？ 感觉如果c==null；是不能用c.size（）的，后面可以测试
		int cSize=c.size();
		if(cSize==0){
			return false;
		}
		checkForComodification();
		l.addAll(index+offset, c);
		size+=cSize;
		return true;
	}
	
	@Override
	public Iterator<E> iterator(){
		return listIterator();
	}
	
	
	@Override
	public ListIterator<E> listIterator(final int index) {
		// TODO Auto-generated method stub
		checkForComodification();
		rangeCheckForAdd(index);
		return new ListIterator<E>() {
			private final ListIterator<E> i=l.listIterator(index+offset);
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return nextIndex()<size;
			}

			@Override
			public E next() {
				// TODO Auto-generated method stub
				if(hasNext()){
					return i.next();
				}else{
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				i.remove();
				SubList.this.modCount=l.modCount;
				size--;
				
			}

			@Override
			public boolean hasPrevious() {
				// TODO Auto-generated method stub
				return previousIndex()>0;
			}

			@Override
			public int previousIndex() {
				// TODO Auto-generated method stub
				return i.previousIndex()-offset;
			}

			@Override
			public E previous() {
				// TODO Auto-generated method stub
				if(hasPrevious()){
					return i.previous();
				}else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public int nextIndex() {
				// TODO Auto-generated method stub
				return i.nextIndex()-offset;
			}

			@Override
			public void set(E e) {
				// TODO Auto-generated method stub
				i.set(e);
			}

			@Override
			public void add(E e) {
				// TODO Auto-generated method stub
				i.add(e);
				SubList.this.modCount=l.modCount;
				size++;
			}
		};

	}
	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		 checkForComodification();
	        l.removeRange(fromIndex+offset, toIndex+offset);
	        this.modCount = l.modCount;
	        size -= (toIndex-fromIndex);
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		checkForComodification();
		return size;
	}
	
	 private void rangeCheck(int index) {
	        if (index < 0 || index >= size)
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }
	 private String outOfBoundsMsg(int index) {
	        return "Index: "+index+", Size: "+size;
	    }

	 private void checkForComodification() {
	        if (this.modCount != l.modCount)
	            throw new ConcurrentModificationException();
	    }
	 private void rangeCheckForAdd(int index) {
	        if (index < 0 || index > size)
	            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	    }


}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess{

	public RandomAccessSubList(MAbstractList<E> list, int fromIndex,
			int toIndex) {
		super(list, fromIndex, toIndex);
		// TODO Auto-generated constructor stub
	}




	
	
}
