package com.nightsky.cryptonate.event;

import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 *
 * @author Chris
 */
public class HibernatePreUpdateEvent implements HibernateEvent {

    private final PreUpdateEvent event;

    public HibernatePreUpdateEvent(PreUpdateEvent event) {
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
