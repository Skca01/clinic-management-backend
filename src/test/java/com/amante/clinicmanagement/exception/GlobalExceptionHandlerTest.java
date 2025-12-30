package com.amante.clinicmanagement.exception;

import com.amante.clinicmanagement.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void testHandleRuntimeException() {
        // Arrange
        String errorMessage = "Something went wrong";
        RuntimeException ex = new RuntimeException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleRuntimeException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandleUsernameNotFoundException() {
        // Arrange
        String errorMessage = "User not found";
        UsernameNotFoundException ex = new UsernameNotFoundException(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleUsernameNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandleBadCredentialsException() {
        // Arrange
        BadCredentialsException ex = new BadCredentialsException("Wrong password");

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleBadCredentialsException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        // Verify the handler overrides the message with "Invalid email or password"
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandleValidationExceptions() {
        // Arrange
        // We mock the MethodArgumentNotValidException and its inner BindingResult
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        // Create a specific field error to return
        FieldError fieldError = new FieldError("userDto", "email", "Email is required");
        List<ObjectError> errors = Collections.singletonList(fieldError);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response =
                globalExceptionHandler.handleValidationExceptions(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());

        Map<String, String> data = response.getBody().getData();
        assertNotNull(data);
        assertTrue(data.containsKey("email"));
        assertEquals("Email is required", data.get("email"));
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        String errorMessage = "Unexpected database error";
        Exception ex = new Exception(errorMessage);

        // Act
        ResponseEntity<ApiResponse<Object>> response =
                globalExceptionHandler.handleGenericException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        // Verify message format: "An unexpected error occurred: " + message
        assertEquals("An unexpected error occurred: " + errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}