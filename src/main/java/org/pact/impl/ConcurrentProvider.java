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

package org.pact.impl;

import org.pact.PromiseChain;
import org.pact.Provider;
import org.pact.Request;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public abstract class ConcurrentProvider implements Provider, AutoCloseable {

    private ConcurrentLinkedQueue<Request> requests=new ConcurrentLinkedQueue<>();
    private AtomicBoolean shutdown =new AtomicBoolean(false);
    private Lock lock=new ReentrantLock();
    private Condition conditon=lock.newCondition();
    private List<Thread> threads=new ArrayList<>();
    private ConcurrentHashMap<Request,PromiseChain> promise_map;

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
    public PromiseChain accept(Request request)
    {
        var promise_chain=new PromiseChain(request);
        promise_map.put(request, promise_chain);
        requests.offer(request);
        conditon.notify();
        return promise_chain;
    }

    protected abstract void handle(Request request, PromiseChain promise_chain);

    private class ConcurrentRoutine extends Thread {
        @Override
        public void run(){
            while (!shutdown.get()) {
                if(requests.isEmpty()) {
                    try{
                        conditon.await();
                    }catch(InterruptedException e){
                        Thread.currentThread().interrupt();
                    }
                }else{
                    var request=requests.poll();
                    if(request!=null){
                        handle(request, promise_map.get(request));
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
        conditon.notifyAll();
    }


}
