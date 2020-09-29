package com.jchiocchio.model;

import java.io.Serializable;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jchiocchio.dto.CellDTO;
import com.jchiocchio.mapping.DTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DTO(CellDTO.class)
public class Cell implements Serializable {
    
    private int row;

    private int column;

    private boolean mine;

    /**
     * Private. Only to be used by Hibernate. Use {@link #reveal()} instead.
     */
    @Setter(AccessLevel.PRIVATE)
    private boolean revealed;

    /**
     * Private. Only to be used by Hibernate. Use {@link #flag(Flag)} ()} instead.
     */
    @Setter(AccessLevel.PRIVATE)
    private Flag flag;

    private int minesAround;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @JsonIgnore
    @Transient
    public boolean isFlagged() {
        return this.flag != null;
    }

    /**
     * Intentionally not following Java Beans convention, as we want to keep the setter free of preconditions, as that
     * is what Hibernate uses to hydrate a Cell object out of a JSON string persisted in the DB.
     */
    @Transient
    public void reveal() {
        checkArgument(!this.isRevealed(), "Cannot reveal and already revealed cell");
        this.revealed = true;
    }

    /**
     * Intentionally not following Java Beans convention, as we want to keep the setter free of preconditions, as that
     * is what Hibernate uses to hydrate a Cell object out of a JSON string persisted in the DB.
     */
    @Transient
    public void flag(Flag flag) {
        if (flag == null) {
            checkArgument(this.isFlagged(), "Cannot unflag an unflagged cell.");
        } else {
            checkArgument(!this.isRevealed(), "Cannot flag a revealed cell");
        }
        this.flag = flag;
    }
}
