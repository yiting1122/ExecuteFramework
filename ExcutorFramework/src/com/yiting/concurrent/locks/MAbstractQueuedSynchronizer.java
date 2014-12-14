package com.yiting.concurrent.locks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.yiting.concurrent.locks.MCondition;

import sun.misc.*;

public abstract class MAbstractQueuedSynchronizer extends
		MAbstarctOwnableSynchronizer implements Serializable {

	private static final long serialVersionUID = 4572261883510245093L;

	protected MAbstractQueuedSynchronizer() {
	}

	/**
	 * the queue
	 * 
	 * @author yiting
	 * 
	 */
	static final class Node {
		static final Node SHARED = new Node();
		static final Node EXCLUSIVE = null;

		/*
		 * 状态字段，只能取下面的值： SIGNAL(-1)： 这个结点的后继是（或很快是）阻塞的（通过park），所以当前结点
		 * 必须unpark它的后继，当它释放或取消时。为了避免竞争，acquire方法必须
		 * 首先表明它们需要一个信号，然后再次尝试原子性acquire，如果失败了就阻塞。
		 * 
		 * CANCELLED(1)： 这个结点由于超时或中断已被取消。结点从不离开这种状态。尤其是， 这种状态的线程从不再次阻塞。
		 * 
		 * CONDITION(-2)： 这个结点当前在一个条件队列上。它将不会用于sync队列的结点， 直到被转移，在那时，结点的状态将被设为0.
		 * 这个值在这里的使用与其他字段的使用没有关系，仅仅是简化结构。
		 * 
		 * PROPAGATE(-3)： releaseShared应该传递给其他结点。这是在doReleaseShared里设置
		 * （仅仅是头结点）以确保传递继续，即使其他操作有干涉。
		 * 
		 * 0： 非以上任何值。
		 * 
		 * 值是组织为数字的用以简化使用。非负值表示结点不需要信号。这样，大部分代码不需要 检查特定的值，只需要(检查)符号。
		 */

		static final int CANCELLED = 1;
		/** waitStatus value to indicate successor's thread needs unparking */
		static final int SIGNAL = -1;
		/** waitStatus value to indicate thread is waiting on condition */
		static final int CONDITION = -2;
		/**
		 * waitStatus value to indicate the next acquireShared should
		 * unconditionally propagate
		 */
		static final int PROPAGATE = -3;

		volatile int waitStatus;
		volatile Node prev;
		volatile Node next;
		volatile Thread thread;
		Node nextWaiter;

		final boolean isShared() {
			return nextWaiter == SHARED;
		}

		final Node predecessor() throws NullPointerException {
			Node p = prev;
			if (p == null) {
				throw new NullPointerException();
			} else {
				return p;
			}
		}

		Node() { // Used to establish initial head or SHARED marker
		}

		Node(Thread thread, Node mode) { // Used by addWaiter
			this.nextWaiter = mode;
			this.thread = thread;
		}

		Node(Thread thread, int waitStatus) { // Used by Condition
			this.waitStatus = waitStatus;
			this.thread = thread;
		}

	} // end the node

	private transient volatile Node head;
	private transient volatile Node tail;
	private volatile int state;

	protected final int getState() {
		return this.state;
	}

	protected final void setState(int newState) {
		this.state = newState;
	}

	/**
	 * cas set the state
	 * 
	 * @param expect
	 * @param update
	 * @return
	 */
	protected final boolean compareAndSetState(int expect, int update) {
		return unsafe.compareAndSwapInt(this, stateOffest, expect, update);
	}

	/**
	 * 入队操作
	 * 
	 * @param node
	 * @return
	 */
	private Node enq(final Node node) {
		for (;;) {
			Node t = tail;
			if (t == null) {
				if (compareAndSetHead(new Node())) {
					tail = head;
				}
			} else {
				node.prev = t;

				// 这里没明白取代tail的意思，为什么？
				/**
				 * 这里的解释：AQS中保存了这个tail节点，只有跟tail节点进行CAS操作成功后才能设置next节点，
				 * 这样保证了并发的正确性 head-> node-> node ->node tail------------↑
				 * 随着node的移动 tail的指向也会向后移动
				 */
				if (compareAndSetTail(t, node)) {
					t.next = node;
					return t;
				}
			}
		}

	}

	/**
	 * @param mode
	 *            Node.EXCLUSIVE for exclusive, Node.SHARED for shared
	 * @return the new node
	 */
	private Node addWaiter(Node mode) {
		Node node = new Node(Thread.currentThread(), mode);
		Node pred = tail;
		if (pred != null) {
			node.prev = pred;
			if (compareAndSetTail(pred, node)) {
				pred.next = node;
				return node;
			}
		}
		enq(node);
		return node;
	}

	/**
	 * set head of queue. Called only by acquire methods.
	 * 
	 * @param node
	 */
	private void setHead(Node node) {
		head = node;
		node.thread = null;
		node.prev = null;
	}

	/**
	 * @param node
	 */
	private void unparkSucessor(Node node) {
		int ws = node.waitStatus;
		if (ws < 0) {
			compareAndSetWaitStatus(node, ws, 0);
		}

		Node s = node.next;
		if (s == null || s.waitStatus > 0) {
			s = null;
			// 从后向前，找到第一个节点
			for (Node t = tail; tail != null && t != node; t = t.prev) {
				if (t.waitStatus <= 0) {
					s = t;
				}
			}
		}

		if (s != null) {
			LockSupport.unpark(s.thread);
		}

	}

	/**
	 * 设置头结点的状态
	 */
	private void doReleaseShared() {
		for (;;) {
			Node h = head;
			if (h != null && h != tail) {
				int ws = h.waitStatus;
				if (ws == Node.SIGNAL) {
					if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) {
						continue;
					}
					unparkSucessor(h); // 唤醒头结点后的下一个节点
				} else if (ws == 0
						&& !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)) {
					continue;
				}
			}
			if (h == head) { // 当h=head时说明head没有改变，此时可以跳出循环
				break;
			}
		}
	}

	private void setHeadAndPropagate(Node node, int propagate) {
		Node h = head; // 这种记录的方式可以用于比较head是否有改变
		setHead(node);
		if (propagate > 0 || h == null || h.waitStatus < 0) {
			Node s = node.next;
			if (s == null || s.isShared()) {
				doReleaseShared();
			}
		}
	}

	private void cancelAcquire(Node node) {
		if (node == null) {
			return;
		}
		node.thread = null;
		Node pred = node.prev;
		while (pred.waitStatus > 0) {
			node.prev = pred = pred.prev;
		}
		Node predNext = pred.next;
		node.waitStatus = Node.CANCELLED;
		// If we are the tail, remove ourselves.
		if (node == tail && compareAndSetTail(node, pred)) {
			compareAndSetNext(pred, predNext, null); // 把node节点中的next指向null
		} else {
			int ws;
			if (pred != head
					&& ((ws = pred.waitStatus) == Node.SIGNAL || (ws < 0 && compareAndSetWaitStatus(
							pred, ws, Node.SIGNAL))) && pred.thread != null) {
				Node next = node.next;
				if (next != null && next.waitStatus <= 0) {
					compareAndSetNext(pred, predNext, next);
				}
			} else {
				unparkSucessor(node);
			}

			node.next = node; // help GC
		}
	}

	private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
		int ws = pred.waitStatus;
		if (ws == Node.SIGNAL) {
			return true;
		}
		if (ws > 0) {
			do {
				node.prev = pred = pred.prev;
			} while (pred.waitStatus > 0);
			pred.next = node;
		} else {
			/*
			 * waitStatus must be 0 or PROPAGATE. Indicate that we need a
			 * signal, but don't park yet. Caller will need to retry to make
			 * sure it cannot acquire before parking.
			 */
			compareAndSetWaitStatus(node, ws, Node.SIGNAL);
		}
		return false;
	}

	public static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	private final boolean parkAndCheckInterrupt() {
		LockSupport.park(this);
		return Thread.interrupted();
	}

	final boolean acquireQueued(final Node node, int arg) {
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null;// help GC;
					failed = false;
					return interrupted;
				}
				if (shouldParkAfterFailedAcquire(p, node)) {
					parkAndCheckInterrupt();
					interrupted = true;
				}
			}
		} finally {
			if (failed) {
				cancelAcquire(node);
			}
		}
	}

	/**
	 * Acquires in exclusive interruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireInterruptibly(int arg) throws InterruptedException {
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return;
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in exclusive timed mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.EXCLUSIVE);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head && tryAcquire(arg)) {
					setHead(node);
					p.next = null; // help GC
					failed = false;
					return true;
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node)
						&& nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared uninterruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireShared(int arg) {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			boolean interrupted = false;
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						if (interrupted)
							selfInterrupt();
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					interrupted = true;
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared interruptible mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 */
	private void doAcquireSharedInterruptibly(int arg)
			throws InterruptedException {
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return;
					}
				}
				if (shouldParkAfterFailedAcquire(p, node)
						&& parkAndCheckInterrupt())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	/**
	 * Acquires in shared timed mode.
	 * 
	 * @param arg
	 *            the acquire argument
	 * @param nanosTimeout
	 *            max wait time
	 * @return {@code true} if acquired
	 */
	private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
			throws InterruptedException {

		long lastTime = System.nanoTime();
		final Node node = addWaiter(Node.SHARED);
		boolean failed = true;
		try {
			for (;;) {
				final Node p = node.predecessor();
				if (p == head) {
					int r = tryAcquireShared(arg);
					if (r >= 0) {
						setHeadAndPropagate(node, r);
						p.next = null; // help GC
						failed = false;
						return true;
					}
				}
				if (nanosTimeout <= 0)
					return false;
				if (shouldParkAfterFailedAcquire(p, node)
						&& nanosTimeout > spinForTimeoutThreshold)
					LockSupport.parkNanos(this, nanosTimeout);
				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
				if (Thread.interrupted())
					throw new InterruptedException();
			}
		} finally {
			if (failed)
				cancelAcquire(node);
		}
	}

	protected boolean tryAcquire(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean tryRelease(int arg) {
		throw new UnsupportedOperationException();
	}

	protected int tryAcquireShared(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean tryReleaseShared(int arg) {
		throw new UnsupportedOperationException();
	}

	protected boolean isHeldExclusively() {
		throw new UnsupportedOperationException();
	}

	public final void acquire(int arg) {
		if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
			selfInterrupt();
		}
	}

	public final void acquireInterruptibly(int arg) throws InterruptedException {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		if (!tryAcquire(arg)) {
			doAcquireInterruptibly(arg);
		}
	}

	public final boolean tryAcquireNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
	}

	public final boolean release(int arg) {
		if (tryRelease(arg)) {
			Node h = head;
			if (h != null && h.waitStatus != 0) {
				unparkSucessor(h);
			}
			return true;
		}
		return false;
	}

	public final void acquireShared(int arg) {
		if (tryAcquireShared(arg) < 0)
			doAcquireShared(arg);
	}

	public final void acquireSharedInterruptibly(int arg)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		if (tryAcquireShared(arg) < 0)
			doAcquireSharedInterruptibly(arg);
	}

	public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
			throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		return tryAcquireShared(arg) >= 0
				|| doAcquireSharedNanos(arg, nanosTimeout);
	}

	public final boolean releaseShared(int arg) {
		if (tryReleaseShared(arg)) {
			doReleaseShared();
			return true;
		}
		return false;
	}

	public final boolean hasQueuedThreads() {
		return head != tail;
	}

	public final boolean hasContended() {
		return head != null;
	}

	public final Thread getFirstQueuedThread() {
		// handle only fast path, else relay
		return (head == tail) ? null : fullGetFirstQueuedThread();
	}

	private Thread fullGetFirstQueuedThread() {
		Node h, s;
		Thread st;
		if (((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null)
				|| ((h = head) != null && (s = h.next) != null
						&& s.prev == head && (st = s.thread) != null)) {
			return st;
		}

		Node t = tail;
		Thread firstThread = null;
		while (t != null && t != head) {
			Thread tt = t.thread;
			if (tt != null) {
				firstThread = tt;
			}
			t = t.prev;
		}

		return firstThread;

	}

	public final boolean isQueued(Thread thread) {
		if (thread == null)
			throw new NullPointerException();
		for (Node p = tail; p != null; p = p.prev)
			if (p.thread == thread)
				return true;
		return false;
	}

	final boolean apparentlyFirstQueuedIsExclusive() {
		Node h, s;
		return (h = head) != null && (s = h.next) != null && !s.isShared()
				&& s.thread != null;
	}

	public final boolean hasQueuedPredecessors() {
		Node t = tail;
		Node h = head;
		Node s;
		return h != t
				&& ((s = h.next) == null || s.thread != Thread.currentThread());
	}

	/**
	 * estimate the length that's wait to acquire CPU
	 * 
	 * @return
	 */
	public final int getQueueLength() {
		int n = 0;
		for (Node p = tail; p != null; p = p.prev) {
			if (p.thread != null) {
				++n;
			}
		}
		return n;
	}

	/**
	 * estimate the queued threads ,because the thread is dynamically.
	 * 
	 * @return
	 */
	public final Collection<Thread> getQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			Thread t = p.thread;
			if (t != null) {
				list.add(t);
			}
		}
		return list;
	}

	public final Collection<Thread> getExclusiveThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (!p.isShared()) {
				Thread t = p.thread;
				if (t != null) {
					list.add(t);
				}
			}
		}
		return list;
	}

	final boolean isOnSyncQueue(Node node) {
		if (node.waitStatus == Node.CONDITION || node.prev == null) {
			return false;
		}
		if (node.next != null) {// If has successor, it must be on queue
			return true;
		}
		return findNodeFromTail(node);
	}

	private boolean findNodeFromTail(Node node) {
		Node t = tail;
		for (;;) {
			if (t == node) {
				return true;
			}
			if (t == null) {
				return false;
			}
			t = t.prev;
		}
	}

	/**
	 * Transfers a node from a condition queue onto sync queue. Returns true if
	 * successful.
	 * 
	 * @param node
	 *            the node
	 * @return true if successfully transferred (else the node was cancelled
	 *         before signal).
	 */
	final boolean transferForSignal(Node node) {
		if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			return false;
		}
		Node p = enq(node);
		int ws = p.waitStatus;
		if (ws > 0 || compareAndSetWaitStatus(p, ws, Node.SIGNAL)) {
			LockSupport.unpark(node.thread);
		}
		return true;
	}

	/**
	 * Transfers node, if necessary, to sync queue after a cancelled wait.
	 * Returns true if thread was cancelled before being signalled.
	 * 
	 * @param current
	 *            the waiting thread
	 * @param node
	 *            its node
	 * @return true if cancelled before the node was signalled
	 */
	final boolean transferAfterCancelledWait(Node node) {
		if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
			enq(node);
			return true;
		}
		/*
		 * If we lost out to a signal(), then we can't proceed until it finishes
		 * its enq(). Cancelling during an incomplete transfer is both rare and
		 * transient, so just spin.
		 */
		while (!isOnSyncQueue(node))
			Thread.yield();
		return false;
	}

	final int fullyRelease(Node node) {
		boolean failed = true;
		try {
			int saveState = getState();
			if (release(saveState)) {
				failed = false;
				return saveState;
			} else {
				throw new IllegalMonitorStateException();
			}
		} finally {
			if (failed) {
				node.waitStatus = Node.CANCELLED;
			}
		}
	}

	public final Collection<Thread> getExclusiveQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (!p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	public final Collection<Thread> getSharedQueuedThreads() {
		ArrayList<Thread> list = new ArrayList<Thread>();
		for (Node p = tail; p != null; p = p.prev) {
			if (p.isShared()) {
				Thread t = p.thread;
				if (t != null)
					list.add(t);
			}
		}
		return list;
	}

	public String toString() {
		int s = getState();
		String q = hasQueuedThreads() ? "non" : "";
		return super.toString() + "[State = " + s + ", " + q + "empty queue]";
	}

	public final boolean owns(ConditionObject condition) {
		if (condition == null)
			throw new NullPointerException();
		return condition.isOwnedBy(this);
	}

	public final boolean hasWaiters(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.hasWaiters();
	}

	public final int getWaitQueueLength(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitQueueLength();
	}

	public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
		if (!owns(condition))
			throw new IllegalArgumentException("Not owner");
		return condition.getWaitingThreads();
	}

	public class ConditionObject implements MCondition, Serializable {
		private static final long serialVersionUID = -3088849475955141529L;
		/** first node of condition queue */
		private transient Node firstWaiter;
		/** last node of condition queue */
		private transient Node lastWaiter;

		public ConditionObject() {
		}

		private Node addConditionWaiter() {
			Node t = lastWaiter;
			if (t != null && t.waitStatus != Node.CONDITION) {
				unlinkCancelledWaiters();
				t = lastWaiter;
			}
			Node node = new Node(Thread.currentThread(), Node.CONDITION);
			if (t == null) {
				firstWaiter = node;
			} else {
				t.nextWaiter = node;
			}
			return node;
		}

		private void doSignal(Node first) {
			do {
				if ((firstWaiter = first.nextWaiter) == null) {
					lastWaiter = null;
				}
				first.nextWaiter = null;
			} while (!transferForSignal(first) && (first = firstWaiter) != null);
		}

		private void doSignalAll(Node first) {
			lastWaiter = firstWaiter = null;
			do {
				Node next = first.nextWaiter;
				first.nextWaiter = null;
				transferForSignal(first);
				first = next;
			} while (first != null);
		}

		/**
		 * Unlinks cancelled waiter nodes from condition queue. Called only
		 * while holding lock. This is called when cancellation occurred during
		 * condition wait, and upon insertion of a new waiter when lastWaiter is
		 * seen to have been cancelled. This method is needed to avoid garbage
		 * retention in the absence of signals. So even though it may require a
		 * full traversal, it comes into play only when timeouts or
		 * cancellations occur in the absence of signals. It traverses all nodes
		 * rather than stopping at a particular target to unlink all pointers to
		 * garbage nodes without requiring many re-traversals during
		 * cancellation storms.
		 */
		private void unlinkCancelledWaiters() {
			Node t = firstWaiter;
			Node trail = null;
			while (t != null) {
				Node next = t.nextWaiter;
				if (t.waitStatus != Node.CONDITION) { // 删除t节点
					t.nextWaiter = null;
					if (trail == null) {
						firstWaiter = next;
					} else {
						trail.nextWaiter = next;
					}
					if (next == null) {
						lastWaiter = trail;
					}
				} else {
					trail = t;
				}
				t = next;
			}
		}

		@Override
		public final void signal() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignal(first);
			}
		}

		@Override
		public void signalAll() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			Node first = firstWaiter;
			if (first != null) {
				doSignalAll(first);
			}

		}

		@Override
		public void await() throws InterruptedException {
			// TODO Auto-generated method stub

		}

		@Override
		public void awaitUninterruptly() {
			Node node = addConditionWaiter();
			int saveState = fullyRelease(node);
			boolean interrupted = false;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if (Thread.interrupted()) {
					interrupted = true;
				}
			}
			if (acquireQueued(node, saveState) || interrupted) {
				selfInterrupt();
			}
		}

		private static final int REINTERRUPT = 1;
		private static final int THROW_IE = -1;

		private int checkInterruptWhileWaiting(Node node) {
			return Thread.interrupted() ? (transferAfterCancelledWait(node) ? REINTERRUPT
					: THROW_IE)
					: 0;
		}

		private void reportInterruptAfterWait(int interruptMode)
				throws InterruptedException {
			if (interruptMode == THROW_IE) {
				throw new InterruptedException();
			} else if (interruptMode == REINTERRUPT) {
				selfInterrupt();
			}
		}

		@Override
		public long awaitNanos(long nanosTimeout) throws InterruptedException {
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			long lastTime = System.nanoTime();
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (nanosTimeout <= 0L) {
					transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkNanos(this, nanosTimeout);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;

				long now = System.nanoTime();
				nanosTimeout -= now - lastTime;
				lastTime = now;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return nanosTimeout - (System.nanoTime() - lastTime);
		}

		@Override
		public boolean await(long time, TimeUnit unit)
				throws InterruptedException {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				LockSupport.park(this);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
					break;
				}
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
				interruptMode = REINTERRUPT;
			}
			if (node.nextWaiter != null) {
				unlinkCancelledWaiters();
			}
			if (interruptMode != 0) {
				reportInterruptAfterWait(interruptMode);
			}
			return false;
		}

		@Override
		public boolean awaitUntil(Date deadline) throws InterruptedException {
			if (deadline == null)
				throw new NullPointerException();
			long abstime = deadline.getTime();
			if (Thread.interrupted())
				throw new InterruptedException();
			Node node = addConditionWaiter();
			int savedState = fullyRelease(node);
			boolean timedout = false;
			int interruptMode = 0;
			while (!isOnSyncQueue(node)) {
				if (System.currentTimeMillis() > abstime) {
					timedout = transferAfterCancelledWait(node);
					break;
				}
				LockSupport.parkUntil(this, abstime);
				if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
					break;
			}
			if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
				interruptMode = REINTERRUPT;
			if (node.nextWaiter != null)
				unlinkCancelledWaiters();
			if (interruptMode != 0)
				reportInterruptAfterWait(interruptMode);
			return !timedout;
		}

		final boolean isOwnedBy(MAbstractQueuedSynchronizer sync) {
			return sync == MAbstractQueuedSynchronizer.this;
		}

		protected final boolean hasWaiters() {
			if (!isHeldExclusively()) {
				throw new IllegalMonitorStateException();
			}
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION) {
					return true;
				}
			}
			return false;
		}

		protected final int getWaitQueueLength() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			int n = 0;
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION)
					++n;
			}
			return n;
		}

		protected final Collection<Thread> getWaitingThreads() {
			if (!isHeldExclusively())
				throw new IllegalMonitorStateException();
			ArrayList<Thread> list = new ArrayList<Thread>();
			for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
				if (w.waitStatus == Node.CONDITION) {
					Thread t = w.thread;
					if (t != null)
						list.add(t);
				}
			}
			return list;
		}

	}

	static final long spinForTimeoutThreshold = 1000L;

	private static final Unsafe unsafe = Unsafe.getUnsafe();
	private static final long stateOffest;
	private static final long headOffset;
	private static final long tailOffset;
	private static final long waitStatusOffset;
	private static final long nextOffset;

	static {
		try {
			stateOffest = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("state"));
			headOffset = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("head"));
			tailOffset = unsafe
					.objectFieldOffset(MAbstractQueuedSynchronizer.class
							.getDeclaredField("tail"));
			waitStatusOffset = unsafe.objectFieldOffset(Node.class
					.getDeclaredField("waitStatus"));
			nextOffset = unsafe.objectFieldOffset(Node.class
					.getDeclaredField("next"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	/**
	 * cas head field.used only by enq
	 * 
	 * @param update
	 * @return
	 */
	private final boolean compareAndSetHead(Node update) {
		return unsafe.compareAndSwapObject(this, headOffset, null, update);
	}

	/**
	 * cas tail field .Used only by enq
	 * 
	 * @param expect
	 * @param update
	 * @return
	 */
	private final boolean compareAndSetTail(Node expect, Node update) {
		return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
	}

	/**
	 * cas waitstatus field of a node
	 * 
	 * @param node
	 * @param expect
	 * @param update
	 * @return
	 */
	private static final boolean compareAndSetWaitStatus(Node node, int expect,
			int update) {
		return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
	}

	/**
	 * cas next filed of a node
	 * 
	 * @param node
	 * @param expect
	 * @param update
	 * @return
	 */
	private static final boolean compareAndSetNext(Node node, Node expect,
			Node update) {
		return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
	}

}
