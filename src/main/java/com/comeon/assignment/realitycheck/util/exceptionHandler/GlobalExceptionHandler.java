package com.comeon.assignment.realitycheck.util.exceptionHandler;

import com.comeon.assignment.realitycheck.util.exception.RealityCheckException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleResourseNotFoundException(NoSuchElementException e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RealityCheckException.class)
    public ResponseEntity<ApiError> handleResourseNotFoundException(RealityCheckException e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "status", "METHOD_NOT_ALLOWED"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllException(Exception e){
        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleAllValidationException(MethodArgumentNotValidException e){
        List<String> errors = e
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError
                .builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(errors.toString())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

}
