package com.jchiocchio.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.jchiocchio.dto.UserDTO;
import com.jchiocchio.mapping.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@DTO(UserDTO.class)
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String username;
}
