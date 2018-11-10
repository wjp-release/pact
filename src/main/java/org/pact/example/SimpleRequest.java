package org.pact.example;

import org.pact.consumer.ConsumerPact;
import org.pact.consumer.EmptyRequest;
import org.pact.shared.Pact;


public class SimpleRequest extends EmptyRequest {
    private int input;
    SimpleRequest(int input)
    {
        this.input=input;
    }

    public int get_input()
    {
        return input;
    }

    @Override
    public ConsumerPact submit()
    {
        return new ConsumerPact(SimpleProvider.instance().accept(this));
    }

}
