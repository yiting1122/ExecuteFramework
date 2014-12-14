package com.yiting.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class MAbstractExecutorService implements MExecutorService {

	protected <T> MRunnableFuture<T> newTaskFor(Runnable runnable,T value){
		return new
	}

}
