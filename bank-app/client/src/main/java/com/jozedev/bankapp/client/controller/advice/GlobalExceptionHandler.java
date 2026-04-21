package com.jozedev.bankapp.client.controller.advice;

import com.jozedev.bankapp.client.exception.ClientNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> catchException(Exception exception) {
        LOGGER.error("Error inesperado", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, "Ocurrió un error inesperado"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> catchException(NoResourceFoundException exception) {
        LOGGER.error("Recurso no encontrado", exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "No se encontró el recurso solicitado"));
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> catchClientNotFoundException(ClientNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, exception.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(WebExchangeBindException ex) {
        Map<String, String> fieldErrors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, "Validation failed", fieldErrors));
    }

    public static class ErrorResponse {
        private int status;
        private String error;
        private Map<String, String> fields;

        public ErrorResponse(int status, String error) {
            this.status = status;
            this.error = error;
        }

        public ErrorResponse(int status, String error, Map<String, String> fields) {
            this.status = status;
            this.error = error;
            this.fields = fields;
        }

        public int getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

         public Map<String, String> getFields() {
            return fields;
        }
    }
}
