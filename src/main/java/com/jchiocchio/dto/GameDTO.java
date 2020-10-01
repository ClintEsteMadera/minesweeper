package com.jchiocchio.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jchiocchio.model.GameOutcome;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDTO {

    private UUID id;

    private String name;

    private BoardDTO board;

    private LocalDateTime created;

    private LocalDateTime modified;

    private GameOutcome outcome;
}
