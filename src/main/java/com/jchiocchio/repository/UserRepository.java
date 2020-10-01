package com.jchiocchio.repository;

import java.util.Optional;

import com.jchiocchio.model.User;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User> {

    Optional<User> findByUsername(String username);
}
