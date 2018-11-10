/*
 * MIT License
 *
 * Copyright (c) 2018 jipeng wu
 * <recvfromsockaddr at gmail dot com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.pact.provider;

import org.pact.Provider;
import org.pact.shared.Promise;
import org.pact.shared.Request;
import org.pact.shared.Pact;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public abstract class ConcurrentProvider implements Provider, AutoCloseable {
    protected ConcurrentProvider(){}
    private ConcurrentLinkedQueue<Request> requests=new ConcurrentLinkedQueue<>();
    private AtomicBoolean shutdown =new AtomicBoolean(false);
    private Lock lock=new ReentrantLock();
    private Condition conditon=lock.newCondition();
    private List<Thread> threads=new ArrayList<>();
    private ConcurrentHashMap<Request, Pact> promise_map=new ConcurrentHashMap<>();

    public ConcurrentProvider(int nr_threads)
    {
        for(int i=0;i<nr_threads;i++){
            threads.add(new ConcurrentRoutine());
        }
    }

    public void start()
    {
        for(var thread:threads){
            thread.start();
        }
    }

    @Override
    public Pact accept(Request request)
    {
        lock.lock();
        var pact=new Pact(request);
        promise_map.put(request, pact);
        requests.offer(request);
        System.out.println("wake one!");
        try{
            conditon.signal();
        }finally {
            lock.unlock();
        }
        return pact;
    }

    // expect no exceptions; either operation succeeds or fails, record the status in shared
    protected abstract void handle(Request request, Pact pact);

    private class ConcurrentRoutine extends Thread {
        @Override
        public void run(){
            while (!shutdown.get()) {
                lock.lock();
                if(requests.isEmpty()) {
                    try{
                        System.out.println("awaits...");
                        conditon.await();
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                }else{
                    lock.unlock();
                    var request=requests.poll();
                    if(request!=null){
                        Pact pact=promise_map.get(request);
                        promise_map.remove(request);
                        if(!pact.is_cancelled()){
                            handle(request, pact);
                            // todo: handle promises in a separate threadpool
                            // todo: support error handling chain; now we just reject all pending promises
                            var cbs=request.cb_chain();
                            if(!cbs.isEmpty()){
                                Object result=pact.get_promise(0).value;
                                for(int i=0;i<cbs.size();i++){
                                    Promise promise =pact.get_promise(i+1);
                                    try{
                                        result=cbs.get(i).apply(result);
                                        promise.fulfill(result);
                                    }catch(Exception e){
                                        promise.reject(e);
                                        for(;i<=cbs.size();i++){  // reject following promises with the same reason
                                            Promise following_promise=pact.get_promise(i);
                                            following_promise.reject(e);
                                        }
                                        break;
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void close(){
        requests.clear();
        promise_map.clear();
        shutdown.set(true);
        conditon.signalAll();
    }


}
