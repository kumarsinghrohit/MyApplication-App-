package com.coreapi.valueobject;

import java.util.Objects;

import lombok.ToString;
import lombok.Value;
import com.coreapi.exception.InvalidAppNameException;

@Value
@ToString(includeFieldNames = true)
public final class AppName {

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 35;
    private String value;

    public AppName(String value) {
        if (!isValid(value)) {
            throw new InvalidAppNameException();
        }
        this.value = value;
    }

    private boolean isValid(String value) {
        return Objects.nonNull(value) && value.length() >= MIN_LENGTH && value.length() <= MAX_LENGTH;
    }
}
