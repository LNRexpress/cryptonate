package com.nightsky.cryptonate.event;

import java.io.Serializable;

/**
 *
 * @author Chris
 */
public class ContextBuilder {

    private final Context context;

    public ContextBuilder() {
        context = new Context();
    }

    public ContextBuilder withEntity(Object entity) {
        context.setEntity(entity);
        return this;
    }

    public ContextBuilder withId(Serializable id) {
        context.setId(id);
        return this;
    }

    public ContextBuilder withEvent(HibernateEvent event) {
        context.setEvent(event);
        return this;
    }

    public Context build() {
        return context;
    }

}
