package com.nightsky.cryptonate.event;

import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 *
 * @author Chris
 */
public class HibernatePreLoadEvent implements HibernateEvent {

    private final PreLoadEvent event;

    public HibernatePreLoadEvent(PreLoadEvent event) {
        this.event = event;
    }

    @Override
    public Object[] getState() {
        return event.getState();
    }

    @Override
    public EntityPersister getPersister() {
        return event.getPersister();
    }

}
