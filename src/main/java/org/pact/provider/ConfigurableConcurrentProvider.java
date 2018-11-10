package org.pact.provider;

import org.pact.shared.Pact;
import org.pact.shared.Request;

import java.util.function.BiConsumer;

public class ConfigurableConcurrentProvider extends ConcurrentProvider {

    private BiConsumer<Request, Pact> customized_handle;

    public ConfigurableConcurrentProvider(BiConsumer<Request, Pact> customized_handle)
    {
        super();
        this.customized_handle=customized_handle;
    }

    protected void handle(Request request, Pact pact)
    {
        customized_handle.accept(request,pact);
    }

}
