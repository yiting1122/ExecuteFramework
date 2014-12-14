package com.yiting.concurrent.locks;

import java.io.Serializable;

public abstract class MAbstarctOwnableSynchronizer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337637757280101496L;
	
	protected MAbstarctOwnableSynchronizer(){
	}
	
	private transient Thread exclusiveOwnerThread;

	public Thread getExclusiveOwnerThread() {
		return exclusiveOwnerThread;
	}

	public void setExclusiveOwnerThread(Thread t) {
		this.exclusiveOwnerThread = t;
	}
	
	
	
}
