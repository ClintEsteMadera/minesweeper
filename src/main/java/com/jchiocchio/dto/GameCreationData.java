package com.jchiocchio.dto;

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameCreationData {

    @NotEmpty(message = "'name' must not be empty")
    private String name;

    @Min(value = 2, message = "'rowsCount' must be at least 2")
    @Max(value = 16, message = "'rowsCount' must be at most 16")
    private int rowsCount;

    @Min(value = 2, message = "'columnsCount' must be at least 2")
    @Max(value = 30, message = "'columnsCount' must be at most 30")
    private int columnsCount;

    @Min(value = 1, message = "'minesCount' must be at least 1")
    @Max(value = 435, message = "'minesCount' must be at most 435")
    private int minesCount;

    @JsonIgnore
    @AssertTrue(message = "Invalid number of mines requested", groups = AfterDefaultValidationGroup.class)
    public boolean isNumberOfMinesValid() {
        return this.minesCount <= (this.rowsCount - 1) * (this.columnsCount - 1);
    }

    /**
     * Defines an interface to assign custom validations that need to take place *after* the default -field-centric-
     * ones run.
     */
    public interface AfterDefaultValidationGroup {
        // marker interface
    }

    @GroupSequence({Default.class, AfterDefaultValidationGroup.class})
    public interface ValidationGroup {
        // marker interface
    }
}
