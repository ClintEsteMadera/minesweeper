package com.jchiocchio.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;
import com.jchiocchio.dto.CellUpdateAction;
import com.jchiocchio.dto.GameCreationData;
import com.jchiocchio.dto.GameUpdate;
import com.jchiocchio.model.Cell;
import com.jchiocchio.model.Game;
import com.jchiocchio.model.GameOutcome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MvcResult;

import lombok.SneakyThrows;

import static com.jchiocchio.dto.CellUpdateAction.ADD_QUESTION_MARK;
import static com.jchiocchio.dto.CellUpdateAction.ADD_RED_FLAG;
import static com.jchiocchio.dto.CellUpdateAction.REVEAL;
import static com.jchiocchio.dto.CellUpdateAction.UNFLAG;
import static com.jchiocchio.entityfactory.GameTestEntityFactory.DEFAULT_USERNAME;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_PATH = "/api/games/";

    private static final Integer VALID_ROWS_COUNT = 3;

    private static final Integer VALID_ROW = 1; // this one has an implicit relationship with VALID_ROWS_COUNT

    private static final Integer VALID_COLUMNS_COUNT = 3;

    private static final Integer VALID_COLUMN = 1; // this one has an implicit relationship with VALID_COLUMNS_COUNT

    private static final Integer VALID_MINE_COUNT = 3;

    private static final String MIN_MSG_PATTERN = "'%s' must be at least %d";

    private static final String MAX_MSG_PATTERN = "'%s' must be at most %d";

    private static final String ROW_NOT_IN_RANGE_MSG_PATTERN = "Row %d is not in range";

    private static final String COLUMN_NOT_IN_RANGE_MSG_PATTERN = "Column %d is not in range";

    private static final String NUMBER_OF_MINES_MSG = "Invalid number of mines requested";

    private static final String LOST_GAME_MSG = "Cannot update a game that is already finished (LOST)";

    private static final String FLAG_A_REVEALED_CELL_MSG = "Cannot flag a revealed cell";

    private static final String REVEAL_AND_ALREADY_REVEALED_CELL_MSG = "Cannot reveal and already revealed cell";

    private static final String CANNOT_UNFLAG_AN_UNFLAGGED_CELL_MSG = "Cannot unflag an unflagged cell";

    @Override
    String getBasePath() {
        return BASE_PATH;
    }

    @Test
    void createGame_validInput_returnsCreatedGame() throws Exception {
        var gameCreationData = GameCreationData.builder()
                                               .username(DEFAULT_USERNAME)
                                               .rowsCount(VALID_ROWS_COUNT)
                                               .columnsCount(VALID_COLUMNS_COUNT)
                                               .minesCount(VALID_MINE_COUNT)
                                               .build();

        post()
            .withContent(gameCreationData)
            .withExpectedStatus(status().isCreated())
            .andExpect(jsonPath("$.id", is(notNullValue())))
            .andExpect(jsonPath("$.username", is(gameCreationData.getUsername())))
            .andExpect(jsonPath("$.modified").doesNotExist())
            .andExpect(jsonPath("$.outcome").doesNotExist())
            .andExpect(jsonPath("$.board.rowsCount", is(gameCreationData.getRowsCount())))
            .andExpect(jsonPath("$.board.columnsCount", is(gameCreationData.getColumnsCount())))
            .andExpect(jsonPath("$.board.minesCount", is(gameCreationData.getMinesCount())))
            .perform()
            .andDo(mvcResult -> expectTimestampToBeWithin(20, SECONDS, now(), mvcResult, "$.created"));
    }

    @ParameterizedTest
    @MethodSource("invalidGameCreationData")
    void createGame_invalidInput_throwsBadRequest(String username, Integer rows, Integer columns, Integer minesCount,
                                                  String expectedErrorMessage) {
        var gameCreationData = GameCreationData.builder()
                                               .username(username)
                                               .rowsCount(rows)
                                               .columnsCount(columns)
                                               .minesCount(minesCount)
                                               .build();

        post()
            .withContent(gameCreationData)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(expectedErrorMessage)
            .perform();
    }

    @Test
    void updateGame_revealingAnEmptyCell_updatesGameAsExpected() throws Exception {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // this should reveal everything except where the mine is at (0,0), thus, marking the game as "WON"
        var gameUpdate = GameUpdate.builder().row(2).column(2).cellUpdateAction(REVEAL).build();

        //noinspection UnnecessaryLocalVariable (variable aliasing for better readability)
        var expectedGame = game;
        var expectedBoard = expectedGame.getBoard();

        // reveal all cells, except (0,0) 
        Arrays.stream(expectedBoard.getCells())
              .flatMap(Arrays::stream)
              .filter(cell -> !(cell.getRow() == 0 && cell.getColumn() == 0))
              .forEach(Cell::reveal);

        expectedGame.setOutcome(GameOutcome.WON);

        patchById(game.getId())
            .withContent(gameUpdate)
            .withExpectedStatus(status().isOk())
            .andExpectJsonContentOf(expectedGame)
            .perform()
            .andDo(this::expectLastModificationTimestampToHaveBeenRecentlyUpdated);
    }

    @Test
    void updateGame_revealingAMine_marksTheGameAsLost() throws Exception {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // reveal the mine at (0,0) => game over
        var gameUpdate = GameUpdate.builder().row(0).column(0).cellUpdateAction(REVEAL).build();

        //noinspection UnnecessaryLocalVariable (variable aliasing for better readability)
        var expectedGame = game;
        var expectedBoard = expectedGame.getBoard();

        expectedBoard.revealAllMines();
        expectedGame.setOutcome(GameOutcome.LOST);

        patchById(game.getId())
            .withContent(gameUpdate)
            .withExpectedStatus(status().isOk())
            .andExpectJsonContentOf(expectedGame)
            .perform()
            .andDo(this::expectLastModificationTimestampToHaveBeenRecentlyUpdated);
    }

    @Test
    void updateGame_whenGameIsAlreadyFinished_throwsBadRequest() {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // reveal the mine at (0,0) => game over
        var gameUpdate = GameUpdate.builder().row(0).column(0).cellUpdateAction(REVEAL).build();

        patchById(game.getId())
            .withContent(gameUpdate)
            .withExpectedStatus(status().isOk())
            .perform();

        // now attempt another (irrelevant) update, should throw an error, as the game is already finished
        var anotherUpdate = GameUpdate.builder().row(1).column(1).cellUpdateAction(ADD_RED_FLAG).build();

        patchById(game.getId())
            .withContent(anotherUpdate)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(LOST_GAME_MSG)
            .perform();
    }

    @ParameterizedTest
    @MethodSource("coordinatesOutOfRange")
    void updateGame_withRowOrColumnOutOfRange_throwsBadRequest(Integer row, Integer column, String expectedMessage) {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // coordinate out of range => returns 400
        var gameUpdate = GameUpdate.builder().row(row).column(column).cellUpdateAction(REVEAL).build();

        patchById(game.getId())
            .withContent(gameUpdate)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(expectedMessage)
            .perform();
    }

    @ParameterizedTest
    @MethodSource("actionsOverAnAlreadyRevealedCell")
    void updateGame_updatingAnAlreadyRevealedCell_throwsBadRequest(CellUpdateAction action, String expectedMessage) {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // firstly: reveal the mine at (1,1) => should succeed
        var revealCellAtRow1Column1 = GameUpdate.builder().row(1).column(1).cellUpdateAction(REVEAL).build();

        patchById(game.getId())
            .withContent(revealCellAtRow1Column1)
            .withExpectedStatus(status().isOk())
            .perform();
        
        // secondly: attempt to reveal the same cell => returns 400
        var actionOnRevealedCell = GameUpdate.builder().row(1).column(1).cellUpdateAction(action).build();
        
        patchById(game.getId())
            .withContent(actionOnRevealedCell)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(expectedMessage)
            .perform();
    }

    @Test
    void updateGame_unflaggingAnUnflaggedCell_throwsBadRequest() {
        Game game = this.createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero();

        // unflagging an unflagged cell => returns 400
        var unflagUnflaggedCell = GameUpdate.builder().row(1).column(1).cellUpdateAction(UNFLAG).build();

        patchById(game.getId())
            .withContent(unflagUnflaggedCell)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(CANNOT_UNFLAG_AN_UNFLAGGED_CELL_MSG)
            .perform();
    }

    @Test
    void updateGame_nonExistingGame_throwsNotFound() {
        // non-existent Game ID => returns 404
        var validUpdateAction = GameUpdate.builder().row(1).column(1).cellUpdateAction(REVEAL).build();

        patchById(UUID.randomUUID())
            .withContent(validUpdateAction)
            .withExpectedStatus(status().isNotFound())
            .perform();
    }

    /**
     * Creates a new game with the following board:
     * <pre>
     * |-------------------------------|
     * |       | col 0 | col 1 | col 2 |
     * |-------|-------|-------|-------|
     * | row 0 | mine  |   1   |       |
     * | row 1 |   1   |   1   |       |
     * | row 2 |       |       |       |
     * |-------------------------------|
     * </pre>
     */
    private Game createAndPersistABasic3x3GameWithOneMineAtRowZeroColumnZero() {
        var board = boardTestEntityFactory.builder().rows(3).columns(3).mineAt(0, 0).build();
        var game = gameTestEntityFactory.builder().board(board).build();

        // prevents further changes to `game` be persisted, as, during tests execution, the Hibernate session remains
        // open until the test finishes
        this.gameTestEntityFactory.detachEntity(game);

        return game;
    }

    private void expectLastModificationTimestampToHaveBeenRecentlyUpdated(MvcResult mvcResult) {
        this.expectTimestampToBeWithin(20, SECONDS, now(), mvcResult, "$.modified");
    }
    
    @SneakyThrows
    private void expectTimestampToBeWithin(long period, ChronoUnit unit, LocalDateTime from, MvcResult mvcResult,
                                           String propertyPath) {
        String response = mvcResult.getResponse().getContentAsString();
        final LocalDateTime parsedTimestamp = LocalDateTime.parse(JsonPath.parse(response).read(propertyPath));

        assertThat(parsedTimestamp, within(period, unit, from));
    }
    
    private static Stream<Arguments> invalidGameCreationData() {
        return Stream.of(
            Arguments.of(null, VALID_ROWS_COUNT, VALID_COLUMNS_COUNT, VALID_MINE_COUNT, "'username' must not be empty"),

            Arguments.of(DEFAULT_USERNAME,
                         Integer.MIN_VALUE,
                         VALID_COLUMNS_COUNT,
                         VALID_MINE_COUNT,
                         format(MIN_MSG_PATTERN, "rowsCount", 2)),

            Arguments.of(DEFAULT_USERNAME,
                         Integer.MAX_VALUE,
                         VALID_COLUMNS_COUNT,
                         VALID_MINE_COUNT,
                         format(MAX_MSG_PATTERN, "rowsCount", 16)),

            Arguments.of(DEFAULT_USERNAME,
                         VALID_ROWS_COUNT,
                         Integer.MIN_VALUE,
                         VALID_MINE_COUNT,
                         format(MIN_MSG_PATTERN, "columnsCount", 2)),

            Arguments.of(DEFAULT_USERNAME,
                         VALID_ROWS_COUNT,
                         Integer.MAX_VALUE,
                         VALID_MINE_COUNT,
                         format(MAX_MSG_PATTERN, "columnsCount", 30)),

            Arguments.of(DEFAULT_USERNAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT,
                         Integer.MIN_VALUE,
                         format(MIN_MSG_PATTERN, "minesCount", 1)),

            Arguments.of(DEFAULT_USERNAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT,
                         Integer.MAX_VALUE,
                         format(MAX_MSG_PATTERN, "minesCount", 435)),

            Arguments.of(DEFAULT_USERNAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT, VALID_ROWS_COUNT * VALID_COLUMNS_COUNT, NUMBER_OF_MINES_MSG)
        );
    }

    private static Stream<Arguments> coordinatesOutOfRange() {
        return Stream.of(
            Arguments.of(VALID_ROWS_COUNT, VALID_COLUMN, format(ROW_NOT_IN_RANGE_MSG_PATTERN, VALID_ROWS_COUNT)),
            Arguments.of(VALID_ROW, VALID_COLUMNS_COUNT, format(COLUMN_NOT_IN_RANGE_MSG_PATTERN, VALID_COLUMNS_COUNT)));
    }

    private static Stream<Arguments> actionsOverAnAlreadyRevealedCell() {
        return Stream.of(
            Arguments.of(ADD_QUESTION_MARK, FLAG_A_REVEALED_CELL_MSG),
            Arguments.of(ADD_RED_FLAG, FLAG_A_REVEALED_CELL_MSG),
            Arguments.of(REVEAL, REVEAL_AND_ALREADY_REVEALED_CELL_MSG));
    }
}
