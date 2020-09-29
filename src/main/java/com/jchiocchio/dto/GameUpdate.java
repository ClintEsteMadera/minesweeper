package com.jchiocchio.dto;

import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameUpdate {

    @NotNull(message = "'id' is required")
    private UUID id;

    @Min(2)
    @Max(16)
    private int row;

    @Min(2)
    @Max(30)
    private int column;

    @NotNull(message = "'cellUpdateAction' is required")
    private CellUpdateAction cellUpdateAction;
}
