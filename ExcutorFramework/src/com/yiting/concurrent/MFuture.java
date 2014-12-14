package com.yiting.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future 是异步任务的结果 ，提供methods 可以检验计算是否完成、等待完成、取回结果等 当计算完成时只能通过get获取结果
 * 
 * @author yiting
 * 
 * @param <V>
 */

public interface MFuture<V> {

	/**
	 * 试图取消执行任务，当任务执行完毕、任务已经取消、或者不能被取消等其他原因
	 * 执行该方法则都会返回false，当任务还没有执行却调用了该任务的cancel方法，那么该方法不会执行
	 * 
	 * <p>
	 * After this method returns, subsequent calls to {@link #isDone} will
	 * always return <tt>true</tt>. Subsequent calls to {@link #isCancelled}
	 * will always return <tt>true</tt> if this method returned <tt>true</tt>.
	 * 
	 * @param mayInterruptIfRunning
	 *            true当线程执行该任务时可以被终端interrupt
	 * @return 当任务已经执行完执行该方法会返回false，其他都返回true
	 */
	boolean cancel(boolean mayInterruptIfRunning);

	/**
	 * 
	 * @return true 当在task正常结束前将task终止
	 */
	boolean isCancelled();

	/**
	 * 
	 * @return true 当任务完成（包括terminate、cancel、exception。或者正常完成）
	 */
	boolean isDone();

	/**
	 * 该方法一直等待计算出结果并获取返回
	 * 
	 * @return 返回结果
	 * @throws InterruptedException
	 *             中断异常
	 * @throws ExecutionException
	 *             执行计算异常
	 * @throws cancellationException
	 *             取消异常
	 */
	V get() throws InterruptedException, ExecutionException;

	/**
	 * 跟get（）的区别就在与本方法只等待固定的时间
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 *             超时异常
	 */
	V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException;
}
