package com.nightsky.cryptonate.event;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 *
 * @author Chris
 */
public class HibernatePreInsertEvent implements HibernateEvent {

    private final PreInsertEvent event;

    public HibernatePreInsertEvent(PreInsertEvent event) {
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
