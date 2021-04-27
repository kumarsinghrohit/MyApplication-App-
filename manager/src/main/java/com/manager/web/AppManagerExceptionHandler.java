package com.manager.web;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.coreapi.exception.InvalidImageFormatException;
import com.coreapi.exception.InvalidImageSizeException;

@Controller
@ControllerAdvice
public class AppManagerExceptionHandler {

    @ExceptionHandler(InvalidAppDataException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    Object handle(InvalidAppDataException ex) {
        StringBuilder msgBuilder = new StringBuilder();
        ex.getValidationMessages().forEach(msg -> msgBuilder.append(msg).append("<br/>"));
        return new ResponseEntity<>(msgBuilder.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidImageFormatException.class)
    Object handle(InvalidImageFormatException ex) {
        return new ResponseEntity<>(ex.getValidationMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidImageSizeException.class)
    Object handle(InvalidImageSizeException ex) {
        return new ResponseEntity<>(ex.getValidationMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    Object handle(IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error!! Please try again.");
    }
}
