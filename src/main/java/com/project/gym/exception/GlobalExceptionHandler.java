package com.project.gym.exception;

import com.project.gym.dto.ErrorResponseDTO;
import com.project.gym.entity.enums.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler
{
    private final HttpServletRequest request;

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPasswordException(InvalidPasswordException e)
    {
        return sendErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), ApiError.INVALID_PASSWORD);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidTokenException(InvalidTokenException e)
    {
        log.warn("Attempted to use an invalid or expired JWT");
        return sendErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), ApiError.INVALID_TOKEN);
    }

    @ExceptionHandler(CookieNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCookieNotFoundException(CookieNotFoundException e)
    {
        log.warn("Attempted to perform an action without a JWT cookie");
        return sendErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), ApiError.COOKIE_NOT_FOUND);
    }

    @ExceptionHandler(MembershipNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleMembershipNotFoundException(MembershipNotFoundException e)
    {
        return sendErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), ApiError.MEMBERSHIP_NOT_FOUND);
    }

    @ExceptionHandler(InvalidMembershipAssignmentException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidMembershipAssignmentException(InvalidMembershipAssignmentException e)
    {
        log.warn("Attempted to create a membership for an ADMIN or STAFF");
        return sendErrorResponse(HttpStatus.CONFLICT, e.getMessage(), ApiError.INVALID_MEMBERSHIP_ASSIGNMENT);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExistsException(UserAlreadyExistsException e)
    {
        return sendErrorResponse(HttpStatus.CONFLICT, e.getMessage(), ApiError.USER_ALREADY_EXISTS);
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException e)
    {
        return sendErrorResponse(HttpStatus.CONFLICT, e.getMessage(), ApiError.PHONE_NUMBER_ALREADY_EXISTS);
    }







    /*
     * Non-custom exceptions
     */

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentMismatchException(MethodArgumentTypeMismatchException e)
    {
        return sendErrorResponse(HttpStatus.BAD_REQUEST, "Invalid URL parameter type.", ApiError.METHOD_ARGUMENT_MISMATCH);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
    {
        Map<String, String> errors = new HashMap<>();

        // Store the field and its validation message in the HashMap (field, validation_message)
        e.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
    {
        log.warn("Attempted to perform an action with an invalid request body");
        return sendErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body.", ApiError.HTTP_MESSAGE_NOT_READABLE);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException e)
    {
        return sendErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), ApiError.BAD_CREDENTIALS);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e)
    {
        // This exception is logged with WARN level in the methods where it may be thrown.
        return sendErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), ApiError.ACCESS_DENIED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException e)
    {
        return sendErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), ApiError.USERNAME_NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e)
    {
        log.error("Unexpected error occurred");
        return sendErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", ApiError.GENERIC);
    }

    private ResponseEntity<ErrorResponseDTO> sendErrorResponse(HttpStatus status, String message, ApiError error)
    {
        return ResponseEntity
                .status(status)
                .body(
                        new ErrorResponseDTO(
                                LocalDateTime.now(),
                                status.value(),
                                message,
                                error,
                                request.getRequestURI()
                        )
                );
    }
}
