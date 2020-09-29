package com.jchiocchio.repository;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * This generic JPA Repository intends to be a go-to place for all kind of operations that are common to multiple JPA
 * entities.
 *
 * @param <T> the type of Entity this repository will deal with.
 */
@NoRepositoryBean
public interface GenericRepository<T> extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {

    /**
     * Finds an entity by ID, or throws a {@link EntityNotFoundException} if it does not exist.
     *
     * @param id the ID of the entity to be found
     *
     * @return the entity matching the ID passed in.
     * @throws EntityNotFoundException if there is no such entity matching the ID passed as parameter.
     */
    default T findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(id.toString() + " not found"));
    }
}
