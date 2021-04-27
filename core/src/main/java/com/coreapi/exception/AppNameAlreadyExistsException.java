package com.coreapi.exception;

import com.coreapi.valueobject.AppName;

/**
 * Exception raised when the {@link AppName} already exists for a user.
 */
public class AppNameAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -3338656210629236088L;

}
