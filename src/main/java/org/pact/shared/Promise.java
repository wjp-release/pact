package org.pact.shared;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Promise {
    final public static int pending=0, fulfilled=1, rejected=2;
    public AtomicInteger state=new AtomicInteger(pending);
    public Object value=null;
    public Exception reason=null;
    public Lock lock=new ReentrantLock();
    public Condition conditon=lock.newCondition();
    public AtomicBoolean canceled=new AtomicBoolean(false);

    public void fulfill(Object v)
    {
        lock.lock();
        this.value=v;
        state.set(fulfilled);
        try{
            conditon.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void reject(Exception e)
    {
        lock.lock();
        this.reason=e;
        state.set(rejected);
        try{
            conditon.signalAll();
        }finally {
            lock.unlock();
        }
    }
}
