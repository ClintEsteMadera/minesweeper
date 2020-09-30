package com.jchiocchio.entityfactory;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestEntitiesRepository {

    @Autowired
    private EntityManager entityManager;

    public <E> E save(E entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public <E> void persist(E entity) {
        entityManager.persist(entity);
    }

    public <E> void flush() {
        entityManager.flush();
    }

    public <T> T getById(UUID uuid, Class<T> clazz){
        return entityManager.find(clazz, uuid);
    }

    public <T> T getById(String uuid, Class<T> clazz){
        return this.getById(UUID.fromString(uuid), clazz);
    }

    public <T> void detach(T entity) {
        entityManager.detach(entity);
    }
}
