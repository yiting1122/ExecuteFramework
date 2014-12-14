package com.yiting.concurrent;

import java.util.Collection;
import java.util.List;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 继承Mexecutor,在excutr上提供了对线程进行管理的功能，包括shutdown shutdownnow submit invokall 等共功能
 * 
 * @author yiting
 * 
 */
public interface MExecutorService extends MExcutor {
	/**
	 * shutdown用于关闭已经提交的线程，同时组织新的task加入，其不等待线程关闭。
	 */
	void shutdown();

	/**
	 * 试图关闭所有运行的task，终止所有等待中的task，同时返回所有等待tasks list. 但是其并不等待所有运行的task任务关闭
	 * 
	 * @return 等待tasks list 同时其并不保证线程都能被关闭，当通过interrupt取消task可能使得一些task无法关闭
	 */
	List<Runnable> shutdownNow();

	/**
	 * 
	 * @return true当executor关闭
	 */
	boolean isShutdown();

	/**
	 * 
	 * @return ture 当所有的task都终止 但是该函数必须后于shutdown或者shutdownNow 否则返回值不可能为true。
	 */
	boolean isTerminated();

	/**
	 * 该函数会一直阻塞 直到在shutdown请求后所有task执行完毕，或者timeout超时、或者当前线程thread 已经 interruted。
	 * 
	 * @param timeout
	 *            等待时间
	 * @param unit
	 *            时间单位 秒、毫秒、...
	 * @return true 当在timeout时间范围内executor终止
	 * @throws InterruptedException
	 *             当waiting状态时 线程interrupted
	 */
	boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * 提交一个可以获取返回值的task，通过MFuture的get函数
	 * 
	 * @param task
	 * @return
	 * @throws RejectedExecutionException
	 *             if the task cannot be scheduled for execution
	 * @throws NullPointerException
	 *             if the task is null
	 */
	<T> MFuture<T> submit(MCallable<T> task);

	/**
	 * 与submit(MCallable<T> task)的区别在于MCallable可以返回结果
	 * 而Runnable不能返回结果，所以传入result参数，从而可以获取计算结果
	 * 
	 * @param task
	 * @param result
	 * @return
	 */
	<T> MFuture<T> submit(Runnable task, T result);

	/**
	 * 该方法提交task 不会返回结果，MFuture.get返回的null
	 * 
	 * @param task
	 * @return
	 */
	MFuture<?> submit(Runnable task);

	/**
	 * 执行任务collection，并返回MFuture列表list，通过get可以获取每个collection的结果
	 * 
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 */
	<T> List<MFuture<T>> invokeAll(Collection<? extends MCallable<T>> tasks)
			throws InterruptedException;

	/**
	 * 相对于上一个方法 设置了时间限制
	 * 
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	<T> List<MFuture<T>> invokeAll(Collection<? extends MCallable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 返回任意一个已经成功处理的task的值，返回的结果类型根据collection的参数确定
	 * 
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 */
	<T> T invokeAny(Collection<? extends MCallable<T>> tasks)
			throws InterruptedException,ExecutionException;
	
	/**
	 * 设置了超时时间
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	<T> T invokeAny(Collection<? extends MCallable<T>> tasks,
            long timeout, TimeUnit unit)throws InterruptedException, ExecutionException, TimeoutException;

}
