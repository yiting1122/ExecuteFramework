package com.yiting.test;

import org.junit.Test;

import com.yiting.concurrent.MSemaphore;

public class MSemaphoreTest {

	public static void main(String[]args){
		(new MSemaphoreTest()).testMSemphore();
	}
	@Test
	public void testMSemphore() {
		Task task=new Task(3, 30);
		task.execute();
	}

	public final class Task {
		MSemaphore mSemaphore;
		int task;

		public Task(int permits, int task) {
			mSemaphore = new MSemaphore(permits);
			this.task = task;
		}

		public void execute() {
			Thread[] threads = new Thread[task];
			for (int i = 0; i < task; i++) {
				threads[i] = new Thread() {
					public void run() {
						try {
							mSemaphore.acquire();
							System.out.println(Thread.currentThread()+" available :"+mSemaphore.availablePermits());
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							mSemaphore.release();
						}
					}
				};
			}
			
			for(Thread t:threads){
				t.start();
			}
		}

	}
}
