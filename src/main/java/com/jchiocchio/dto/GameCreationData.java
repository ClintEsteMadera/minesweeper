package com.jchiocchio.dto;

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.groups.Default;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameCreationData {

    @NotEmpty
    private String name;

    @Min(2)
    @Max(16)
    private int rows;

    @Min(2)
    @Max(30)
    private int columns;

    @Min(1)
    @Max(435)
    private int minesCount;

    @AssertTrue(message = "The number of minesToo many data records requested. Please check your date period " +
                          "and/or resolution and reduce your query to a max of 10,080 records",
                groups = AfterDefaultValidationGroup.class)
    public boolean isNumberOfMinesValid() {
        return this.minesCount <= (this.rows - 1) * (this.columns - 1);
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
