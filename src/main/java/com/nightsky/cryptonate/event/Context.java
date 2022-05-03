package com.nightsky.cryptonate.event;

import java.io.Serializable;

/**
 *
 * @author Chris
 */
public class Context {

    private Object entity;

    private Serializable id;

    private HibernateEvent event;

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    /**
     * @return the entity
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * @param entity the entity to set
     */
    public void setEntity(Object entity) {
        this.entity = entity;
    }

    /**
     * @return the id
     */
    public Serializable getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Serializable id) {
        this.id = id;
    }

    /**
     * @return the event
     */
    public HibernateEvent getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(HibernateEvent event) {
        this.event = event;
    }

}
