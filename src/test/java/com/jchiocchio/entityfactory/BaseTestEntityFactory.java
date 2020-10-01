package com.jchiocchio.entityfactory;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseTestEntityFactory<E> {

    @Autowired
    protected TestEntitiesRepository repository;

    public <T> void detachEntity(T entity) {
        this.repository.detach(entity);
    }

    public <T> T save(T entity) {
        return this.repository.save(entity);
    }
}
