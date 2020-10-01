package com.jchiocchio.service;

import com.jchiocchio.dto.UserDTO;
import com.jchiocchio.model.User;
import com.jchiocchio.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(UserDTO userDTO) {
        var username = userDTO.getUsername();

        checkArgument(!this.doesUserExist(username), format("User %s already exists", username));

        log.info("Creating user {}...", username);

        return userRepository.saveAndFlush(User.builder().username(username).build());
    }

    public boolean doesUserExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
