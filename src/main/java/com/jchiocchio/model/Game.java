package com.jchiocchio.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.jchiocchio.dto.GameDTO;
import com.jchiocchio.mapping.DTO;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import org.hibernate.annotations.TypeDef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DTO(GameDTO.class)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Game {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String name;

    @Column
    @Embedded
    private Board board;

    @Column
    @Enumerated(EnumType.STRING)
    private GameOutcome outcome;
}
