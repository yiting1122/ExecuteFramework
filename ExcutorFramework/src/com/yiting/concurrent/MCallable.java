package com.yiting.concurrent;

/**
 * 该接口有点类似runnable接口，但是runnable接口无法返回结果，而ruannal可以 返回计算结果
 * 
 * @author yiting
 * 
 * @param <V>
 */
public interface MCallable<V> {

	/**
	 * 
	 * @return computed result
	 * @exception 如果无法返回一个结果时 throw exception
	 */
	V call() throws Exception;
}
