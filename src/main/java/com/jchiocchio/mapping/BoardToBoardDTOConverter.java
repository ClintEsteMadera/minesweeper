package com.jchiocchio.mapping;

import java.util.Arrays;

import com.jchiocchio.dto.BoardDTO;
import com.jchiocchio.dto.CellDTO;
import com.jchiocchio.model.Board;
import com.jchiocchio.model.Cell;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public class BoardToBoardDTOConverter extends CustomConverter<Board, BoardDTO> {

    @Override
    public BoardDTO convert(Board source, Type<? extends BoardDTO> destinationType, MappingContext mappingContext) {
        return BoardDTO.builder()
                       .cells(Arrays.stream(source.getCells()).map(this::toCellDTOs).toArray(CellDTO[][]::new))
                       .build();
    }

    private CellDTO[] toCellDTOs(Cell[] rows) {
        return Arrays.stream(rows).map(this::toCellDTO).toArray(CellDTO[]::new);
    }

    private CellDTO toCellDTO(Cell cell) {
        return CellDTO.builder()
                      .row(cell.getRow())
                      .column(cell.getColumn())
                      .flag(cell.getFlag())
                      .mine(cell.isMine())
                      .minesAround(cell.getMinesAround())
                      .revealed(cell.isRevealed())
                      .build();
    }
}
