package com.yiting.concollection;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import com.yiting.collection.MAbstractQueue;
import com.yiting.collection.MArrayList;
import com.yiting.collection.MCollection;
import com.yiting.collection.MIterator;
import com.yiting.collection.MQueue;
import sun.misc.Unsafe;

public class MConcurrentLinkedQueue<E> extends MAbstractQueue<E> implements
		MQueue<E>, Serializable {
	private static final long serialVersionUID = -6141140182150843181L;

	private static class Node<E> {
		volatile E item;
		volatile Node<E> next;

		private static final long itemOffset;
		private static final long nextOffset;

		static {

			try {
				itemOffset = unsafe.objectFieldOffset(Node.class
						.getDeclaredField("item"));
				nextOffset = unsafe.objectFieldOffset(Node.class
						.getDeclaredField("nextOffset"));
			} catch (Exception e) {
				throw new Error(e);
			}
		}

		Node(E item) {
			unsafe.putObject(this, itemOffset, item);
		}

		boolean casItem(E expect, E update) {
			return unsafe
					.compareAndSwapObject(this, itemOffset, expect, update);
		}

		void lazySetNext(Node<E> update) {
			unsafe.putOrderedObject(this, nextOffset, update);
		}

		boolean casNext(Node<E> expect, Node<E> update) {
			return unsafe
					.compareAndSwapObject(this, nextOffset, expect, update);
		}

	}

	public static Unsafe getUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (Exception e) {
			throw new Error(e);
		}

	}

	private transient volatile Node<E> head;
	private transient volatile Node<E> tail;
	private static final Unsafe unsafe;
	private static final long headOffset;
	private static final long tailoffset;

	static {
		unsafe = getUnsafe();
		try {
			headOffset = unsafe.objectFieldOffset(MConcurrentLinkedQueue.class
					.getDeclaredField("head"));
			tailoffset = unsafe.objectFieldOffset(MConcurrentLinkedQueue.class
					.getDeclaredField("tail"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	private boolean casHead(Node<E> expect, Node<E> update) {
		return unsafe.compareAndSwapObject(this, headOffset, expect, update);
	}

	private boolean casTail(Node<E> expect, Node<E> update) {
		return unsafe.compareAndSwapObject(this, tailoffset, expect, update);
	}

	public MConcurrentLinkedQueue() {
		head = tail = new Node<E>(null);
	}

	public MConcurrentLinkedQueue(MCollection<? extends E> c) {
		Node<E> h = null, t = null;
		for (E e : c) {
			checkNotNull(e);
			Node<E> node = new Node<E>(e);
			if (h == null) {
				h = node;
				t = node;
			} else {
				t.lazySetNext(node);
				t = node;
			}
		}
		if (h == null) {
			h = t = new Node<E>(null);
		}
		head = h;
		tail = t;
	}

	@Override
	public boolean add(E e) {
		return offer(e);
	}

	final void updateHead(Node<E> h, Node<E> p) {
		if (h != p && casHead(h, p)) {
			h.lazySetNext(h);
		}
	}

	final Node<E> succ(Node<E> p) {
		Node<E> next = p.next;
		return p == next ? head : next;
	}

	@Override
	public boolean offer(E e) {
		Node<E> newNode = new Node<E>(e);
		for (Node<E> t = tail, p = t;;) {
			Node<E> q = p.next;
			if (q == null) {
				if (t.casNext(null, newNode)) {
					if (p != t) {
						casTail(t, newNode);
					}
				}
				return true;
			} else if (p == q) {
				Node<E> temp = t;
				t = tail;
				if (t != temp) {
					p = t;
				} else {
					p = head;
				}
			} else {
				if (p == t) {
					p = q;
				} else {
					Node<E> temp = t;
					t = tail;
					if (temp != t) {
						p = t;
					}
				}
			}
		}

	}

	@Override
	public E poll() {
		restartFromHead: for (;;) {
			for (Node<E> h = head, p = h, q;;) {
				E item = p.item;
				if (item != null && p.casItem(item, null)) {
					if (p != h) {
						updateHead(h, ((q = p.next) != null ? q : p));
					}
				} else if ((q = p.next) == null) {
					updateHead(h, p);
					return null;
				} else if (p == q) {
					continue restartFromHead;
				} else {
					p = q;
				}
			}
		}
	}

	@Override
	public E peek() {
		restartFromeHead: for (;;) {
			for (Node<E> h = head, p = h, q;;) {
				E item = p.item;
				if (item != null || (q = p.next) == null) {
					updateHead(h, p);
					return item;
				} else if (p == q) {
					continue restartFromeHead;
				} else {
					p = q;
				}
			}
		}

	}

	/**
	 * like peek but return the node
	 * 
	 * @return
	 */
	Node<E> first() {
		restartFromHead: for (;;) {
			for (Node<E> h = head, p = h, q;;) {
				boolean hasItem = (p.item != null);
				if (hasItem || (q = p.next) == null) {
					updateHead(h, p);
					return hasItem ? p : null;
				} else if (p == q)
					continue restartFromHead;
				else
					p = q;
			}
		}
	}

	public boolean isEmpty() {
		return first() == null;
	}

	@Override
	public int size() {
		int count = 0;
		for (Node<E> p = first(); p != null; p = succ(p))
			if (p.item != null)
				// Collection.size() spec says to max out
				if (++count == Integer.MAX_VALUE)
					break;
		return count;
	}

	public boolean contains(Object o) {
		if (o == null)
			return false;
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null && o.equals(item))
				return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		if (o == null)
			return false;
		Node<E> pred = null;
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null && o.equals(item) && p.casItem(item, null)) {
				Node<E> next = succ(p);
				if (pred != null && next != null)
					pred.casNext(p, next);
				return true;
			}
			pred = p;
		}
		return false;
	}

	@Override
	public boolean addAll(MCollection<? extends E> c) {
		if (c == this) {
			throw new IllegalArgumentException();
		}

		// 把集合先复制到一个链表中
		Node<E> begin = null, last = null;
		for (E e : c) {
			Node<E> newNode = new Node<E>(e);
			if (begin == null) {
				begin = last = newNode;
			}
			last.lazySetNext(newNode);
			last = newNode;
		}
		if (begin == null) {
			return false;
		}

		// 把链表加入并发队列中
		for (Node<E> t = tail, p = t;;) {
			Node<E> q = p.next;
			if (q == null) {
				if (p.casNext(null, begin)) {
					if (!casTail(t, last)) {
						// 在尝试一遍，可能是因为其他线程的增加，导致tail已经发生改变
						t = tail;
						if (last.next == null) {
							casTail(t, last);
						}
					}
				}
			} else if (p == q) {
				Node<E> temp = t;
				t = tail;
				if (t != temp) {
					p = t;
				} else {
					p = head;
				}
			} else {
				if (p == t) {
					p = q;
				} else {
					Node<E> temp = t;
					t = tail;
					if (temp != t) {
						p = t;
					}
				}
			}
		}

	}

	public Object[] toArray() {
		// Use ArrayList to deal with resizing.
		MArrayList<E> al = new MArrayList<E>();
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null)
				al.add(item);
		}
		return al.toArray();
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		// try to use sent-in array
		int k = 0;
		Node<E> p;
		for (p = first(); p != null && k < a.length; p = succ(p)) {
			E item = p.item;
			if (item != null)
				a[k++] = (T) item;
		}
		if (p == null) {
			if (k < a.length)
				a[k] = null;
			return a;
		}

		// If won't fit, use ArrayList version
		MArrayList<E> al = new MArrayList<E>();
		for (Node<E> q = first(); q != null; q = succ(q)) {
			E item = q.item;
			if (item != null)
				al.add(item);
		}
		return al.toArray(a);
	}

	@Override
	public MIterator<E> iterator() {
		// TODO Auto-generated method stub
		return new Itr();
	}

	private class Itr implements MIterator<E> {
		/**
		 * Next node to return item for.
		 */
		private Node<E> nextNode;

		/**
		 * nextItem holds on to item fields because once we claim that an
		 * element exists in hasNext(), we must return it in the following
		 * next() call even if it was in the process of being removed when
		 * hasNext() was called.
		 */
		private E nextItem;

		/**
		 * Node of the last returned item, to support remove.
		 */
		private Node<E> lastRet;

		Itr() {
			advance();
		}

		/**
		 * Moves to next valid node and returns item to return for next(), or
		 * null if no such.
		 */
		private E advance() {
			lastRet = nextNode;
			E x = nextItem;

			Node<E> pred, p;
			if (nextNode == null) {
				p = first();
				pred = null;
			} else {
				pred = nextNode;
				p = succ(nextNode);
			}

			for (;;) {
				if (p == null) {
					nextNode = null;
					nextItem = null;
					return x;
				}
				E item = p.item;
				if (item != null) {
					nextNode = p;
					nextItem = item;
					return x;
				} else {
					// skip over nulls
					Node<E> next = succ(p);
					if (pred != null && next != null)
						pred.casNext(p, next);
					p = next;
				}
			}
		}

		public boolean hasNext() {
			return nextNode != null;
		}

		public E next() {
			if (nextNode == null)
				throw new NoSuchElementException();
			return advance();
		}

		public void remove() {
			Node<E> l = lastRet;
			if (l == null)
				throw new IllegalStateException();
			// rely on a future traversal to relink.
			l.item = null;
			lastRet = null;
		}
	}

	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {

		// Write out any hidden stuff
		s.defaultWriteObject();

		// Write out all elements in the proper order.
		for (Node<E> p = first(); p != null; p = succ(p)) {
			Object item = p.item;
			if (item != null)
				s.writeObject(item);
		}

		// Use trailing null as sentinel
		s.writeObject(null);
	}

	/**
	 * Reconstitutes the instance from a stream (that is, deserializes it).
	 * 
	 * @param s
	 *            the stream
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		// Read in elements until trailing null sentinel found
		Node<E> h = null, t = null;
		Object item;
		while ((item = s.readObject()) != null) {
			@SuppressWarnings("unchecked")
			Node<E> newNode = new Node<E>((E) item);
			if (h == null)
				h = t = newNode;
			else {
				t.lazySetNext(newNode);
				t = newNode;
			}
		}
		if (h == null)
			h = t = new Node<E>(null);
		head = h;
		tail = t;
	}

	public void checkNotNull(Object e) {
		if (e == null) {
			throw new NullPointerException();
		}
	}

}
