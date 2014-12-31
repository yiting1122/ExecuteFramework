
/**
 * @ # this class Iterable write in 2014/5/13
 */


package com.yiting.collection;

import javax.tools.JavaCompiler;

/**
 * iterable用形容词命名，表述的是实现该接口的类拥有遍历的功能，从而使得一个集合拥有 foreach的功能
 * for-each循环可以与任何实现了Iterable接口的对象一起工作。
 * @author yiting
 *
 * @param <T> 遍历集合中元素的类型
 */
public interface Iterable<T> extends java.lang.Iterable<T> {
	
	/**
	 * 通过该方法获取一个遍历Iterator的遍历子对象，通过该遍历子对集合进行遍历
	 * @return  iteraor对象
	 */
	Iterator<T> iterator();
}
