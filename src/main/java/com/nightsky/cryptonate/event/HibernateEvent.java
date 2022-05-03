package com.nightsky.cryptonate.event;

import org.hibernate.persister.entity.EntityPersister;

/**
 *
 * @author Chris
 */
public interface HibernateEvent {

    public Object[] getState();

    public EntityPersister getPersister();

}
