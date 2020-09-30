package com.jchiocchio.entityfactory;

import java.util.HashSet;
import java.util.Set;

import com.jchiocchio.model.Board;
import com.jchiocchio.model.Cell;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.experimental.Accessors;

@Component
public class BoardTestEntityFactory extends BaseTestEntityFactory<Board> {

    public Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public class Builder {

        private int rows;

        private int columns;

        private Set<Pair<Integer, Integer>> minesLocations = new HashSet<>();
        
        public Builder mineAt(int row, int column) {
            minesLocations.add(Pair.of(row, column));
            return this;
        }
        
        public Board build() {
            var board = new Board(this.rows, this.columns, 0);
            board.setCells(this.buildCells(board));
            return board;
        }

        private Cell[][] buildCells(Board board) {
            var cells = board.getCells();
            this.minesLocations.forEach(location -> cells[location.getLeft()][location.getRight()].setMine(true));
            board.recordMinesAround();
            
            return cells;
        }
    }
}
