package com.yiting.test;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

import com.yiting.concurrent.MCyclicBarrier;

public class BarrierTest {
	public static class ComponentThread implements Runnable {
		MCyclicBarrier barrier;
		int ID;
		int[] array;

		public ComponentThread(MCyclicBarrier barrier, int iD, int[] array) {
			this.barrier = barrier;
			ID = iD;
			this.array = array;
		}

		@Override
		public void run() {
			try {
				array[ID] = new Random().nextInt(100);
				System.out.println("Component " + ID + " generates: "
						+ array[ID]);
				// 在这里等待Barrier处
				System.out.println("Component " + ID + " sleep...");
				barrier.await();
				System.out.println("Component " + ID + " awaked...");
				// 计算数据数组中的当前值和后续值
				int result = array[ID] + array[ID + 1];
				System.out.println("Component " + ID + " result: " + result);
			} catch (Exception ex) {
			}
		}

	}
	
	@Test
	 public  void testCyclicBarrier() {  
	        final int[] array = new int[4];  
	        MCyclicBarrier barrier = new MCyclicBarrier(3, new Runnable() {  
	            // 在所有线程都到达Barrier时执行  
	            public void run() {  
	                System.out.println("testCyclicBarrier run...");  
	                array[2] = array[0] + array[1];  
	            }  
	        });  
	  
	        // 启动线程  
	        new Thread(new ComponentThread(barrier, 0, array)).start();  
	        new Thread(new ComponentThread(barrier, 1, array)).start();  
	        new Thread(new ComponentThread(barrier, 2, array)).start(); 
	    } 
	
	
	
	public static void main(String[] args){
		(new BarrierTest()).testCyclicBarrier();
	}
}
