package com.jchiocchio.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameUpdate {

    @Min(value = 0, message = "'row' must be at least 0")
    @Max(value = 15, message = "'row' must be at most 29")
    private int row;

    @Min(value = 0, message = "'column' must be at least 0")
    @Max(value = 29, message = "'column' must be at most 29")
    private int column;

    @NotNull(message = "'cellUpdateAction' is required")
    private CellUpdateAction cellUpdateAction;
}
