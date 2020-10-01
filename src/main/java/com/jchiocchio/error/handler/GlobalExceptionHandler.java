package com.jchiocchio.error.handler;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    private static final String JSON_PARSE_ERROR_MSG = "Unable to parse JSON input.";

    // Client-Side errors (not to be logged as errors)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
                          IllegalArgumentException.class,
                          ValidationException.class,
                          HttpMediaTypeNotSupportedException.class
                      })
    public ApplicationError handleBadRequest(HttpServletRequest req, Exception e) {
        return newApplicationErrorNoExceptionLogging(req, e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApplicationError handleHttpMessageNotReadable(HttpServletRequest req, @SuppressWarnings("unused")
        Exception e) {
        return newApplicationErrorNoExceptionLogging(req, JSON_PARSE_ERROR_MSG);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApplicationError handleMethodArgumentTypeMismatch(HttpServletRequest req,
                                                             MethodArgumentTypeMismatchException e) {
        String errorMessage = e.getMessage();
        Throwable cause = e.getCause();

        if (cause instanceof NumberFormatException) {
            errorMessage = format("Unable to bind '%s' to '%s'", e.getName(), e.getValue());
        } else if (cause instanceof IllegalArgumentException) {
            errorMessage = cause.getMessage();
        }
        return newApplicationErrorNoExceptionLogging(req, errorMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApplicationError handleBindException(HttpServletRequest req, BindException e) {
        return newApplicationErrorFromBindingResultNoLogging(req, e.getBindingResult());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApplicationError handleMethodArgumentNotValidException(HttpServletRequest req,
                                                                  MethodArgumentNotValidException e) {
        return newApplicationErrorFromBindingResultNoLogging(req, e.getBindingResult());
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApplicationError handleMethodNotAllowed(HttpServletRequest req, Exception e) {
        return newApplicationErrorNoExceptionLogging(req, e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = EntityNotFoundException.class)
    public ApplicationError handleEntityNotFound(HttpServletRequest req, EntityNotFoundException e) {
        return newApplicationErrorNoExceptionLogging(req, e.getMessage());
    }

    @ExceptionHandler(value = JpaObjectRetrievalFailureException.class)
    public ResponseEntity<ApplicationError> handleJpaObjectRetrievalFailure(HttpServletRequest req,
                                                                            JpaObjectRetrievalFailureException e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);

        if (rootCause instanceof EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(this.handleEntityNotFound(req, (EntityNotFoundException) rootCause));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(newApplicationErrorLoggingTheException(req, rootCause.getMessage(), rootCause));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ApplicationError handleConstraintViolation(HttpServletRequest req, Exception e) {
        return newApplicationErrorLoggingTheException(req, "Data integrity violation error", e);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ApplicationError> handleCompletionException(HttpServletRequest req, CompletionException e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);

        if (rootCause instanceof IllegalStateException) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                 .body(this.handleIllegalStateException(req, (IllegalStateException) rootCause));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(newApplicationErrorLoggingTheException(req, rootCause.getMessage(), rootCause));
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(IllegalStateException.class)
    public ApplicationError handleIllegalStateException(HttpServletRequest req, IllegalStateException e) {
        return newApplicationErrorLoggingTheException(req, e.getMessage(), e);
    }

    // Uncaught (i.e. server side) errors (to be logged as errors)

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ApplicationError uncaughtExceptionHandler(HttpServletRequest req, Exception e) {
        return newApplicationErrorLoggingTheException(req,
                                                      "Unexpected Error. Please contact support if this error persists",
                                                      e);
    }

    private ApplicationError newApplicationErrorFromBindingResultNoLogging(HttpServletRequest req,
                                                                           BindingResult bindingResult) {
        String[] errorMessages = bindingResult
            .getFieldErrors().stream()
            .map(this::processFieldError)
            .toArray(String[]::new);

        return newApplicationErrorNoExceptionLogging(req, errorMessages);
    }

    private String processFieldError(FieldError fieldError) {
        if (fieldError.isBindingFailure()) {
            return format("Invalid value '%s' for field '%s'", fieldError.getRejectedValue(), fieldError.getField());
        }
        return fieldError.getDefaultMessage();
    }

    private String newTrackingID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Use when you only want to return one or more custom error message(s) to the end user, without logging any error.
     *
     * @param req           the request involved
     * @param errorMessages the custom error messages to tbe included in the {@link ApplicationError} returned.
     */
    private ApplicationError newApplicationErrorNoExceptionLogging(HttpServletRequest req, String... errorMessages) {
        return ApplicationError.builder()
                               .messages(errorMessages)
                               .url(req.getRequestURL().toString())
                               .build();
    }

    /**
     * Logs the internal cause as an ERROR but returns <code>errorMessage</code> to the end-user, to hide internal
     * details, but with the addition of a tracking ID that should help developers correlate error reports from the
     * end-user with specific error seen in the logs.
     *
     * @param req                the request involved
     * @param errorMsgForEndUser the error to be shown to the end user
     * @param t                  the exception that is the cause of the error. Not to be shown to the end user.
     */
    private ApplicationError newApplicationErrorLoggingTheException(HttpServletRequest req,
                                                                    String errorMsgForEndUser,
                                                                    Throwable t) {
        if (t != null) {
            errorMsgForEndUser += ". Tracking ID: " + newTrackingID();
            log.error(errorMsgForEndUser, t);
        }

        return this.newApplicationErrorNoExceptionLogging(req, Optional.ofNullable(errorMsgForEndUser).orElse(""));
    }
}
