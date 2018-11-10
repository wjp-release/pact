package org.pact.consumer;

import org.pact.shared.Pact;
import org.pact.shared.Promise;

// consumer is allowed to watch, wait or cancel the progress of certain promises in the shared
public class ConsumerPact {
    private Pact pact;

    public ConsumerPact(Pact pact)
    {
        this.pact=pact;
    }

    public int nr_of_promises()
    {
        return pact.nr_of_promises();
    }

    public int state_of_promise(int index)
    {
        return pact.state_of_promise(index);
    }

    // block untill the index-th promise fulfilled/rejected
    public Object wait(int index) throws Exception{
        Promise promise=pact.get_promise(index);
        promise.lock.lock();
        while(promise.state.get() == Promise.pending){
            promise.conditon.awaitUninterruptibly();
        }
        if(promise.state.get()==Promise.fulfilled){
            return promise.value;
        }else{
            throw promise.reason;
        }
    }

    public void cancel()
    {
        pact.cancel();
    }

    public void cancel(int index){
        pact.get_promise(index).canceled.set(true);
    }
}
