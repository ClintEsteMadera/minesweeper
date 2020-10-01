package com.jchiocchio.controller;

import java.util.List;
import java.util.stream.Stream;

import com.jchiocchio.dto.UserDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.jchiocchio.entityfactory.GameTestEntityFactory.DEFAULT_USERNAME;
import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_PATH = "/api/users/";

    private static final String EMAIL_VALIDATION_MESSAGE = "must be a well-formed email address";

    private static final String BLANK_USERNAME_VALIDATION_MESSAGE = "'username' is required";

    @Override
    String getBasePath() {
        return BASE_PATH;
    }

    @Test
    void createUser_validUsername_returnsCreatedUser() {
        var userDTO = UserDTO.builder().username("valid@mail.com").build();

        post()
            .withContent(userDTO)
            .withExpectedStatus(status().isCreated())
            .andExpect(jsonPath("$.id").doesNotExist()) // this is a piece of data that, for now, users don't need
            .andExpect(jsonPath("$.username", is(userDTO.getUsername())))
            .perform();
    }

    @ParameterizedTest
    @MethodSource("invalidUsernames")
    void createUser_invalidUsernames_throwsBadRequest(String username, List<String> expectedErrorMessages) {
        var userDTOWithWrongUsername = UserDTO.builder().username(username).build();

        post()
            .withContent(userDTOWithWrongUsername)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(expectedErrorMessages.toArray(String[]::new))
            .perform();
    }

    @Test
    void createUser_usingAnExistingUsername_throwsBadRequest() {
        var requestUsingExistingUsername = UserDTO.builder().username(DEFAULT_USERNAME).build();

        // this one must fail, as we are using an e-mail that already exists in the DB (see test/resources/data.sql)
        post()
            .withContent(requestUsingExistingUsername)
            .withExpectedStatus(status().isBadRequest())
            .andExpectErrorMessages(format("User %s already exists", requestUsingExistingUsername.getUsername()))
            .perform();
    }

    private static Stream<Arguments> invalidUsernames() {

        return Stream.of(
            Arguments.of(null, List.of(BLANK_USERNAME_VALIDATION_MESSAGE)),
            Arguments.of("", List.of(BLANK_USERNAME_VALIDATION_MESSAGE)),
            Arguments.of("   ", List.of(BLANK_USERNAME_VALIDATION_MESSAGE, EMAIL_VALIDATION_MESSAGE)),
            Arguments.of("@gmail.com", List.of(EMAIL_VALIDATION_MESSAGE)),
            Arguments.of("jchiocchio@", List.of(EMAIL_VALIDATION_MESSAGE)),
            Arguments.of("jchiocchio", List.of(EMAIL_VALIDATION_MESSAGE)),
            Arguments.of("jchiocchio@space gmail.com", List.of(EMAIL_VALIDATION_MESSAGE)),
            Arguments.of("jonathan chiocchio@gmail.com", List.of(EMAIL_VALIDATION_MESSAGE))
        );
    }
}
