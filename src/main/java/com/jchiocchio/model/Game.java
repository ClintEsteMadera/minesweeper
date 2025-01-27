package com.jchiocchio.model;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jchiocchio.dto.GameDTO;
import com.jchiocchio.mapping.DTO;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "games")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DTO(GameDTO.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Game {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Embedded
    @Column(nullable = false)
    private Board board;

    @Column(nullable = false)
    private LocalDateTime created;

    @Column
    private LocalDateTime modified;
    
    @Column
    @Enumerated(EnumType.STRING)
    private GameOutcome outcome;

    @JsonIgnore
    public boolean isFinished() {
        return this.outcome != null;
    }

    @PrePersist
    public void prePersist() {
        created = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        modified = LocalDateTime.now();
    }
}
