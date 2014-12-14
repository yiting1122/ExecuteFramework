package com.yiting.concurrent;

/**
 * 重写runnable接口 即提供一个实现future功能（能返回结果的runnable接口）
 * @author yiting
 *
 * @param <V>
 */

public interface MRunnableFuture<V> extends Runnable , MFuture<V> {
	
	public void run();
}
