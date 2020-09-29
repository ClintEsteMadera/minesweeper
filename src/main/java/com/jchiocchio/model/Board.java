package com.jchiocchio.model;

import java.util.Arrays;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.google.common.base.Preconditions;
import com.jchiocchio.dto.BoardDTO;
import com.jchiocchio.mapping.DTO;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.annotations.Type;

import lombok.Data;
import lombok.NoArgsConstructor;

import static java.lang.String.format;

@Data
@Embeddable
@NoArgsConstructor
@DTO(BoardDTO.class)
public class Board {

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Cell[][] cells;

    public Board(int rows, int columns, int minesCount) {
        this.cells = this.createCells(rows, columns);
        this.placeMines(minesCount);
        this.recordMinesAround();
    }

    @Transient
    public Cell getCellAt(int row, int column) {
        Preconditions.checkArgument(this.rowIsInRange(row), format("Row %d is not in range", row));
        Preconditions.checkArgument(this.rowIsInRange(column), format("Column %d is not in range", column));

        return this.cells[row][column];
    }

    public void revealCellAndItsAdjacentsRecursively(int row, int column) {
        if (row < 0 || row >= this.rowsCount() || column < 0 || column >= this.columnsCount()) {
            return;
        }
        var cell = this.getCellAt(row, column);

        if (!cell.isMine() && !cell.isFlagged() && !cell.isRevealed()) {
            cell.reveal();
            if (cell.getMinesAround() == 0) {
                revealCellAndItsAdjacentsRecursively(row + 1, column);
                revealCellAndItsAdjacentsRecursively(row + 1, column + 1);
                revealCellAndItsAdjacentsRecursively(row + 1, column - 1);
                revealCellAndItsAdjacentsRecursively(row - 1, column);
                revealCellAndItsAdjacentsRecursively(row - 1, column - 1);
                revealCellAndItsAdjacentsRecursively(row - 1, column + 1);
                revealCellAndItsAdjacentsRecursively(row, column - 1);
                revealCellAndItsAdjacentsRecursively(row, column + 1);
            }
        }
    }

    public void revealAllMines() {
        for (int row = 0; row < this.rowsCount(); row++) {
            for (int col = 0; col < this.columnsCount(); col++) {
                var cell = this.getCellAt(row, col);
                if (cell.isMine() && !cell.isRevealed()) {
                    cell.reveal();
                }
            }
        }
    }

    public boolean allNonMineCellsAreRevealed() {
        return Arrays.stream(this.cells)
                     .flatMap(Arrays::stream)
                     .filter(cell -> !cell.isMine())
                     .allMatch(Cell::isRevealed);
    }
    
    private void recordMinesAround() {
        for (int row = 0; row < this.rowsCount(); row++) {
            for (int col = 0; col < this.columnsCount(); col++) {
                this.cells[row][col].setMinesAround(this.countMinesNearby(row, col));
            }
        }
    }

    private Cell[][] createCells(int rows, int columns) {
        var cells = new Cell[rows][];

        for (int row = 0; row < rows; row++) {
            cells[row] = new Cell[columns];

            for (int col = 0; col < columns; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }
        return cells;
    }

    private void placeMines(int minesCount) {
        int placed = 0;
        var random = new Random();

        while (placed < minesCount) {
            int row = random.nextInt(this.rowsCount());
            int col = random.nextInt(this.columnsCount());

            if (!this.cells[row][col].isMine()) {
                this.cells[row][col].setMine(true);
                placed++;
            }
        }
    }

    private int countMinesNearby(int row, int col) {
        int mines = 0;
        for (int r : new int[] {row - 1, row, row + 1}) {
            mines += minesAt(r, col - 1);
            mines += minesAt(r, col + 1);
        }
        mines += minesAt(row - 1, col);
        mines += minesAt(row + 1, col);

        return mines;
    }

    private int minesAt(int row, int col) {
        return BooleanUtils.toInteger(rowIsInRange(row) && columnIsInRange(col) && this.cells[row][col].isMine());
    }

    private boolean rowIsInRange(int row) {
        return row >= 0 && row < this.rowsCount();
    }

    private boolean columnIsInRange(int col) {
        return col >= 0 && col < this.columnsCount();
    }

    private int rowsCount() {
        return this.cells.length;
    }

    private int columnsCount() {
        return this.cells[0].length;
    }
}
