package com.nightsky.cryptonate.event;

import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 *
 * @author Chris
 */
public class HibernatePostLoadEvent implements HibernateEvent {

    private final PostLoadEvent event;

    public HibernatePostLoadEvent(PostLoadEvent event) {
        this.event = event;
    }

    @Override
    public Object[] getState() {
        return null;
    }

    @Override
    public EntityPersister getPersister() {
        return event.getPersister();
    }

}
