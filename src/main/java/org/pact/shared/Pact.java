package org.pact.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Pact {
    private List<Promise> promises = new ArrayList<>();
    private AtomicBoolean cancelled = new AtomicBoolean(false);

    public Pact(Request request) {
        for (int i = 0; i < request.nr_steps(); i++) {
            promises.add(new Promise());
        }
    }

    public boolean is_cancelled()
    {
        return cancelled.get();
    }

    public void cancel()
    {
        cancelled.set(true);
    }

    public int nr_of_promises()
    {
        return promises.size();
    }

    public int state_of_promise(int index)
    {
        return promises.get(index).state.get();
    }

    public Promise get_promise(int index){
        return promises.get(index);
    }

}

