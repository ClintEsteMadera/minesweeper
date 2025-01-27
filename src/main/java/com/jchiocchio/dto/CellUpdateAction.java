package com.jchiocchio.dto;

import com.jchiocchio.model.Cell;
import com.jchiocchio.model.Flag;
import com.jchiocchio.model.Game;
import com.jchiocchio.model.GameOutcome;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public enum CellUpdateAction {
    ADD_QUESTION_MARK {
        public void apply(Cell cell, Game game) {
            cell.flag(Flag.QUESTION_MARK);
        }
    },
    ADD_RED_FLAG {
        public void apply(Cell cell, Game game) {
            cell.flag(Flag.RED_FLAG);
        }
    },
    UNFLAG {
        public void apply(Cell cell, Game game) {
            cell.flag(null);
        }
    },
    REVEAL {
        public void apply(Cell cell, Game game) {
            checkArgument(!cell.isRevealed(), "Cannot reveal and already revealed cell");

            var board = game.getBoard();

            // either revealing all mines or all adjacent cells, will, in particular, also reveal "cell".
            if (cell.isMine()) {
                log.info("[Game {}] Mine Revealed. Game over.", game.getId());
                game.setOutcome(GameOutcome.LOST);
                board.revealAllMines();
            } else {
                board.revealCellAndItsAdjacentsRecursively(cell.getRow(), cell.getColumn());
                if (board.allNonMineCellsAreRevealed()) {
                    log.info("[Game {}] All empty cells were revealed. You win!", game.getId());
                    game.setOutcome(GameOutcome.WON);
                }
            }
        }
    };

    /**
     * Applies the cell update action in the cell.
     *
     * @param cell the cell to apply the update action to.
     * @param game the game this action is taking place at.
     */
    public abstract void apply(Cell cell, Game game);
}
