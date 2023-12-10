package com.replaymod.render.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public class JailingQueue<T> extends PriorityBlockingQueue<T> {
	private final PriorityBlockingQueue<T> delegate;
	private final Set<Thread> jailed = new HashSet();

	public JailingQueue(PriorityBlockingQueue<T> delegate) {
		this.delegate = delegate;
	}

	public synchronized void jail(int atLeast) {
		while (this.jailed.size() < atLeast) {
			try {
				this.wait();
			} catch (InterruptedException var3) {
				Thread.interrupted();
			}
		}

	}

	public synchronized void free(Thread thread) {
		Preconditions.checkState(this.jailed.remove(thread), "Thread is not jailed.");
		thread.interrupt();
	}

	public synchronized void freeAll() {
		this.jailed.clear();
		this.notifyAll();
	}

	private synchronized void tryAccess() {
		this.jailed.add(Thread.currentThread());
		this.notifyAll();

		while (this.jailed.contains(Thread.currentThread())) {
			try {
				this.wait();
			} catch (InterruptedException var2) {
				Thread.interrupted();
			}
		}

	}

	public Iterator<T> iterator() {
		this.tryAccess();
		return this.delegate.iterator();
	}

	public int size() {
		this.tryAccess();
		return this.delegate.size();
	}

	public void put(T t) {
		this.tryAccess();
		this.delegate.put(t);
	}

	public boolean offer(T t, long timeout, TimeUnit unit) {
		this.tryAccess();
		return this.delegate.offer(t, timeout, unit);
	}

	public T take() throws InterruptedException {
		this.tryAccess();
		return this.delegate.take();
	}

	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		this.tryAccess();
		return this.delegate.poll(timeout, unit);
	}

	public int remainingCapacity() {
		this.tryAccess();
		return this.delegate.remainingCapacity();
	}

	public int drainTo(Collection<? super T> c) {
		this.tryAccess();
		return this.delegate.drainTo(c);
	}

	public int drainTo(Collection<? super T> c, int maxElements) {
		this.tryAccess();
		return this.delegate.drainTo(c, maxElements);
	}

	public boolean offer(T t) {
		this.tryAccess();
		return this.delegate.offer(t);
	}

	public T poll() {
		this.tryAccess();
		return this.delegate.poll();
	}

	public T peek() {
		this.tryAccess();
		return this.delegate.peek();
	}
}
