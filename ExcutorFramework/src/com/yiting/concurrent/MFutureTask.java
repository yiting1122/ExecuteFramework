package com.yiting.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.yiting.concurrent.locks.MAbstractQueuedSynchronizer;

/**
 * 
 * @author yiting
 * 
 * 
 *         A cancellable asynchronous computation. This class provides a base
 *         implementation of {@link Future}, with methods to start and cancel a
 *         computation, query to see if the computation is complete, and
 *         retrieve the result of the computation. The result can only be
 *         retrieved when the computation has completed; the <tt>get</tt> method
 *         will block if the computation has not yet completed. Once the
 *         computation has completed, the computation cannot be restarted or
 *         cancelled.
 * @param <V>
 * 
 */
public class MFutureTask<V> implements MRunnableFuture<V> {
	/** 同步控制标识 */
	private final Sync sync;

	public MFutureTask(MCallable<V> callable) {
		if (callable == null) {
			throw new NullPointerException();
		}
		sync = new Sync(callable);
	}

	public MFutureTask(Runnable runnable, V result) {
		sync = new Sync(MExcutors.callable(runnable, result));
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {

		return sync.innerCancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {

		return sync.innerIsCancelled();
	}

	@Override
	public boolean isDone() {

		return sync.innerIsDone();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {

		return sync.innerGet();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {

		return sync.innerget(unit.toNanos(timeout));
	}

	@Override
	public void run() {
		sync.innerRun();
	}

	protected void set(V v) {
		sync.innerSet(v);
	}

	protected void setException(Throwable t) {
		sync.innerSetException(t);
	}

	protected boolean runAndReset() {
		return sync.innerRunAndReset();
	}

	protected void done() {
	}

	private final class Sync extends MAbstractQueuedSynchronizer {

		private static final long serialVersionUID = -6831548415462362258L;

		/** state value of the task; */
		private static final int READY = 0; // TASK is ready to run;
		private static final int RUNNING = 1;// TASK is running;
		private static final int RAN = 2; // TASK ran(over)
		private static final int CANCELLED = 4; // TASK is cancelled

		private final MCallable<V> callable; // the underlying callable
		private V result; // the result to return from get()
		private Throwable exception; // the exception to throw from get()

		/**
		 * The thread running task. When nulled after set/cancel, this indicates
		 * that the results are accessible. Must be volatile, to ensure
		 * visibility upon completion.
		 */
		private volatile Thread runner;

		Sync(MCallable<V> callable) {
			this.callable = callable;
		}

		private boolean ranOrCancelled(int state) {
			return (state & (RAN | CANCELLED)) != 0;
		}

		protected int tryAcquireShared(int ignore) {
			return innerIsDone() ? 1 : -1;
		}

		protected boolean tryReleaseShared(int ignore) {
			runner = null;
			return true;
		}

		boolean innerIsCancelled() {
			return getState() == CANCELLED;
		}

		private boolean innerIsDone() {
			return ranOrCancelled(getState()) && runner == null;
		}

		V innerGet() throws InterruptedException, ExecutionException {
			acquireSharedInterruptibly(0);
			if (getState() == CANCELLED) {
				throw new CancellationException();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}
			return result;
		}

		V innerget(long nanosTimeout) throws InterruptedException,
				ExecutionException, TimeoutException {
			if (!tryAcquireSharedNanos(0, nanosTimeout)) {
				throw new TimeoutException();
			}
			if (getState() == CANCELLED) {
				throw new CancellationException();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}
			return result;
		}

		void innerSet(V v) {
			for (;;) {
				int s = getState();
				if (s == RAN) {
					return;
				}
				if (s == CANCELLED) {
					releaseShared(0);
					return;
				}
				if (compareAndSetState(s, RAN)) {
					result = v;
					releaseShared(0);
					done();
					return;
				}
			}
		}

		void innerSetException(Throwable t) {
			for (;;) {
				int s = getState();
				if (s == RAN)
					return;
				if (s == CANCELLED) {
					// aggressively release to set runner to null,
					// in case we are racing with a cancel request
					// that will try to interrupt runner
					releaseShared(0);
					return;
				}
				if (compareAndSetState(s, RAN)) {
					exception = t;
					releaseShared(0);
					done();
					return;
				}
			}
		}

		boolean innerCancel(boolean mayInterruptIfRunning) {
			for (;;) {
				int s = getState();
				if (ranOrCancelled(s)) {
					return false;
				}
				if (compareAndSetState(s, CANCELLED)) {
					break;
				}
			}
			if (mayInterruptIfRunning) {
				Thread r = runner;
				if (r != null) {
					r.interrupt();
				}
			}
			releaseShared(0);
			done();
			return true;
		}

		void innerRun() {
			if (compareAndSetState(READY, RUNNING)) {
				return;
			}
			runner = Thread.currentThread();
			if (getState() == RUNNING) {
				V result;
				try {
					result = callable.call();
				} catch (Throwable ex) {
					setException(ex);
					return;
				}
				setResult(result);
			} else {
				releaseShared(0);
			}
		}

		boolean innerRunAndReset() {
			if (!compareAndSetState(READY, RUNNING)) {
				return false;
			}
			try {
				runner = Thread.currentThread();
				if (getState() == RUNNING) {
					callable.call();
				}
				runner = null;
				return compareAndSetState(RUNNING, READY);
			} catch (Throwable ex) {
				setException(ex);
				return false;
			}
		}

	}

}
