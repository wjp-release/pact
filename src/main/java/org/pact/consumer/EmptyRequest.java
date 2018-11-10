package org.pact.consumer;

import org.pact.shared.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class EmptyRequest implements Request {
    List<Function<Object,Object>> cbs=new ArrayList<>();

    @Override
    public List<Function<Object,Object>> cb_chain()
    {
        return cbs;
    }

    public abstract ConsumerPact submit();


}
