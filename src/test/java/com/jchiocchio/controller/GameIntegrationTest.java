package com.jchiocchio.controller;

import java.util.stream.Stream;

import com.jchiocchio.dto.GameCreationData;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_PATH = "/api/games/";

    private static final Integer VALID_ROWS_COUNT = 3;

    private static final Integer VALID_COLUMNS_COUNT = 3;

    private static final Integer VALID_MINE_COUNT = 3;

    private static final String VALID_GAME_NAME = "some game name";

    private static final String MIN_MESSAGE_PATTERN = "'%s' must be at least %d";

    private static final String MAX_MESSAGE_PATTERN = "'%s' must be at most %d";

    private static final String NUMBER_OF_MINES_MESSAGE = "Invalid number of mines requested";

    @Override
    String getBasePath() {
        return BASE_PATH;
    }

    @Test
    void createGame_validInput_returnsCreatedGame() {
        var gameCreationData = GameCreationData.builder()
                                               .name(VALID_GAME_NAME)
                                               .rowsCount(VALID_ROWS_COUNT)
                                               .columnsCount(VALID_COLUMNS_COUNT)
                                               .minesCount(VALID_MINE_COUNT)
                                               .build();

        post()
            .withContent(gameCreationData)
            .withExpectedStatus(status().isCreated())
            .andExpect(jsonPath("$.id", is(notNullValue())))
            .andExpect(jsonPath("$.name", is(gameCreationData.getName())))
            .andExpect(jsonPath("$.outcome").doesNotExist())
            .andExpect(jsonPath("$.board.rowsCount", is(gameCreationData.getRowsCount())))
            .andExpect(jsonPath("$.board.columnsCount", is(gameCreationData.getColumnsCount())))
            .andExpect(jsonPath("$.board.minesCount", is(gameCreationData.getMinesCount())))
            .perform();
    }

    @ParameterizedTest
    @MethodSource("invalidGameCreationData")
    void createGame_invalidInput_throwsBadRequest(String name, Integer rows, Integer columns, Integer minesCount,
                                                  String expectedErrorMessage) {
        var gameCreationData = GameCreationData.builder()
                                               .name(name)
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

    private static Stream<Arguments> invalidGameCreationData() {
        return Stream.of(
            Arguments.of(null, VALID_ROWS_COUNT, VALID_COLUMNS_COUNT, VALID_MINE_COUNT, "'name' must not be empty"),

            Arguments.of(VALID_GAME_NAME,
                         Integer.MIN_VALUE,
                         VALID_COLUMNS_COUNT,
                         VALID_MINE_COUNT,
                         format(MIN_MESSAGE_PATTERN, "rowsCount", 2)),

            Arguments.of(VALID_GAME_NAME,
                         Integer.MAX_VALUE,
                         VALID_COLUMNS_COUNT,
                         VALID_MINE_COUNT,
                         format(MAX_MESSAGE_PATTERN, "rowsCount", 16)),

            Arguments.of(VALID_GAME_NAME,
                         VALID_ROWS_COUNT,
                         Integer.MIN_VALUE,
                         VALID_MINE_COUNT,
                         format(MIN_MESSAGE_PATTERN, "columnsCount", 2)),

            Arguments.of(VALID_GAME_NAME,
                         VALID_ROWS_COUNT,
                         Integer.MAX_VALUE,
                         VALID_MINE_COUNT,
                         format(MAX_MESSAGE_PATTERN, "columnsCount", 30)),

            Arguments.of(VALID_GAME_NAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT,
                         Integer.MIN_VALUE,
                         format(MIN_MESSAGE_PATTERN, "minesCount", 1)),

            Arguments.of(VALID_GAME_NAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT,
                         Integer.MAX_VALUE,
                         format(MAX_MESSAGE_PATTERN, "minesCount", 435)),

            Arguments.of(VALID_GAME_NAME,
                         VALID_ROWS_COUNT,
                         VALID_COLUMNS_COUNT, VALID_ROWS_COUNT * VALID_COLUMNS_COUNT, NUMBER_OF_MINES_MESSAGE)
        );
    }
}
