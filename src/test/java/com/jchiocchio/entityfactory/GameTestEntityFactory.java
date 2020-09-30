package com.jchiocchio.entityfactory;

import com.jchiocchio.model.Board;
import com.jchiocchio.model.Game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.experimental.Accessors;

@Component
public class GameTestEntityFactory extends BaseTestEntityFactory<Game> {

    @Autowired
    private BoardTestEntityFactory boardTestEntityFactory;

    public Builder builder() {
        return new Builder();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    public class Builder {

        private String name;
        
        private Board board;

        public Game build() {
            Game game = new Game();

            if (this.name == null) {
                this.name = "default game";
            }
            if (this.board == null) {
                this.board = boardTestEntityFactory.builder().build();
            }
            game.setBoard(this.board);
    
            repository.save(game);

            return game;
        }
    }
}
