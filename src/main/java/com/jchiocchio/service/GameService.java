package com.jchiocchio.service;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import com.jchiocchio.dto.GameCreationData;
import com.jchiocchio.dto.GameUpdate;
import com.jchiocchio.model.Board;
import com.jchiocchio.model.Game;
import com.jchiocchio.repository.GameRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

@Slf4j
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserService userService;

    public Game createGame(GameCreationData creationData) {
        var username = creationData.getUsername();

        log.info("Creating board for game {}...", username);

        if (!userService.doesUserExist(username)) {
            throw new EntityNotFoundException(format("User %s does not exist", username));
        }
        var board =
            new Board(creationData.getRowsCount(), creationData.getColumnsCount(), creationData.getMinesCount());

        Game game = gameRepository.save(Game.builder().username(username).board(board).build());

        log.info("Game {} created", username);

        return game;
    }

    public Game findGameById(UUID gameId) {
        return gameRepository.findByIdOrThrow(gameId);
    }

    public Game updateGame(UUID gameId, GameUpdate gameUpdate) {
        var game = gameRepository.findByIdOrThrow(gameId);

        if (game.isFinished()) {
            throw new IllegalArgumentException(format("Cannot update a game that is already finished (%s)",
                                                      game.getOutcome()));
        }

        var board = game.getBoard();
        var cell = board.getCellAt(gameUpdate.getRow(), gameUpdate.getColumn());

        gameUpdate.getCellUpdateAction().apply(cell, game);

        return gameRepository.saveAndFlush(game);
    }
}
