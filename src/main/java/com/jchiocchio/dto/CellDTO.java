package com.jchiocchio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jchiocchio.model.Flag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CellDTO {

    private int row;

    private int column;

    private boolean mine;

    private boolean revealed;

    private Flag flag;

    private int minesAround;
}
