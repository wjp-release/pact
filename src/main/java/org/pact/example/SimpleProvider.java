package org.pact.example;

import org.pact.provider.ConcurrentProvider;
import org.pact.shared.Pact;
import org.pact.shared.Promise;
import org.pact.shared.Request;

public class SimpleProvider extends ConcurrentProvider {
    private SimpleProvider()
    {
        super(2);
    }
    private static SimpleProvider inst=new SimpleProvider();
    public static SimpleProvider instance()
    {
        return inst;
    }
    @Override
    protected void handle(Request request, Pact pact)
    {
        SimpleRequest simple_request = (SimpleRequest) request;
        int input = simple_request.get_input();
        Promise first_promise = pact.get_promise(0);
        // try do something with the input, fulfill or reject the promise
        try {
            input-=100;
            if (input > 0) {
                first_promise.fulfill(Integer.valueOf(input));
            } else {
                first_promise.reject(new Exception("input too small!"));
            }
        }catch(Exception e)
        {
            first_promise.reject(e); // reject if any exception caught
        }
    }
}
