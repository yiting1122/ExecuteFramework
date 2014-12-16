package com.yiting.atomic;

import java.io.Serializable;
import sun.misc.Unsafe;

public class MAtomicBoolean implements Serializable {
	private static final long serialVersionUID = 4899833938999259474L;
	private static final Unsafe unsafe = Unsafe.getUnsafe();
	private static final long valueOffset;
	static {
		try {
			valueOffset = unsafe.objectFieldOffset(MAtomicInteger.class
					.getDeclaredField("value"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	private volatile int value;

	public MAtomicBoolean(boolean initValue) {
		value = initValue ? 1 : 0;
	}

	public MAtomicBoolean() {
	}

	public final boolean get() {
		return value != 0;
	}

	public final boolean compareAndSet(boolean expect, boolean update) {
		int e = expect ? 1 : 0;
		int u = update ? 1 : 0;
		return unsafe.compareAndSwapInt(this, valueOffset, e, u);
	}
	
	 /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * <p>May <a href="package-summary.html#Spurious">fail spuriously</a>
     * and does not provide ordering guarantees, so is only rarely an
     * appropriate alternative to {@code compareAndSet}.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }
    
    public final void set(boolean newValue){
    	value=newValue?1:0;
    }
	
    public final void lazySet(boolean newValue){
    	int v=newValue?1:0;
    	unsafe.putOrderedInt(this, valueOffset, v);
    }
    
    public final boolean getAndSet(boolean newValue){
    	for(;;){
    		boolean current=get();
    		if(compareAndSet(current, newValue)){
    			return current;
    		}
    	}
    }
    
    public String toString() {
        return Boolean.toString(get());
    }
    

}
