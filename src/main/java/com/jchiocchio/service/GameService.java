package com.jchiocchio.service;

import java.util.UUID;

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

    public Game createGame(GameCreationData creationData) {
        log.trace("Creating board for game {}...", creationData.getName());
        var board = new Board(creationData.getRowsCount(), creationData.getColumnsCount(), creationData.getMinesCount());

        Game game = gameRepository.save(Game.builder().name(creationData.getName()).board(board).build());
        log.info("Game {} created", creationData.getName());

        return game;
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

        return gameRepository.save(game);
    }
}
