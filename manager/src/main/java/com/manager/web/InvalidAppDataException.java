package com.manager.web;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class InvalidAppDataException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> validationMessages;
}
