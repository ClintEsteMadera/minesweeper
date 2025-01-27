package com.jchiocchio.controller;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.jchiocchio.dto.GameCreationData;
import com.jchiocchio.dto.GameUpdate;
import com.jchiocchio.model.Game;
import com.jchiocchio.service.GameService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Tag(name = "Games")
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "Creates a new game for an existing User")
    public Game createGame(
        @RequestBody @Validated(GameCreationData.ValidationGroup.class) GameCreationData creationData) {
        return gameService.createGame(creationData);
    }

    @PatchMapping("{gameId}")
    @Operation(description = "Updates an existing Game, by performing individual updates to the board's cells")
    public Game updateGame(@PathVariable @NotNull(message = "'id' is required") UUID gameId,
                           @RequestBody @Valid GameUpdate update) {
        return gameService.updateGame(gameId, update);
    }

    @GetMapping("{gameId}")
    @Operation(description = "Finds a Game by ID")
    public Game findGameById(@PathVariable @NotNull(message = "'id' is required") UUID gameId) {
        return gameService.findGameById(gameId);
    }
}
